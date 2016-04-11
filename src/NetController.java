/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Payl
 */
public class NetController {

    private class NetInstance{
        private Object net;
        private final ConnectionInterface ci;
        private Calendar pingsent,pingrecieved;
        private final ByteBuffer recvbuf,sendbuf;
        public float reportlevel;
        public boolean needsupdate;
        NetInstance(Object _net,ConnectionInterface _ci) throws Exception{
            if (_net==null) throw new Exception("Unable to start networking (network interface returned connect/host error).");
            net=_net;
            ci=_ci;
            reportlevel=0.0f;
            needsupdate=true;
            recvbuf=ByteBuffer.allocate(5000);
            recvbuf.limit(0);
            sendbuf=ByteBuffer.allocate(5000);
            sendbuf.limit(0);
            ResetSendTimer();
            ResetRecvTimer();
        }
        void ResetSendTimer()
        {
            pingsent=Calendar.getInstance();
            pingsent.add(Calendar.SECOND, 1);
            
        }
        void ResetRecvTimer()
        {
            pingrecieved=Calendar.getInstance();
            pingrecieved.add(Calendar.SECOND, 3);
        }
        void Close()
        {
            ci.Close(net);
            net=null;
        }
        
        boolean IsConnected()
        {
            if (net==null) return false;
            if (!ci.IsOpen(net)) return false;
            if (pingrecieved.before(Calendar.getInstance())) return false;//Not responding
            if (pingsent.before(Calendar.getInstance())) 
            {
                Send(new byte[0]);
                ResetSendTimer();
            }
            return true;
        }
        
        byte[] Recv()
        {
            recvbuf.position(recvbuf.limit());
            recvbuf.limit(recvbuf.capacity());
            int i=ci.Read(net, recvbuf);
            if (i!=0) ResetRecvTimer();
            recvbuf.limit(recvbuf.position());
            recvbuf.position(0);
            if (recvbuf.remaining()<4) return null;
            int curlen=recvbuf.getInt();
            if (curlen==0)
            {
                //NOP
                ClearUpRecvBuf();
                return Recv();
            }
            if (recvbuf.remaining()<curlen) 
            {
                recvbuf.position(0);
                return null;
            }
            byte[] r=new byte[curlen];
            recvbuf.get(r);
            ClearUpRecvBuf();
            return r;
        }
        
        void Send(byte[] buf)
        {
            //System.out.println("BUFSTAT1: "+sendbuf.position()+" "+sendbuf.limit()+" "+sendbuf.capacity()+" "+sendbuf.remaining());
            sendbuf.position(sendbuf.limit());
            sendbuf.limit(sendbuf.capacity());
            //System.out.println("BUFSTAT2: "+sendbuf.position()+" "+sendbuf.limit()+" "+sendbuf.capacity()+" "+sendbuf.remaining());
            if (sendbuf.remaining()<=buf.length+4) 
            {
                Close();
                return;
            }
            sendbuf.putInt(buf.length);
            sendbuf.put(buf);
            //System.out.println("BUFSTAT3: "+sendbuf.position()+" "+sendbuf.limit()+" "+sendbuf.capacity()+" "+sendbuf.remaining());
            sendbuf.limit(sendbuf.position());
            sendbuf.position(0);
            //System.out.println("BUFSTAT4: "+sendbuf.position()+" "+sendbuf.limit()+" "+sendbuf.capacity()+" "+sendbuf.remaining());
            int i=ci.Write(net, sendbuf);
            //System.out.println("BUFSTAT5: "+sendbuf.position()+" "+sendbuf.limit()+" "+sendbuf.capacity()+" "+sendbuf.remaining());
            
            if (i!=0) 
            {
                ResetSendTimer();
                ClearUpSendBuf();
                //System.out.println("BUFSTAT6: "+sendbuf.position()+" "+sendbuf.limit()+" "+sendbuf.capacity()+" "+sendbuf.remaining());
            
            }
        }

