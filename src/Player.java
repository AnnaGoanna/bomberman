/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

/**
 *
 * @author Payl
 */
public class Player {
    public int oldx,oldy; //defines old coords player was in
    public int newx,newy;
    public float moveprog; //defines how far player has moved to end of newx
    private final Map map;
    public boolean moves;
    //public boolean needsupdate;
    public int lastdir;
    
    public int bombrange;
    public double godmodelen;
    public int bombcntmax;
    public double speedupdatelen;
    
    Player(Map onmap,int x,int y)
    {
        moves=false;
        map=onmap;
        Warp(x,y);
        //needsupdate=true;
        bombrange=2;
        bombcntmax=1;
        godmodelen=0.0;
        speedupdatelen=0.0;
        lastdir=-1;
    }
    
    void tick(float time_delta)
    {
        godmodelen-=time_delta;
        if (godmodelen<0.0)
        {
            godmodelen=0.0;
        }
        speedupdatelen-=time_delta;
        if (speedupdatelen<0.0)
        {
            speedupdatelen=0.0;
        } else time_delta=time_delta * 2;
        moveprog+=time_delta/250;
        if (moveprog>=1)
        {
            lastdir=-1;
            moves=false;
            oldx=newx;
            oldy=newy;
        }
    }
    
    void Warp(int x,int y)//Teleports player to given coords
    {
        oldx=x;
        oldy=y;
        newx=x;
        newy=y;
        moves=false;
        moveprog=0;
        lastdir=-1;
    }

    void startmove(int dir) {
        lastdir=dir;
        newx=Map.GetDirX(oldx, dir);
        newy=Map.GetDirY(oldy, dir);
        moveprog=0;
        moves=true;
    }
    
            
}
