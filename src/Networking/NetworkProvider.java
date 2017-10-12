package Networking;

import FileManagement.IFileManager;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import Models.TwitterMessage;

public class NetworkProvider implements INetworkingProvider
{
    public static final int serverPort = 7777;
    public static final int clientPort = 7778;

    private InetAddress serverAddr;
    private HashMap<Integer, InetAddress> addresses;

    public NetworkProvider(IFileManager fileManager)
    {
        this.addresses = new HashMap<>(fileManager.loadAddresses());
        this.serverAddr = addresses.get(fileManager.loadCurrentUser().getId());
    }

    public void listenForMessages(ConcurrentLinkedQueue<TwitterMessage> queue)
    {
        System.out.println("listening on " + serverAddr.toString());
        new Thread(new MainListenerRunnable(serverPort, queue,serverAddr)).start();
    }

    public void sendMessage(TwitterMessage msg, int dest)
    {
        try
        {
            Socket socket = new Socket( addresses.get(dest), serverPort);
            ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
            oStream.writeObject(msg);
            oStream.close();
            socket.close();
        }
        catch(Exception e)
        {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }
}
