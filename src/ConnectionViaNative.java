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
public class ConnectionViaNative implements ConnectionInterface {
    
    ConnectionViaNativeBackend backend;
    ByteBuffer readbuf;
    ByteBuffer writebuf;
    
    ConnectionViaNative()
    {
        backend=new ConnectionViaNativeBackend();
        readbuf=ByteBuffer.allocate(2048);
        writebuf=ByteBuffer.allocate(2048);
    }

    @Override
    public Object CreateServer(int port)
    {
        int tmp=backend.CreateServer(port);
        if (tmp==0) return null; else return tmp;
    }

    @Override
    public Object GrabServerConnection(Object server)
    {
        int tmp=backend.GrabServerConnection((Integer)server);
        if (tmp==0) return null; else return tmp;
    }

    @Override
    public Object ConnectToServer(String ip, int port)
    {
        int tmp=backend.ConnectToServer(ip, port);
        if (tmp==0) return null; else return tmp;
    }

    @Override
    public void Close(Object connection)
    {
        backend.Close((Integer)connection);
    }

    @Override
    public boolean IsOpen(Object connection)
    {
        return backend.IsOpen((Integer)connection);
    }

    @Override
    public int Read(Object connection, ByteBuffer buf)
    {
        readbuf.position(0).limit(readbuf.capacity());
        int tmpread = backend.Read((Integer)connection, readbuf.array());
        if (tmpread<0) tmpread=0;
        if (tmpread>0)
            {
            readbuf.limit(tmpread);
            buf.put(readbuf);
            }
        return tmpread;
    }

    @Override
    public int Write(Object connection, ByteBuffer buf)
    {
      int tmpwrite=backend.Write((Integer)connection, buf.array(),buf.remaining());
      if (tmpwrite<0) tmpwrite=0;
      if (tmpwrite>0)
      {
          buf.position(tmpwrite);
      }
      return tmpwrite;
    }
    
    public static int getSocket(Object obj)
    {
        return (Integer)obj;
    }
    public static Object setSocket(int a)
    {
        return (Object)new Integer(a);
    }
}
