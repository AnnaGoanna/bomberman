/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.io.File;

/**
 *
 * @author Ania
 */
public class Paths {
    public final File ART_PATH;
    public final File MAPS_PATH;
    public final File POWERUPS_PATH;
   
    private File GlobalPath() throws Exception
    {
        return new File(Paths.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    }
    public Paths() throws Exception
    {
        File maindir=new File(System.getProperty("user.dir"));
        ART_PATH=new File(maindir,"art");
        MAPS_PATH=new File(maindir,"maps");
        POWERUPS_PATH=new File(ART_PATH,"powerups");
    }
}
