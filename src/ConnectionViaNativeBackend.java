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
public class ConnectionViaNativeBackend {
    static{
    String archDataModel = System.getProperty("sun.arch.data.model");
System.loadLibrary("bombermanconnection"+archDataModel);
    }
    public native int CreateServer(int port);
    public native int GrabServerConnection(int server);
    public native int ConnectToServer(String ip, int port);
    public native void Close(int connection);
    public native boolean IsOpen(int connection);
    public native int Read(int connection,byte[] data);
    public native int Write(int connection, byte[] buf, int len);
}
