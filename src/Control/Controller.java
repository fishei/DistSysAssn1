package Control;

import CommandLine.ICommandLineParser;
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
    private ICommandLineParser cmdParser;

    public Controller(INetworkingProvider networkingProvider, IFileManager fileManager, ICommandLineParser cmdParser)
    {
        this.cmdParser = cmdParser;
        this.networkingProvider = networkingProvider;
        this.siteState = new SiteState(fileManager);
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.userNames = new HashMap<>();
        for(User user : fileManager.loadUsers().values())
        {
            this.userNames.put(user.getUserName(), user);
        }
        this.userIds = new HashMap<Integer,User>(fileManager.loadUsers());
    }

    private void processTweet(String text)
    {
        Tweet tweet = new Tweet(
                 siteState.getUserId()
                ,siteState.getLocalClock() + 1
                ,text
                , DateTime.now(DateTimeZone.UTC)
        );
        siteState.onTwitterEvent(tweet);
        siteState.incrementLocalClock();
        siteState.saveToDisk();
        sendMessages();
    }

    private void processBlockCommand(BlockCommand b) {
        if(!userNames.containsKey(b.getUserName()))
        {
            System.out.println("Invalid username: " + b.getUserName());
            return;
        }
        BlockEvent e = new BlockEvent(
                 siteState.getUserId()
                ,siteState.getLocalClock() + 1
                ,userNames.get(b.getUserName()).getId()
                ,b.isBlockOrUnblock()
        );
        siteState.onTwitterEvent(e);
        siteState.incrementLocalClock();
        siteState.saveToDisk();
    }
    private void processViewCommand(ViewCommand v)
    {
        System.out.println(siteState.getTweets().size() + " tweets:");
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

    private void processTweetCommand(TweetCommand tweetCommand)
    {
        processTweet(tweetCommand.getText());
    }

    protected void processCommand(TwitterCommand command)
    {
        if(command instanceof TweetCommand)
        {
            processTweetCommand((TweetCommand) command);
        }
        else if (command instanceof BlockCommand)
        {
            processBlockCommand((BlockCommand) command);
        }
        else if (command instanceof ViewCommand)
        {
            processViewCommand((ViewCommand) command);
        }
        else
        {
            System.out.println("Invalid command");
        }
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
        networkingProvider.listenForMessages(messageQueue);
        cmdParser.listenForCommands(commandQueue);
        while(true)
        {
            if(!commandQueue.isEmpty())
            {
                processCommand(commandQueue.poll());
            }
            else if(!messageQueue.isEmpty())
            {
                processMessage(messageQueue.poll());
            }
        }
    }
}
