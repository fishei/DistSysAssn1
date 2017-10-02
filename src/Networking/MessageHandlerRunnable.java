package Networking;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import Models.TwitterMessage;
import Models.TwitterEvent;

public class MessageHandlerRunnable implements Runnable
{
    private ConcurrentLinkedQueue<TwitterMessage> queue;
    private Socket clientSocket;

    public MessageHandlerRunnable(ConcurrentLinkedQueue<TwitterMessage> queue, Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        this.queue = queue;
    }

    private void handleError(String message, Exception e)
    {
        System.out.println(message + e.getMessage());
        tryCloseSocket();
    }

    private void tryCloseSocket()
    {
        try
        {
            clientSocket.close();
        }
        catch(Exception e2)
        {
            System.out.println("Error closing socket: " + e2.getMessage());
        }
    }
    public void run() {
        InputStream iStream;
        ObjectInputStream oStream;
        try
        {
            iStream = clientSocket.getInputStream();
            oStream = new ObjectInputStream(iStream);
        }
        catch(Exception e)
        {
            handleError("Error getting object input stream: ",e);
            return;
        }
        try
        {
            while(iStream.available() > 0)
            {
                TwitterMessage message = (TwitterMessage) oStream.readObject();
                queue.add(message);
            }
        }
        catch(Exception e)
        {
            handleError("Error reading from input stream: ",e);
        }
        tryCloseSocket();
    }
}
