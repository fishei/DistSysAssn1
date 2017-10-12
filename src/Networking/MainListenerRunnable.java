package Networking;

import Models.TwitterMessage;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainListenerRunnable implements Runnable
{
    private int port;
    private InetAddress serverAddr;
    private ConcurrentLinkedQueue<TwitterMessage> eventQueue;
    boolean continueRunning = true;

    public MainListenerRunnable(int port, ConcurrentLinkedQueue<TwitterMessage> eventQueue, InetAddress serverAddr)
    {
        this.port = port;
        this.eventQueue = eventQueue;
        this.serverAddr = serverAddr;
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
            serverSocket = new ServerSocket(port,0,serverAddr);
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
