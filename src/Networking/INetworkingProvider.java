package Networking;

import Models.TwitterMessage;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface INetworkingProvider
{
    void sendMessage(TwitterMessage msg, int dest);

    void listenForMessages(ConcurrentLinkedQueue<TwitterMessage> queue);
}
