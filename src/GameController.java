/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import static java.lang.Thread.sleep;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Payl
 */
public class GameController {
    public Random rand;
    public Map map;//our map we present to user
    public Player players[];
    boolean is_server=false;
    public NetController nc;
    public RoundController rc;
    
    public Chat mychat;
    public UIInterface ui;
    public float report_dec_timer;
    
    GameController(Map m,String destip,Chat _mychat,UIInterface _ui) throws Exception
    {
        rand=new Random();
        map=m;
        m.gc=this;
        ConnectionInterface connectionvia=null;
        if (destip.startsWith("util")) 
        {
            connectionvia=new ConnectionViaUtil();
            System.out.println("Using NIO network interface");
            destip=destip.replace("util", "");
        } else 
        {
            try{
                connectionvia=new ConnectionViaNative();
                System.out.println("Using native network interface");
            }catch(Exception ex)
            {
                Logger.getLogger(Bomberman.class.getName()).log(Level.SEVERE, "Cant load native internet interface", ex);
                System.out.println("!!! NETWORK FALLBACK TO NIO !!!");
                connectionvia=new ConnectionViaUtil();
                System.out.println("Using NIO network interface");
            }
        }
        is_server=(destip.length()==0);
        mychat=_mychat;
        ui=_ui;
        players=new Player[4];
        int port=10024;
        if (is_server)
        {
            nc=new NetController(this,connectionvia, port);
        } else
        {
            if (destip.equals("fetch"))
            {
                destip=fetchfromservice.GetIPFromService();
            }
            nc=new NetController(this,connectionvia,destip ,port);
            while (nc.mypid==-1)
            {
                nc.tick();
            }
        }
        if(is_server) rc=new RoundController(this);
        
    }
    
    void reloadmap()
    {
        try{
            map=new Map(map.map_name);
            map.gc=this;
            ui.updateMap(map);
        }catch(Exception ex){
            Logger.getLogger(Bomberman.class.getName()).log(Level.SEVERE, "Cant reload map", ex);
        }
    }
    void roundstarts()
    {
        for(int i=0;i<4;i++)
        {
            if (players[i]==null) continue;
            playerdeath(i,false);
            newplayer(i);
            //players[i].needsupdate=true;
            nc.SetClientNeedsupdate(i, true);
        }
    }
    
    void newplayer(int playerid)
    {
        assert(players[playerid]==null);
        
        int x=0;
        int y=0;
        switch(playerid){
            case 0: x=1; y=1; 
                break;
            case 1: x=map.WIDTH-2; y=1;
                break;
            case 2: x=1; y=map.HEIGHT-2;
                break;
            case 3: x=map.WIDTH-2; y=map.HEIGHT-2;
                break;
            default: assert(false);
        }
        
        players[playerid]=new Player(map, x, y);//todo - check coords
        map.data[x][y].color=(byte) playerid;
        if (is_server)
        {
            nc.Broadcast(new byte[]{0x06,(byte)playerid,(byte)0});
        }
    }
    void playerdeath(int playerid,boolean is_from_server)
    {
        if (players[playerid]==null) return;
        if (is_server)
        {
            nc.Broadcast(new byte[]{0x06,(byte)playerid,(byte)1});
        }
        if (is_from_server || is_server) players[playerid]=null;
    }
    
    void playerleft(int playerid)
    {
        if (is_server)
        {
            nc.Broadcast(new byte[]{0x06,(byte)playerid,(byte)2});
        }
        players[playerid]=null;
        
        if(is_server) ServerMessage(-1,"Player "+playerid+" has left the game.");
    }
    
    boolean placebomb(int playerid, int cx,int cy, boolean check_validity)
    {
        Player p=players[playerid];
        if (check_validity)
            {
            if (p==null) return false;
            if (p.moves) return false;
            if ((cx!=p.oldx)||(cy!=p.oldy)) return false;
            if (p.bombcntmax<=map.BombsFromPlayer(playerid)) return false;
            }
        map.data[cx][cy].type=Map.FieldType.Bomb;
        map.data[cx][cy].subtype=(byte)playerid;
        map.data[cx][cy].duration_progress=2000;
        return true;
    }
   
