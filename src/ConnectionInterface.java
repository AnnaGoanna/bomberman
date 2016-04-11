/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.nio.ByteBuffer;

/**
 *
 * @author Payl
 */
public interface ConnectionInterface {
    Object CreateServer(int port);
    Object GrabServerConnection(Object server);
    Object ConnectToServer(String ip,int port);
    void Close(Object connection);
    boolean IsOpen(Object connection);
    int Read(Object connection,ByteBuffer buf);
    int Write(Object connection, ByteBuffer buf);
}
