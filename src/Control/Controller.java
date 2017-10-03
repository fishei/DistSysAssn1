package Control;

import Networking.INetworkingProvider;
import Models.*;
import FileManagement.IFileManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


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

    private void processTweet(String text)
    {
        siteState.incrementLocalClock();
        Tweet tweet = new Tweet(
                 siteState.getUserId()
                ,siteState.getLocalClock()
                ,text
                , DateTime.now(DateTimeZone.UTC)
        );
        siteState.onTwitterEvent(tweet);
        sendMessages();
    }

    private void processBlockCommand(int userId, boolean block) {
        siteState.incrementLocalClock();
        BlockEvent e = new BlockEvent(
                 siteState.getUserId()
                ,siteState.getLocalClock()
                ,userId
                , block
        );
        siteState.onTwitterEvent(e);
    }

    private void viewTweets()
    {
        for(Tweet tweet : siteState.getTweets())
        {
            System.out.println(tweet.getText());
        }
    }

    private void sendMessages()
    {
        for(Map.Entry<Integer,TwitterMessage> entry : siteState.generateMessages().entrySet())
        {
            networkingProvider.sendMessage(entry.getValue(),entry.getKey());
        }
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