    boolean playermoves(int playerid, int dir,int cx,int cy, boolean check_validity,boolean strict)
    {
        Player p=players[playerid];
        if (check_validity)
            {
            if (p==null) return false;
            if (p.moves)
                {
                    if (strict) return false;
                    float minlevel=0.7f;
                    if (p.speedupdatelen>0.0f) minlevel=0.45f;
                    float mult=1.0f/(1.0f-minlevel);
                    if (p.moveprog>=minlevel)
                    {
                        reportPlayer(playerid, 2, 1-(p.moveprog-minlevel)*mult);
                    } else return false;//DEBUG
                }
            //if (!((p.oldx==cx)&&(p.oldy==cy))) return false;
            int nx=map.GetDirX(p.newx, dir);
            int ny=map.GetDirY(p.newy, dir);
            if (!map.CanPlayerStand(nx, ny)) return false;
            }
        if (p==null)
        {
            newplayer(playerid);
            p=players[playerid];
        }
        if (cx==-1) p.startmove(dir); 
        else
        {
            if ((p.oldx==cx)&&(p.oldy==cy)) p.startmove(dir); else
              {p.Warp(cx, cy);p.startmove(dir);}
        }
        map.data[p.newx][p.newy].color=(byte) playerid;
        //if (is_server)
        //{
        //    for(byte i=0;i<4;i++)  if ((nc.IsConnected(i))&&(i!=playerid)) nc.SendMove(i,(byte) playerid, (byte) dir,true);
        //}
        return true;
    }
    
    void move_from_net(int playerid,int cx,int cy,int dir)
    {
        
        if (!playermoves(playerid, dir, cx, cy, is_server,false))
        {
            assert(is_server);
            if (players[playerid]==null) return;
            //Error moving player: resend state to player.
            reportPlayer(playerid, 3, 1.0f);
            //players[playerid].needsupdate=true;
            nc.SetClientNeedsupdate(playerid, true);
        } else //Dispatch move
        {
            move_events(playerid);
            if (is_server) for(int i=0;i<4;i++)
            {
                if ((nc.IsConnected((byte)i))&&(i!=playerid)) nc.SendMove((byte)i,(byte)playerid,(byte)dir,true);
            }
        }
    }
    
    void DisplayChatMessage(String chat)
    {
        mychat.addMessageToChat(chat);
    }
    
    void ClientMessage(String chat)//TODO -allow entering chat message
    {
        if (chat.length()==0) return;
        if (is_server) ServerMessage(-1, chat); 
        else
        {
            SendChatMessage(nc.mypid, -1, chat);
        }
    }
    
    void ServerMessage(int playerid,String chat)
    {
        if (chat.length()==0) return;
        if (playerid==-1) DisplayChatMessage("[Me] "+chat); else
            DisplayChatMessage("[To "+playerid+"] "+chat);
        SendChatMessage(playerid,-1,"[Srv] "+chat);
        if ((playerid==-1)&&(chat.equals("endround"))) rc.EndRound();
    }
    
    void ServerRelayChat(int playerid,String chat)
    {
        chat="["+playerid+"] "+chat;
        DisplayChatMessage(chat);
        SendChatMessage(-1,playerid,chat);
    }
    
