/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Payl
 */

public class Map {
    public GameController gc;
    public static final int WIDTH=15;
    public static final int HEIGHT=13;
    public static enum FieldType {
            Nothing((byte)0),
            Wall((byte)1),
            Destructible_Wall((byte)2),
            Powerup((byte)3),
            Explosion((byte)4),
            Bomb((byte)5);
            private final byte code;

            FieldType (byte code) {
                this.code = code;
            }

            static final FieldType[] ftypes = new FieldType[16];
            static {
                for(FieldType c: values())
                    ftypes[c.getCode()] = c;
            }

            public byte getCode() {
                return code;
            }

            public static FieldType decode(byte b) {
                return ftypes[b];
            }
        };
    public class Field{
        Field()
        {
            type=FieldType.Nothing;
            ground_dmg=0;
            subtype=0;
            color=-1;
        }
        public FieldType type;
        public byte subtype;
        public byte ground_dmg;
        public byte color;
        public float duration_progress;
    }
    
    public Field data[][];
    public String map_name;
    
    Map()
    {
        map_name="";
        data=new Field[WIDTH][HEIGHT];
        for(int i=0;i<15;i++)
            for(int j=0;j<13;j++)
                data[i][j]=new Field();
    }
    Map(String filename) throws Exception{  
        map_name=filename;
        String line;
        char c;
        try (InputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr)) 
        {               
            data=new Field[WIDTH][HEIGHT];
            
            for(int j=0;j<13;j++){
                line = br.readLine();
                
                for(int i=0;i<15;i++){
                    data[i][j]=new Field();
                    
                    c = line.charAt(i);
                    switch(c){
                        case ' ': data[i][j].type = FieldType.Nothing;
                            data[i][j].subtype = 0;
                            break;
                        case '#': data[i][j].type = FieldType.Wall;
                            data[i][j].subtype = 0;
                            break;
                        case 'D': data[i][j].type = FieldType.Destructible_Wall;
                            data[i][j].subtype = 1;
                            break;    
                        case 'E': data[i][j].type = FieldType.Destructible_Wall;
                            data[i][j].subtype = 2;
                            break;    
                        case '0': data[i][j].type = FieldType.Powerup; //bmbcntmax
                            data[i][j].subtype = 0;
                            data[i][j].duration_progress=500000;
                            break;
                        case '1': data[i][j].type = FieldType.Powerup; //bmbrange
                            data[i][j].subtype = 1;
                            data[i][j].duration_progress=500000;
                            break;
                        case '2': data[i][j].type = FieldType.Powerup; //speed
                            data[i][j].subtype = 2;
                            data[i][j].duration_progress=500000;
                            break;
                        case '5': data[i][j].type = FieldType.Powerup; //godmod
                            data[i][j].subtype = 3;
                            data[i][j].duration_progress=500000;
                            break;
                        default: assert(false);
                            break;
                    }
                    
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, "Couldnt load file "+filename, ex);
            ex.printStackTrace();
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, "Couldnt load file "+filename, ex);
            ex.printStackTrace();
            throw ex;
        }
    }
    int BombsFromPlayer(int playerid)
    {
        int n=0;
        for (int i=0;i<WIDTH;i++)
        {
            for(int j=0;j<HEIGHT;j++)
            {
                if ((data[i][j].type==FieldType.Bomb)&&(data[i][j].subtype==(byte)playerid))
                {
                    n++;
                }
            }
        }
        //System.out.println("BOMBS: "+n);
        return n;
    }
    static int GetDirX(int x,int dir)
    {
        if (dir==0) return x-1;
        if (dir==2) return x+1;
        return x;
    }
    static int GetDirY(int y,int dir)
    {
        if (dir==1) return y-1;
        if (dir==3) return y+1;
        return y;
    }
    
    static boolean IsValidXY(int x, int y) {
        return ((x<WIDTH)&&(y<HEIGHT)&&(x>=0)&&(y>=0));
    }
    
    byte[] AsBaseBlob()
    {
        byte[] r=new byte[WIDTH*HEIGHT*4];
        int n=0;
        for (int i=0;i<WIDTH;i++)
        {
            for(int j=0;j<HEIGHT;j++)
            {
                r[n++]=data[i][j].type.getCode();
                r[n++]=data[i][j].subtype;
                r[n++]=data[i][j].color;
                r[n++]=data[i][j].ground_dmg;
            }
        }
        return r;
    }
    int GetBaseBlob(byte[] m)
    {
        if (m.length<WIDTH*HEIGHT*4) return 0;
        int n=0;
        for (int i=0;i<WIDTH;i++)
        {
            for(int j=0;j<HEIGHT;j++)
            {
                data[i][j].type=FieldType.decode(m[n++]);
                data[i][j].subtype=m[n++];
                data[i][j].color=m[n++];
                data[i][j].ground_dmg=m[n++];
            }
        }
        return n;
    }
    
    byte[] AsFullBlob() throws IOException
    {
        ByteArrayOutputStream r=new ByteArrayOutputStream();
        r.write(AsBaseBlob());
        for (int i=0;i<WIDTH;i++)
        {
            for(int j=0;j<HEIGHT;j++)
            {
                if (data[i][j].duration_progress!=0.0)
                {
                    //System.out.println(i+" "+j+" SENT!");
                    r.write(i);
                    r.write(j);
                    r.write(ByteBuffer.allocate(4).putFloat(data[i][j].duration_progress).array());
                }
            }
        }
        return r.toByteArray();
    }
    
    void GetFullBlob(byte[] m)
    {
        int n=GetBaseBlob(m);
        ByteBuffer buf =ByteBuffer.wrap(m);
        while (n!=m.length)
        {
            int i=buf.get(n++);
            int j=buf.get(n++);
            //System.out.println(n+" "+buf.limit()+" "+i+" "+j);
            data[i][j].duration_progress=buf.getFloat(n);
            n+=4;
        }
    }
    
    boolean CanPlayerStand(int x,int y)
    {
        if (!IsValidXY(x, y)) return false;
        // TODO check if other player isnt already there
        switch(data[x][y].type)
        {
            case Bomb:
            case Destructible_Wall:
            case Wall:
                return false;
            case Explosion:
            case Nothing:
            case Powerup:
                return true;
        }
        return false;
    }
    
    boolean DoesPlayerDie(int playerid,int x,int y)
    {
        if (!IsValidXY(x, y)) return true;
        assert(gc!=null);
        assert(gc.players[playerid]!=null);
        if (gc.players[playerid].godmodelen>0.0) return false;
        if (data[x][y].type==FieldType.Explosion) return true;
        return false;
    }
    
    void UpdateTerrain(float time_delta)
    {
        for(int x=0;x<WIDTH;x++)
        {
            for(int y=0;y<HEIGHT;y++)
            {
                switch (data[x][y].type)
                {
                    case Nothing:
                    case Destructible_Wall:
                    case Wall:
                        break;
                    case Powerup:
                    case Explosion:
                        data[x][y].duration_progress-=time_delta;
                        if (data[x][y].duration_progress<=0)
                        {
                            data[x][y].subtype=0;
                            data[x][y].type=FieldType.Nothing;
                        }
                        break;
                    case Bomb:
                        data[x][y].duration_progress-=time_delta;
                        if (data[x][y].duration_progress<=0)
                        {
                            if (gc.is_server) BombExplodes(x, y);
                        }
                        break;
                }
            }
        }
    }
    
    void PlacePowerup(int x,int y)
    {
        //if (!gc.is_server) return;
        if ((gc.rand.nextInt(3)==0)||(!gc.is_server)) {
            data[x][y].type=FieldType.Nothing;
            data[x][y].subtype=0;
            data[x][y].duration_progress=0;
            //gc.nc.BroadcastBlockUpdate((byte)x, (byte)y);
            return;
        }
        data[x][y].type=FieldType.Powerup;
        data[x][y].duration_progress=500000;
        data[x][y].subtype=(byte) gc.rand.nextInt(4);
        gc.nc.BroadcastBlockUpdate((byte)x, (byte)y);
        //assert(false);//TODO - send info to client
        //assert(false);
    }
    
    void TryExplodeBomb(int x,int y)
    {
        if (data[x][y].type==FieldType.Bomb)
        {
            BombExplodes(x, y);
        }
    }
    
    void BombExplodes(int x,int y)
    {
        assert(data[x][y].type==FieldType.Bomb);
        if (gc.is_server)
        {
            gc.nc.Broadcast(new byte[]{0x24,(byte)x,(byte)y});
        }
        data[x][y].type=FieldType.Explosion;
        data[x][y].duration_progress=500;
        int playerid=data[x][y].subtype;//Player who placed bomb TODO not always the id of the actual player who placed the bomb
        data[x][y].subtype=0;
        data[x][y].color=-1;
        data[x][y].ground_dmg++;
        Player p=gc.players[playerid];
        int player_bomb_explosion_radius=2;//Constant for now
        if (p!=null) player_bomb_explosion_radius=p.bombrange;
        ArrayList<Point> v=new ArrayList<>();
        for (int i=0;i<4;i++)//Produce explosion in all 4 directions.
        {
            int tx=x;
            int ty=y;
            for(int j=0;j<player_bomb_explosion_radius;j++)
            {
                switch(i)
                {
                    case 0: tx++;break;
                    case 1: tx--;break;
                    case 2: ty++;break;
                    case 3: ty--;break;
                }
                if (!IsValidXY(tx,ty)) break;
                boolean destroys=false;
                boolean continue_exploding=true;
                switch(data[tx][ty].type)
                {
                    case Bomb:
                        if (gc.is_server) v.add(new Point(tx,ty));
                        break;
                    case Destructible_Wall:
                        int tmp=data[tx][ty].subtype;
                        if (tmp<=1) {destroys=false;PlacePowerup(tx,ty);} else//Setting destroys is needed for cases where two bombs explode at once
                            data[tx][ty].subtype--;
                        continue_exploding=false;
                        break;
                    case Wall:continue_exploding=false;break;
                    case Nothing:destroys=true;break;
                    case Powerup:
                        destroys=true;break;
                    case Explosion:destroys=true;break;
                }
                if (destroys)
                {
                    data[tx][ty].type=FieldType.Explosion;
                    //System.out.println(data[tx][ty].color+" "+playerid);
                    if (data[tx][ty].color!=playerid) data[tx][ty].color=-1;
                    data[tx][ty].duration_progress=500;
                }
                if (!continue_exploding) break;
                data[tx][ty].ground_dmg++;
            }
        } 
        for (Point pkt: v)
        {
            TryExplodeBomb(pkt.x, pkt.y);
        }
    }
    
    int countPlayerTiles(int playerid)
    {
        int number = 0;
        for(int x=0;x<WIDTH;x++)
        {
            for(int y=0;y<HEIGHT;y++)
            {
                if(data[x][y].color == playerid) number++;
            }
        }
        return number;
    }
}
