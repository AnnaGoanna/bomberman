/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.TextureImpl;

/**
 *
 * @author Ania
 */
public class Chat {
    private class Message {
        private String text;
        private float duration;
        Message(String text){
            this.text = text;
            this.duration = 10000;//10sec
        }
    }
    public List<Message> messages;
    public static final int maxMessages = 5;
    
    protected boolean is_writing = false;
    protected boolean send = false;
    protected String userInput = "";
    private GameWindow gw;
    
    private int fontSize = 12;//2*h/3;
    private TrueTypeFont font = new TrueTypeFont(new Font("Times New Roman", Font.PLAIN, fontSize), false);
    
    Chat(GameWindow _gw){
        gw=_gw;
        messages = new ArrayList<Message>();
    }
    
    public void changeFontSize(int _fontSize)
    {
        if (_fontSize==fontSize) return;
        fontSize=_fontSize;
        System.out.println("changing font");
        Font awtFont2 = new Font("Times New Roman", Font.PLAIN, fontSize);
        font = new TrueTypeFont(awtFont2, false);
    }
    
    public void addMessageToChat(String chat)
    {
        if(messages.size()>=maxMessages) messages.remove(messages.size()-1); // remove oldest message
        messages.add(0,new Message(chat)); // add new message at the beginning
    }
    
    public void startWriting()
    {
        is_writing=true;
        while (Keyboard.next()) {Keyboard.getEventKey();} // clear buffer
        userInput="";
    }
    
    public void endWriting()//(boolean send)
    {
        is_writing=false;
        if (send)
        {
            gw.gc.ClientMessage(userInput);
            userInput="";
        }
    }
    
    private boolean shift;
    private boolean jtLetter;
    public void tick(float time_passed){
        if (is_writing)
        {
            while (Keyboard.next()) {
            
                if((Keyboard.getEventKey()==Keyboard.KEY_UP)||(Keyboard.getEventKey()==Keyboard.KEY_DOWN)||
                        (Keyboard.getEventKey()==Keyboard.KEY_LEFT)||(Keyboard.getEventKey()==Keyboard.KEY_RIGHT))
                    continue;
                if((Keyboard.getEventKey()==Keyboard.KEY_LSHIFT)||(Keyboard.getEventKey()==Keyboard.KEY_RSHIFT)){
                    shift=Keyboard.getEventKeyState();
                } else if (Keyboard.getEventKey()==Keyboard.KEY_RETURN) {
                    send=true;
                    endWriting();//(true);
                } else if (Keyboard.getEventKey()==Keyboard.KEY_DELETE) {
                    userInput="";
                } else if (Keyboard.getEventKey()==Keyboard.KEY_ESCAPE) {
                    send=false;
                    endWriting();//(false);
                } else if (Keyboard.getEventKey()==(Keyboard.KEY_BACK)) {
                    try {
                        if (Keyboard.getEventKeyState()) userInput = userInput.substring(0, userInput.length() - 1);
                    } catch (StringIndexOutOfBoundsException e) {}
                } else if (Keyboard.getEventKeyState() && (!jtLetter)) {
                    System.out.println("KEY: "+Keyboard.getEventCharacter());
                    if (shift) {
                        userInput += Character.toUpperCase(Keyboard.getEventCharacter());
                    } else {
                        userInput += String.valueOf(Keyboard.getEventCharacter());
                        jtLetter = true;
                    }
                } else jtLetter=false;
            }
        }
        if(!messages.isEmpty())
            //for (Message message : messages){
            for(int i = messages.size()-1; i >= 0; i--){
                Message message = messages.get(i);
                message.duration-=time_passed;
                //if(message.duration<=0) messages.remove(message); // update chat
                if(message.duration<=0) messages.remove(i); // update chat
            }
    }
    
    public void displayMessages(GameController gc, int x, int y,int fontSize){
        if (is_writing)
        {
           changeFontSize(fontSize);
           font.drawString(x, y, userInput,Color.cyan);
        }
        
        if(!messages.isEmpty())
        {
            changeFontSize(fontSize);
            //for (Message message : messages){
            for(int i = 0; i < messages.size(); i++){
                y-=(fontSize-2);
                Message message = messages.get(i);

                font.drawString(x, y, message.text, Color.yellow); // display chat
                
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                TextureImpl.unbind();
            }
        }
    }
   
}