    void SendChatMessage(int playerid,int rejectid,String chat)
    {
        byte[] buf = new byte[chat.length()+1];
        buf[0]=0x01;
        byte[] buf2=chat.getBytes();
        System.arraycopy(buf2, 0, buf, 1, buf2.length);
        if (is_server)
        {
            if ((playerid==rejectid)&&(rejectid!=-1)) return;
            if (playerid!=-1)
            {
                nc.Send(playerid, buf);
            } else for(int i=0;i<4;i++)
            {
                if (i!=rejectid) nc.Send(i, buf);
            }
        } else
        {
            DisplayChatMessage("[Me] "+chat);
            nc.ClientSend(buf);
        }
        
    }
    void move_events(int playerid)
    {
       Player p=players[playerid];
       if (p==null) return;
       if ((map.DoesPlayerDie(playerid,p.oldx, p.oldy))
          ||(map.DoesPlayerDie(playerid,p.newx, p.newy)))
            {
             playerdeath(playerid,false);
            }
       if ((map.data[p.oldx][p.oldy].type==Map.FieldType.Powerup)&&(is_server))
            {

            switch(map.data[p.oldx][p.oldy].subtype)
            {
                case 0:p.bombcntmax++;break;
                case 1:p.bombrange++;break;
                case 2:p.speedupdatelen=10000;break;
                case 3:p.godmodelen=5000;break;
            }
            map.data[p.oldx][p.oldy].type=Map.FieldType.Nothing;
            map.data[p.oldx][p.oldy].subtype=0;
            nc.BroadcastBlockUpdate((byte)p.oldx, (byte)p.oldy);
            nc.SendPlayerInfoPowerups(-1, (byte)playerid);
        } 
    }
    void move_from_keyboard(int dir)
    {
        if (nc.mypid==-1) return;//We are not controlling anyone
        if (players[nc.mypid]==null) return;//Our player is dead
        if (dir==5)
        {
            //Placing a bomb
            if (players[nc.mypid].moves) return; // TODO perhaps change so that placing a bomb while being in motion is possible
            assert(!is_server);
            nc.ClientSend(new byte[]{(byte)0x10,(byte)nc.mypid,(byte)players[nc.mypid].oldx,(byte)players[nc.mypid].oldy});
            return;
        }
        if (playermoves(nc.mypid, dir, players[nc.mypid].oldx, players[nc.mypid].oldy, true,true))
        {
            nc.SendMove((byte)0, (byte)nc.mypid, (byte)dir,true);
        }
    }
    
    void tick(float time_delta) throws Exception
    {
        //TESTING:
        //if (!is_server) sleep(rand.nextInt(6));
        //END TESTING
        //sleep(50);
        boolean dec_reports=false;
        if (report_dec_timer<0.0)
        {
            dec_reports=is_server;
            report_dec_timer=500.0f;
            
        } else report_dec_timer-=time_delta;
        map.UpdateTerrain(time_delta);
        //Process how player moves
        for(int i=0;i<4;i++)
        {
            Player p=players[i];
            if (p==null) continue;
            p.tick(time_delta);
            if (p==null) continue;
            if (dec_reports)
            {
                nc.reportupdate(i, 0.9f, 0);
            }
            move_events(i);
            
        }
        nc.tick();
        if(is_server) rc.tick();
    }
    
    void reportPlayer(int playerid,int id,float value)
    {
        //TODO
        /*
        ID MAP:
        1-state has been resent
        2-small wrongdoing in move
        3-wrong move
        */
        float oldval=nc.reportupdate(playerid, 1.0f, 0.0f);
        //ServerMessage(-1, "Player "+playerid+" report for "+id+" value "+value+" oldval "+oldval);
        
        
        float mult=0.0f;
        float add=0.0f;
        if (id==1)
        {
            assert(false);//now this is disabled
            mult=1.5f;
            add=5.0f;
        } else
            if (id==2)
            {
                mult=(1.0f+value/2);
                add=value*2;
            } else
            if (id==3)
            {
                mult=1.5f;
                add=value*25;
            }
        
        float val=nc.reportupdate(playerid, mult, add);
        float level[]={50.0f,200.0f,400.0f,500.0f};
        for (int i=0;i<4;i++)
        {
            if ((val>=level[i]) &&(oldval<=level[i]))
            {
                if (i==3) 
                {
                    ServerMessage(playerid, "You have been kicked by anticheat system");
                    ServerMessage(-1, "Player "+playerid+" is kicked by anticheat system");
                    nc.Disconnect((byte)playerid);
                    return;
                }
                if (i!=0) ServerMessage(-1, "Player "+playerid+" is warned by anticheat system (level: "+i+")");
                ServerMessage(playerid, "Warning: Seems like there is trouble with proper communication between you and server");
            }
        }
    }
}
