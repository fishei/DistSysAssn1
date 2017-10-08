package Control;

import Networking.INetworkingProvider;
import Models.*;
import FileManagement.IFileManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Controller
{
    private INetworkingProvider networkingProvider;
    private ISiteState siteState;
    private ConcurrentLinkedQueue<TwitterMessage> messageQueue;
    private ConcurrentLinkedQueue<TwitterCommand> commandQueue;
    private HashMap<String, User> userNames;
    private HashMap<Integer, User> userIds;

    public Controller(INetworkingProvider networkingProvider, IFileManager fileManager)
    {
        this.networkingProvider = networkingProvider;
        this.siteState = new SiteState(fileManager);
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.commandQueue = new ConcurrentLinkedQueue<>();
        networkingProvider.listenForMessages(messageQueue);
        this.userNames = new HashMap<>();
        for(User user : fileManager.loadUsers().values())
        {
            this.userNames.put(user.getUserName(), user);
        }
        this.userIds = new HashMap<Integer,User>(fileManager.loadUsers());
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
        siteState.saveToDisk();
        sendMessages();
    }

    private void processCommand(BlockCommand b) {
        if(!userNames.containsKey(b.getUserName()))
        {
            System.out.println("Invalid username: " + b.getUserName());
            return;
        }
        siteState.incrementLocalClock();
        BlockEvent e = new BlockEvent(
                 siteState.getUserId()
                ,siteState.getLocalClock()
                ,userNames.get(b.getUserName()).getId()
                ,b.isBlockOrUnblock()
        );
        siteState.onTwitterEvent(e);
        siteState.saveToDisk();
    }
    private void processCommand(ViewCommand v)
    {
        viewTweets();
    }

    private String tweetToString(Tweet tweet)
    {
        String userName = userIds.get(tweet.getOriginatorId()).getUserName();
        String timeStampString = tweet.getUtcTimeStamp().toString("dd/MM/yy hh:mm:ss");
        return userName + " at " + timeStampString + ": " + tweet.getText();
    }
    private void viewTweets()
    {
        for(Tweet tweet : siteState.getTweets())
        {
            System.out.println(tweetToString(tweet));
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
        System.out.println("Invalid command");
    }

    protected void processMessage(TwitterMessage message)
    {
        for(TwitterEvent e : message.eventList)
        {
            siteState.onTwitterEvent(e);
        }
        siteState.updateClocks(message.originatorId, message.siteClocks);
        siteState.saveToDisk();
    }

    public void run()
    {
        while(true)
        {
            if(commandQueue.isEmpty())
            {
                processMessage(messageQueue.poll());
            }
            else if(!messageQueue.isEmpty())
            {
                processCommand(commandQueue.poll());
            }
        }
    }
}
