/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bomberman;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Payl
 */
public class ConnectionViaUtil implements ConnectionInterface{

    @Override
    public Object CreateServer(int port) {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.configureBlocking(false);
            return ssc;
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public Object GrabServerConnection(Object server) {
        try {
            ServerSocketChannel s=(ServerSocketChannel)server;
            SocketChannel sc=s.accept();
            if (sc!=null) sc.configureBlocking(false);
            return sc;
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public Object ConnectToServer(String ip, int port) {
        try {
            SocketChannel sc=SocketChannel.open(new InetSocketAddress(InetAddress.getByName(ip),port));
            sc.configureBlocking(false);
            return sc;
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void Close(Object connection) {
        try {
            ((Closeable)connection).close();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionViaUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean IsOpen(Object connection) {
        return ((Channel)connection).isOpen();
    }

    @Override
    public int Read(Object connection,ByteBuffer buf) {
        try {
            return ((SocketChannel)connection).read(buf);
        } catch (IOException ex) {
            Close(connection);
            return 0;
            
        }
    }

    @Override
    public int Write(Object connection, ByteBuffer buf) {
        try {
            return ((SocketChannel)connection).write(buf);
        } catch (IOException ex) {
            Close(connection);
            return 0;
        }
    }
    
    
}
