/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Payl
 */
public class fetchfromservice {
    static String GetIPFromService()
    {
    URL url;
    InputStream is = null;
    BufferedReader br;
    String line;
    String ip=null;

    try {
        url = new URL("http://5.196.10.150/bomberman/get.php");
        is = url.openStream();  // throws an IOException
        br = new BufferedReader(new InputStreamReader(is));
        
        ip=br.readLine();
    } catch (MalformedURLException mue) {
         mue.printStackTrace();
    } catch (IOException ioe) {
         ioe.printStackTrace();
    } finally {
        try {
            if (is != null) is.close();
        } catch (IOException ioe) {
            // nothing to see here
        }
    }
    return ip;
    }
    
}
