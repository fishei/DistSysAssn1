package Networking;

import Models.TwitterMessage;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainListenerRunnable implements Runnable
{
    private int port;
    private ConcurrentLinkedQueue<TwitterMessage> eventQueue;
    boolean continueRunning = true;

    public MainListenerRunnable(int port, ConcurrentLinkedQueue<TwitterMessage> eventQueue)
    {
        this.port = port;
        this.eventQueue = eventQueue;
    }

    private void tryCloseSocket(ServerSocket serverSocket)
    {
        try
        {
            serverSocket.close();
        }
        catch(Exception e2)
        {
            System.out.println("Error closing socket: " + e2.getMessage());
        }
    }

    public void run()
    {
        ServerSocket serverSocket;
        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch(Exception e)
        {
            System.out.println("Error listening on port " + port + ": " + e.getMessage());
            return;
        }
        while(continueRunning)
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                new Thread(new MessageHandlerRunnable(eventQueue, clientSocket)).start();
            }
            catch(Exception e)
            {
                System.out.println("Error accepting connection: " + e.getMessage());
                tryCloseSocket(serverSocket);
                return;
            }
        }
    }
}