        private void ClearUpRecvBuf() {
            byte[] tmp=new byte[recvbuf.remaining()];
            recvbuf.get(tmp);
            recvbuf.position(0);
            recvbuf.put(tmp);
            recvbuf.limit(recvbuf.position());
            recvbuf.position(0);
        }
        private void ClearUpSendBuf()
        {
            byte[] tmp=new byte[sendbuf.remaining()];
            sendbuf.get(tmp);
            sendbuf.position(0);
            sendbuf.put(tmp);
            sendbuf.limit(sendbuf.position());
            sendbuf.position(0);
        }
        
    }
    private final boolean is_server;
    private NetInstance clients[];
    private Object host_sock;
    private NetInstance conn;
    private final ConnectionInterface ci;
    private final GameController gc;
    public int mypid;
    NetController(GameController _gc,ConnectionInterface _ci,String ip,int port) throws Exception
    {
        gc=_gc;
        ci=_ci;
        is_server=false;
        conn=new NetInstance(ci.ConnectToServer(ip, port), ci);
        if (conn==null) throw new Exception("No connection");
        mypid=-1;
    }
    NetController(GameController _gc,ConnectionInterface _ci,int port) throws Exception
    {
        gc=_gc;
        ci=_ci;
        is_server=true;
        host_sock=ci.CreateServer(port);
        if (host_sock==null) throw new Exception("Cannot start listening on port");
        clients=new NetInstance[4];
        mypid=-1;
    }
    
    public void SetClientNeedsupdate(int clientid, boolean value) { 
        if (clients[clientid]==null) return;
        clients[clientid].needsupdate = value; 
    }
    public boolean GetClientNeedsupdate(int clientid) { 
        if (clients[clientid]==null) return false;
        return clients[clientid].needsupdate; 
    }
    
    private void Kick(byte playerid) {
        gc.ServerMessage(-1, "Player "+playerid+" was kicked.");
        Disconnect(playerid);
    }
    
    void Disconnect(byte playerid)
    {
        clients[playerid].Close();
        clients[playerid]=null;
        gc.playerdeath(playerid,true);
        gc.playerleft(playerid);
    }
    
    void ClientSend(byte[] buf)
    {
        assert(!is_server);
        conn.Send(buf);
    }
    
    float reportupdate(int playerid,float mult,float add)
    {
        if (clients[playerid]==null) return 0.0f;
        clients[playerid].reportlevel=clients[playerid].reportlevel*mult+add;
        return clients[playerid].reportlevel;
    }
    
    void Send(int playerid,byte[] buf)
    {
        if (clients[playerid]==null) return;
        clients[playerid].Send(buf);
    }
    
    void OnGetsChat(int senderid,byte[] buf)
    {
        byte[] tmp=new byte[buf.length-1];
        System.arraycopy(buf, 1, tmp, 0, tmp.length);
        String chat = new String(tmp);
        if (is_server)
        {
            gc.ServerRelayChat(senderid, chat);
        } else
        {
            gc.DisplayChatMessage(chat);
        }
    }
    
    void OnGetPlayerInfoPowerups(byte sender,byte[] cmd)
    {
        if (is_server) {Kick(sender);return;}
        ByteBuffer buf=ByteBuffer.wrap(cmd);
        buf.position(1);
        int playerid=buf.get();
        if (gc.players[playerid]==null) return;
        gc.players[playerid].bombcntmax=buf.get();
        gc.players[playerid].bombrange=buf.get();
        gc.players[playerid].godmodelen=buf.getFloat();
        gc.players[playerid].speedupdatelen=buf.getFloat();
    }
    void SendPlayerInfoPowerups(int toplayerid,byte playerid)
    {
        ByteBuffer buf=ByteBuffer.allocate(12);
        buf.put((byte)0x23);
        buf.put(playerid);
        buf.put((byte)gc.players[playerid].bombcntmax); 
        buf.put((byte)gc.players[playerid].bombrange);
        buf.putFloat((float)gc.players[playerid].godmodelen);
        buf.putFloat((float)gc.players[playerid].speedupdatelen);
        if (toplayerid==-1)
        {
            Broadcast(buf.array());
        } else Send(toplayerid, buf.array());
    }
    
    void OnGetBombPlacement(byte sender,byte playerid,byte cx,byte cy)
    {
        if ((sender!=playerid)&&(is_server)) Kick(sender);
        if ((gc.players[playerid]==null)&&(is_server)) return;
        if (gc.placebomb(playerid, cx, cy, is_server)&&(is_server))
        {
            Broadcast(new byte[]{(byte)0x10,(byte)playerid,(byte)cx,(byte)cy});
        }
    }
    
