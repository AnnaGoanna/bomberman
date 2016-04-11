/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.io.File;
import java.io.FileInputStream;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/**
 *
 * @author Ania
 */
public class ResourceHandler {
    
    /* textures */
    public static Texture tile;
    public static Texture redtile;
    public static Texture bluetile;
    public static Texture greentile;
    public static Texture yellowtile;
        
    public static Texture wall;
    public static Texture dark_wall;
    public static Texture light_wall;
    
    public static Texture bomb;
    public static Texture expl;
    
    public static Texture more;
    public static Texture power;
    public static Texture foot;
    public static Texture hand;
    public static Texture fix;
    public static Texture god;
      
    private static Texture redplayer;
    private static Texture blueplayer;
    private static Texture greenplayer;
    private static Texture yellowplayer;
        
    
    public Texture loadPlayer(int pid){
        switch(pid){
            case 0: return redplayer;
            case 1: return blueplayer;
            case 2: return greenplayer;
            case 3: return yellowplayer;
            default: assert(false);
        }
        return null;
    }
    public Texture loadFloor(byte color){
        switch(color){
            case -1: return tile;
            case 0: return redtile;
            case 1: return bluetile;
            case 2: return greentile;
            case 3: return yellowtile;
            default: assert(false);
        }
        return null;
    }
    
    private Texture loadTexture(File dir,String filepath, String format) throws Exception{
        Texture texture = null;
        texture = TextureLoader.getTexture(format, new FileInputStream(new File(dir,filepath)));
        return texture;
    }
    
    public void loadAllTextures() throws Exception{
        Paths p = new Paths();
        tile = loadTexture(p.ART_PATH,"tile.bmp","BMP");
        redtile = loadTexture(p.ART_PATH,"tile_red.bmp","BMP");
        bluetile = loadTexture(p.ART_PATH,"tile_blue.bmp","BMP");
        greentile = loadTexture(p.ART_PATH,"tile_green.bmp","BMP");
        yellowtile = loadTexture(p.ART_PATH,"tile_yellow.bmp","BMP");
        
        wall = loadTexture(p.ART_PATH,"wall.bmp","BMP");
        dark_wall = loadTexture(p.ART_PATH,"dark_wall.bmp","BMP");
        light_wall = loadTexture(p.ART_PATH,"light_wall.bmp","BMP");
        
        bomb = loadTexture(p.ART_PATH,"bomb1.png","PNG");
        expl = loadTexture(p.ART_PATH,"explosion.png","PNG");
        
        more = loadTexture(p.POWERUPS_PATH,"more.png","PNG");
        power = loadTexture(p.POWERUPS_PATH,"power.png","PNG");
        foot = loadTexture(p.POWERUPS_PATH,"foot.png","PNG");
        hand = loadTexture(p.POWERUPS_PATH,"hand.png","PNG");
        fix =loadTexture(p.POWERUPS_PATH,"fix.png","PNG");
        god =loadTexture(p.POWERUPS_PATH,"god.png","PNG");
        
        redplayer = loadTexture(p.ART_PATH,"redplayer.png","PNG");
        blueplayer = loadTexture(p.ART_PATH,"blueplayer.png","PNG");
        greenplayer = loadTexture(p.ART_PATH,"greenplayer.png","PNG");
        yellowplayer = loadTexture(p.ART_PATH,"yellowplayer.png","PNG");
    }
    
    public void releaseAllTextures(){
       tile.release();
        redtile.release();
        bluetile.release();
        greentile.release();
        yellowtile.release();
        
        wall.release();
        dark_wall.release();
        light_wall.release();
        
        bomb.release();
        expl.release();
        
        more.release();
        power.release();
        foot.release();
        hand.release();
        fix.release();
        god.release();
        
        redplayer.release();
        blueplayer.release();
        greenplayer.release();
        yellowplayer.release();
    }
    
}
