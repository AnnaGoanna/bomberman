/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.util.Calendar;

/**
 *
 * @author Payl
 */
public class RoundController {
    private GameController gc;
    private NetController nc;
    public int time_between_rounds=5;//10;
    public int time_for_round=120;//60;
    private Calendar roundend;
    private boolean inround;
    private int minimum_players = 2;
    RoundController(GameController _gc)
    {
        gc=_gc;
        nc=gc.nc;
        if (gc.is_server) EndRound();
        inround=false;
    }
    
    void setTime(long t)
    {
        gc.ui.updateTimer(t);
        
    }
    long queryTime(boolean without_minus)
    {
        Calendar t=Calendar.getInstance();
        long sec_between=(roundend.getTimeInMillis()-t.getTimeInMillis())/1000;
        if (without_minus && (sec_between<0)) sec_between=-sec_between;
        return sec_between;
    }
    void tick()
    {
        
        long sec_between=queryTime(false);
        if (inround)
        {
            if(countPlayers() < minimum_players)
                {EndRound();setTime(0);}
            else{
                if(sec_between<=0) 
                    {EndRound();setTime(0);}
                else setTime(sec_between);
            }
        } else
        {
            if(sec_between<=0) {
                if(nc.countClients() >= minimum_players) NewRound();
            }
            if (sec_between>0)
            {
                setTime(sec_between);
            }
        }
    }
    
    void NewRound()
    {
        System.out.println("NewRound");
        if (gc.is_server) gc.ClientMessage("New Round Starts!");
        inround=true;
        gc.reloadmap();
        roundend=Calendar.getInstance();
        roundend.add(Calendar.SECOND, time_for_round);
        gc.nc.UpdateTimer(-1,time_for_round);
        for(int i=0;i<4;i++)
        {
            if (!nc.IsConnected((byte)i)) continue;
            gc.playerdeath(i,false);
            gc.newplayer(i);
            //gc.players[i].needsupdate=true;
            gc.nc.SetClientNeedsupdate(i, true);
        }
    }
    
    void EndRound()
    {
        System.out.println("EndRound");
        gc.ClientMessage("End Of Round!");
        displayResults();
        inround=false;
        roundend=Calendar.getInstance();
        roundend.add(Calendar.SECOND, time_between_rounds);
        gc.nc.UpdateTimer(-1,time_between_rounds);
        for(int i=0;i<4;i++)
        {
            if (!nc.IsConnected((byte)i)) continue;
            gc.playerdeath(i,false);
        }
    }
    
    int countPlayers()
    {
        int number = 0;
        for(int i=0;i<4;i++)
        {
            if (gc.players[i]!=null) number++;
        }
        return number;
    }
    
    int getPlayerPoints(int playerid)
    {
        if(gc.players[playerid] == null) return 0;
        return gc.map.countPlayerTiles(playerid);
    }
    
    int whoWins()
    {
        int winner = 0;
        int max_points = getPlayerPoints(0);
        for(int i = 1; i<4; i++)
        {
            int next_points = getPlayerPoints(i);
            if(next_points > max_points) 
            {
                max_points=next_points; 
                winner=i;
            }
            else if(next_points == max_points) 
                winner = -1;
        }
        return winner;
    }
    
    void displayResults()
    {
        int winner = whoWins();
        if(winner != -1) gc.ClientMessage("End of round. Player "+winner+" won!");
        else{
            gc.ClientMessage("End of round. Draw!");
        }
        int points[][] = new int[4][2];
        for(int i=0; i<4; i++)
        {
            points[i][0] = i;
            points[i][1] = getPlayerPoints(i);
        }

        for(int j=4-1; j>0; j--)
        {
            boolean p = true;
            for(int i = 0; i < j; i++)
                if(points[i][1] < points[i+1][1])
                {
                    int tmp0 = points[i][0];
                    int tmp1 = points[i][1];
                    points[i][0] = points[i+1][0];
                    points[i][1] = points[i+1][1];
                    points[i+1][0] = tmp0;
                    points[i+1][1] = tmp1;
                    p = false;
                }
            if(p) break;
        }

        int place = 1;
        for(int i=0; i<4; i++)
        {
            if(points[i][1] != 0){
                if(i!=0 && (points[i-1][1] > points[i][1])) place=i+1;
                gc.ClientMessage(place+". player "+points[i][0]+" ["+points[i][1]+"pts]");
            }
        }
    }
}