    void OnBlockUpdate(byte sender,byte[] cmd)
    {
        if (is_server) {Kick(sender);return;}
        ByteBuffer buf=ByteBuffer.wrap(cmd);
        buf.position(1);
        int x=buf.get();
        int y=buf.get();
        gc.map.data[x][y].type=Map.FieldType.decode(buf.get());
        gc.map.data[x][y].subtype=buf.get();
        gc.map.data[x][y].color=buf.get();
        gc.map.data[x][y].duration_progress=buf.getFloat();
        //gc.map.data[x][y].ground_dmg=0;
    }
    
    void OnPlayerStatusChange(byte sender,byte playerid,byte status)
    {
        if (is_server) Kick(sender);
        else
        {
            switch (status){
                case 0:
                    gc.newplayer(playerid);
                    break;
                case 1:
                    gc.playerdeath(playerid,true);
                    break;
                case 2:
                    gc.playerleft(playerid);
                    break;
            } 
        }
        
    }
    
    void OnGetsPID(byte sender,byte playerid)
    {
        System.out.println("GOT PID: "+playerid);
        if (is_server) Kick(sender);
        else mypid=playerid;
    }
    
    void SendMap(byte playerid)
    {
        try {
            ByteArrayOutputStream buf=new ByteArrayOutputStream();
            buf.write(0x21);
            buf.write(gc.map.AsFullBlob());
            
            clients[playerid].Send(buf.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(NetController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void BroadcastBlockUpdate(byte x,byte y)
    {
        ByteBuffer buf=ByteBuffer.allocate(10);
        buf.put((byte)0x22);
        buf.put(x);
        buf.put(y);
        buf.put(gc.map.data[x][y].type.getCode());
        buf.put(gc.map.data[x][y].subtype);
        buf.put(gc.map.data[x][y].color);
        buf.putFloat(gc.map.data[x][y].duration_progress);
        Broadcast(buf.array());
    }
    
    void OnGetsMap(byte sender,byte[] map)
    {
        if (is_server) {Kick(sender);return;}
        gc.map.GetFullBlob(Arrays.copyOfRange(map, 1, map.length));
    }
    
    boolean IsConnected(byte playerid)
    {
        return clients[playerid]!=null;
    }
    
    public void SendMove(byte toplayer,byte aboutplayerid,byte dir,boolean useoldpos)
    {
        if (gc.players[aboutplayerid]==null) return;//fallback
        byte[] buf= new byte[5];
        buf[0]=0x04;
        buf[1]=aboutplayerid;
        if (useoldpos)
        {
            buf[2]=(byte) gc.players[aboutplayerid].oldx;
            buf[3]=(byte) gc.players[aboutplayerid].oldy;
        }else
        {
            buf[2]=(byte) gc.players[aboutplayerid].newx;
            buf[3]=(byte) gc.players[aboutplayerid].newy;
        }
        
        buf[4]=(byte)dir;
        if (is_server) {Send(toplayer, buf);} else
        {
            conn.Send(buf);
        }
    }
    
    boolean RecieveCommand(byte[] cmd,byte sender)
    {
        if (cmd[0]==0x04)//Player moves
        {
            if (cmd.length!=5) return false;
            OnGetsMove(sender, cmd[1], cmd[2], cmd[3], cmd[4]);
            return true;
        }
        
        if (cmd[0]==0x05)//Getting pid
        {
            if (cmd.length!=2) return false;
            OnGetsPID(sender, cmd[1]);
            return true;
        }
        if (cmd[0]==0x06)
        {
            if (cmd.length!=3) return false;
            OnPlayerStatusChange(sender, cmd[1], cmd[2]);
        }
        if (cmd[0]==0x10)
        {
            if (cmd.length!=4) return false;
            OnGetBombPlacement(sender, cmd[1], cmd[2], cmd[3]);
            return true;
        }
        if (cmd[0]==0x01)
        {
            if (cmd.length==1) return false;
            OnGetsChat(sender, cmd);
            return true;
        }
        if (cmd[0]==0x21)
        {
            OnGetsMap(sender, cmd);
            return true;
        }
        
        if (cmd[0]==0x22)
        {
            if (cmd.length!=10) return false;
            OnBlockUpdate(sender, cmd);
        }
        if (cmd[0]==0x23)
        {
            if (cmd.length!=12) return false;
            OnGetPlayerInfoPowerups(sender, cmd);
        }
        
        if (cmd[0]==0x24)
        {
            if (cmd.length!=3) return false;
            if (is_server) Kick(sender); else gc.map.TryExplodeBomb(cmd[1], cmd[2]);
        }
        
        if (cmd[0]==0x25)
        {
            if (cmd.length!=2) return false;
            if (is_server) Kick(sender); else gc.ui.updateTimerAutoDec(cmd[1]);
        }
        
        return false;
    }
    
    void OnGetsMove(byte sender,byte playerid,byte cx,byte cy,byte dir)
    {
        if ((is_server)&&(sender!=playerid)) {Kick(sender);return;}
        gc.move_from_net(playerid, cx, cy, dir);
        //boolean ok=gc.playermoves(playerid, dir, cx, cy, is_server,false);
        //if (!is_server) return;
        /*if (!ok) {
            SendMove(sender,sender,(byte)gc.players[sender].lastdir,true);
            gc.players[sender].needsupdate=true;
            }*/
    }
    void UpdateTimer(int playerid,int time_in_secs)
    {
        if (playerid==-1) Broadcast(new byte[]{0x25,(byte)time_in_secs}); else Send(playerid, new byte[]{0x25,(byte)time_in_secs});
    }
    void Broadcast(byte[] buf)
    {
        if (is_server){
            for(int i=0;i<4;i++)
            {
                if (clients[i]==null) continue;
                clients[i].Send(buf);
            }
        } else
        {
            conn.Send(buf);
        }
    }
    
    void tick() throws Exception
    {
        if (is_server)
        {
            Object newconn=ci.GrabServerConnection(host_sock);
            if (newconn!=null)
            {
                int j=-1;
                for(int i=0;i<4;i++) if (clients[i]==null) {
                    j=i;
                    break;
                }
                if (j==-1) ci.Close(newconn); else
                {
                    clients[j]=new NetInstance(newconn, ci);
                    byte[] msg=new byte[2];
                    msg[0]=0x05;
                    msg[1]=(byte)j;
                    gc.ServerMessage(-1,"Player "+j+" has joined the game.");
                    clients[j].Send(msg);//Send assigned playerid to player
                    UpdateTimer(j, (int) gc.rc.queryTime(true));
                    //gc.newplayer(j);
                }
            }
            for(int i=0;i<4;i++) if (clients[i]!=null)
            {
                
                //if ((p.needsupdate)&&(is_server))
                
                
                if (GetClientNeedsupdate(i))
                {
                    System.out.println("(Re)Sending state to player: "+i);
                    SendMap((byte) i);
                    for (int j=0;j<4;j++)
                    {
                        if (gc.players[j]==null) continue;
                        SendMove((byte)i, (byte)j, (byte)gc.players[j].lastdir,true);
                        SendPlayerInfoPowerups(i, (byte)j);
                    }
                    //p.needsupdate=false;
                    SetClientNeedsupdate(i, false);
                    //reportPlayer(i,1,1.0f);
                }
                if (clients[i]==null) continue;
                if (!clients[i].IsConnected())
                {
                    Disconnect((byte)i);
                    continue;
                }
                
                
                do{
                    byte[] buf=clients[i].Recv();
                    if (buf!=null)
                    {
                        if (!RecieveCommand(buf, (byte)i))
                        {
                            //Invalid command, protocol error: todo - report it
                        }
                    } else break;
                    if (clients[i]==null) break;
                } while(true);
            }
        } else
        {
            if (!conn.IsConnected()) throw new Exception("Connection lost");
            do{
                byte[] buf=conn.Recv();
                if (buf!=null)
                {
                    RecieveCommand(buf, (byte)0);
                } else break;
            } while(true);
        }        
    }
    int countClients()
    {
        int number = 0;
        for(int i=0;i<4;i++)
        {
            if (clients[i]!=null) number++;
        }
        return number;
    }
}
