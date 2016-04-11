/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Payl
 */
public class Bomberman {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        try {
            GameWindow g = new GameWindow(args);
            
            /* set windowsize */
            Display.setResizable(true);
            g.resizeWindow();
            
            /* set time */
            g.timedelta=System.currentTimeMillis();
            
            /* update window until user closes */
            while (!Display.isCloseRequested()){// && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                g.updateWindow();
            }
            
            g.closeWindow();
        } catch (Exception ex) {
            Logger.getLogger(Bomberman.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}