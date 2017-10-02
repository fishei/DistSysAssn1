package Control;

import Networking.INetworkingProvider;
import Models.*;
import FileManagement.IFileManager;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Controller
{
    private INetworkingProvider networkingProvider;
    private ISiteState siteState;
    private ConcurrentLinkedQueue<TwitterMessage> messageQueue;
    private ConcurrentLinkedQueue<TwitterCommand> commandQueue;

    public Controller(INetworkingProvider networkingProvider, IFileManager fileManager)
    {
        this.networkingProvider = networkingProvider;
        this.siteState = new SiteState(fileManager);
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.commandQueue = new ConcurrentLinkedQueue<>();
        networkingProvider.listenForMessages(messageQueue);
    }

    protected void processCommand(TwitterCommand command)
    {

    }

    protected void processMessage(TwitterMessage message)
    {
        for(TwitterEvent e : message.eventList)
        {
            siteState.onTwitterEvent(e);
        }
        siteState.updateClocks(message.originatorId, message.siteClocks);
    }

    public void run()
    {
        while(true)
        {
            if(commandQueue.isEmpty())
            {
                processMessage(messageQueue.poll());
            }
            else
            {
                processCommand(commandQueue.poll());
            }
        }
    }
}
