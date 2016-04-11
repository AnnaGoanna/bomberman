package bomberman;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import bomberman.Map.FieldType;
import java.awt.Font;
import java.io.File;
import java.util.Calendar;
import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
/**
 *
 * @author Ania
 */
public class GameWindow implements UIInterface {

    /**
     * @param args the command line arguments
     */
    
    /* constants */
    private static final int WINDOW_W = 500;
    private static final int WINDOW_H = 400;

    
    /* textures */
    private static Texture player;
    private static Texture floor;
    private static ResourceHandler rh;
    
    
    /* utils */
    private Map map;
    public GameController gc;
    private NetController nc;
    private Chat mychat;
    
    private TrueTypeFont timer_font;
    private int timer_font_size;
    
    public long timedelta;
    
    private long timeTillEnd = 0;
    
    
    /* coordinates */
    private int w; // width of one displayed tile (w==h)
    private int h; // height of one displayed tile (h==w)
    private int mh; //
    private int mw;
    
    Calendar timer_auto;
    
    public GameWindow(String[] args) throws Exception{
        try {
            Display.setDisplayMode(new DisplayMode(WINDOW_W, WINDOW_H));
            Display.setTitle("Bomberman");
            Display.create();
        } catch (LWJGLException ex) {
            ex.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
        initOpenGL();
        rh = new ResourceHandler();
        rh.loadAllTextures();
        initWorld(args);
    }
    
    private void initWorld(String[] args) throws Exception{
        mychat = new Chat(this);
        String loadmap="mapa1.txt";
        if (args.length>1) loadmap=args[1];
        Paths p=new Paths();
        map = new Map(new File(p.MAPS_PATH,loadmap).getAbsolutePath());//TODO:Move this to loading files, and add loading multiple maps
        String str="";
        if (args.length>0) str=args[0];
        
        gc = new GameController(map, str,mychat,this);
        nc=gc.nc;
    }
    
    public void updateWindow() throws Exception{
        if (Display.wasResized()) resizeWindow();
        initOpenGL();

        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
           
        timePasses(gc);    
            
        drawMap(map);
        
        /* writing */
        if((!mychat.is_writing)){
            if (Keyboard.isKeyDown(Keyboard.KEY_T))
                    {
                        mychat.userInput = "";
                        mychat.startWriting();
                    }
        }
        
        if(!mychat.is_writing)
            moveThePlayer(gc);
            
        for(int qid = 0; qid < 4; ++qid){
            if(gc.players[qid] != null){
                drawPlayer(qid);
            }
        }
        drawTime();

        drawChat();
        
        
        Display.update();
        Display.sync(100);
    }

    
    public void resizeWindow(){
        //starts=false;
        //glLoadIdentity();
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // we set it up more like flash with coordinates starting top left of our screen.
        glOrtho( 0, Display.getWidth(), Display.getHeight(), 0, 1, -1 );
        glViewport(0, 0, Display.getWidth(), Display.getHeight());

        w = (Display.getWidth())/(map.WIDTH);
        h = (Display.getHeight())/(map.HEIGHT);
        if (w>h) {w=h;}
        if (h>w) {h=w;}

        mh=(Display.getHeight()-(h*map.HEIGHT))/2;
        mw=(Display.getWidth()-(w*map.WIDTH))/2; 
    }
    
    public void closeWindow(){
        rh.releaseAllTextures();
        // close nicely
        Display.destroy();
        System.exit(0);
    }
    
    
    public void initOpenGL(){ 
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    
    
    private void timePasses(GameController gc) throws Exception{
        timedelta=System.currentTimeMillis()-timedelta;
        long timetmp= System.currentTimeMillis();
        
        //System.out.println(timedelta);
        mychat.tick(timedelta);
        gc.tick(timedelta);
        timedelta=timetmp;
    }
    
    
    private void drawField(Texture texture, int x, int y, int w, int h){
        
        if(texture != null){
            glColor3f(1.0f,1.0f,1.0f);
            glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
        }    
        else //glColor3f(1.0f,1.0f,0.0f); // yellow
            //glColor3f(0.0f,0.0f,0.0f); // black
            glColor3f(1.0f,1.0f,1.0f); // white
        
        glBegin(GL11.GL_QUADS);
                glTexCoord2f(0, 0); // top left
                glVertex2f(x,y);

                glTexCoord2f(0, 1); // bottom left 
                glVertex2f(x, y + h);

                glTexCoord2f(1, 1); // bottom right
                glVertex2f(x + w,y + h);

                glTexCoord2f(1, 0); // top right
                glVertex2f(x + w, y);
        glEnd();
            
        glBindTexture(GL_TEXTURE_2D, 0);
        TextureImpl.unbind();
    }
    
    private void drawMap(Map map){
        for (int j = 0; j < map.HEIGHT; j++)
            for (int i = 0; i < map.WIDTH; i++)
            {
                //drawField(yellowtile, (i*w)+mw, (j*h)+mh, w, h);
            
                FieldType typ = map.data[i][j].type;
                byte subtyp = map.data[i][j].subtype;
                byte color = map.data[i][j].color;
                floor = rh.loadFloor(color);
                
                switch(typ){
                    case Nothing: drawField(floor, (i*w)+mw, (j*h)+mh, w, h);
                        break;
                    case Wall: drawField(rh.wall, (i*w)+mw, (j*h)+mh, w, h);
                        break;
                    case Destructible_Wall: 
                        if(subtyp == 1) drawField(rh.dark_wall, (i*w)+mw, (j*h)+mh, w, h);
                        else if(subtyp == 2) drawField(rh.light_wall, (i*w)+mw, (j*h)+mh, w, h);
                        else assert(false);
                        break;
                    case Bomb: drawField(floor, (i*w)+mw, (j*h)+mh, w, h);
                        drawField(rh.bomb, (i*w)+mw, (j*h)+mh, w, h);
                        break;
                    case Explosion: drawField(floor, (i*w)+mw, (j*h)+mh, w, h);
                        drawField(rh.expl, (i*w)+mw, (j*h)+mh, w, h);
                        break;
                    case Powerup: drawField(floor, (i*w)+mw, (j*h)+mh, w, h);
                        switch(subtyp){
                            case 0: drawField(rh.more, (i*w)+mw, (j*h)+mh, w, h);
                                break;
                            case 1: drawField(rh.power, (i*w)+mw, (j*h)+mh, w, h);
                                break;
                            case 2: drawField(rh.foot, (i*w)+mw, (j*h)+mh, w, h);
                                break;
                            case 3: drawField(rh.god, (i*w)+mw, (j*h)+mh, w, h);
                                break;
                            case 4: drawField(rh.fix, (i*w)+mw, (j*h)+mh, w, h);
                                break;
                            case 5: drawField(rh.hand, (i*w)+mw, (j*h)+mh, w, h);
                                break;
                            default: assert(false);
                                break;
                        }
                        //assert(false); // TODO
                        break;
                    default: assert(false);
                        break;
                }
            }
    }
    
    /* _timeTillEnd given in seconds */
    @Override
    public void updateTimer(long _timeTillEnd){
        timeTillEnd = _timeTillEnd;
        timer_auto=null;
    }
    @Override
    public void updateTimerAutoDec(long t)
    {
        timer_auto=Calendar.getInstance();
        timer_auto.add(Calendar.SECOND, (int) t);
    }
    
    private void manageFont(int newfontsize)
    {
        if (timer_font_size==newfontsize) return;
        timer_font_size=newfontsize;
        Font awtFont = new Font("New Times Roman", Font.BOLD, newfontsize);
        timer_font = new TrueTypeFont(awtFont, false);
    }
    
    private void drawTime(){
        
        int timeBlockWidth = 3*w/2;
        int timeBlockHeight = 2*h/3;
        int posx = (map.WIDTH*w/2-timeBlockWidth/2)+mw;
        int posy = ((h-timeBlockHeight)/2)+mh;
        //drawField(null, posx, posy, timeBlockWidth, timeBlockHeight);
        
        manageFont(timeBlockHeight);
        
        int sec = 0;
        int min = 0;
        if (timer_auto!=null)
        {
            Calendar t=Calendar.getInstance();
            int tmp=(int) ((timer_auto.getTimeInMillis()-t.getTimeInMillis())/1000);
            if (tmp>0) timeTillEnd=tmp; else timeTillEnd=0;
        }
        long tmpTime = timeTillEnd;
        min=(int)tmpTime / 60;
        sec=(int)tmpTime % 60;
        
        timer_font.drawString(posx, posy, String.format("%02d:%02d", min, sec), Color.yellow);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        TextureImpl.unbind();
    }
        
    private void moveThePlayer(GameController gc){
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)
                || Keyboard.isKeyDown(Keyboard.KEY_J)){
            gc.move_from_keyboard(0);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W)
                || Keyboard.isKeyDown(Keyboard.KEY_I)){
            gc.move_from_keyboard(1);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)
                || Keyboard.isKeyDown(Keyboard.KEY_L)){
            gc.move_from_keyboard(2);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S)
                || Keyboard.isKeyDown(Keyboard.KEY_K)){
            gc.move_from_keyboard(3);
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) //|| Keyboard.isKeyDown(Keyboard.KEY_RETURN)
                || Keyboard.isKeyDown(Keyboard.KEY_E) || Keyboard.isKeyDown(Keyboard.KEY_O)){
            gc.move_from_keyboard(5);
        }
        if(mychat.send == true){
            boolean enter_pressed = false;
            if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)) enter_pressed = true;
            else{
                enter_pressed = false;
                mychat.send = false;
            }
        }
        else if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)){
            gc.move_from_keyboard(5);
        }
    }
    
    private void drawPlayer(int pid){
        int x = gc.players[pid].oldx*w;
        int y = gc.players[pid].oldy*h;
        if(gc.players[pid].moves){
            x = gc.players[pid].oldx*w + 
                    (int)((gc.players[pid].newx*w-gc.players[pid].oldx*w)*gc.players[pid].moveprog);
            y = gc.players[pid].oldy*h + 
                    (int)((gc.players[pid].newy*h-gc.players[pid].oldy*h)*gc.players[pid].moveprog);
        }
        player = rh.loadPlayer(pid);
        drawField(player, x+mw, y+mh, w, h);
    }

    
    private void drawChat(){
        // int fontSize=12; // constant fontsize
        int fontSize=2*h/3; // fontsize resizes with window
        int chatPosX=0;
        int chatPosY= (map.HEIGHT)*h-1 - fontSize;
        //mychat.changeFontSize(fontSize);
        mychat.displayMessages(gc, chatPosX, chatPosY,fontSize);
    }
    
    @Override
    public void updateMap(Map _map) {
        map = _map;
    }
}
