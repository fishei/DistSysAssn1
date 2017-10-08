package CommandLine;

import Models.BlockCommand;
import Models.TweetCommand;
import Models.TwitterCommand;
import Models.ViewCommand;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandListenerRunnable implements Runnable
{
    private ConcurrentLinkedQueue<TwitterCommand> commandQueue;

    public CommandListenerRunnable(ConcurrentLinkedQueue<TwitterCommand> commandQueue)
    {
        this.commandQueue = commandQueue;
    }

    private void processTweet(String[] commandStrings, String commandString)
    {
        String tweetText = commandString.substring(6);
        commandQueue.add(new TweetCommand(tweetText));
    }

    private void processBlock(String[] commandStrings, String commandString)
    {
        if(commandStrings.length != 2)
        {
            System.out.println("Usage: block <username>");
        }
        else
        {
            commandQueue.add(new BlockCommand(commandStrings[1],true));
        }
    }

    private void processUnblock(String[] commandStrings, String commandString)
    {
        if(commandStrings.length != 2)
        {
            System.out.println("Usage: block <username>");
        }
        else
        {
            commandQueue.add(new BlockCommand(commandStrings[1],false));
        }
    }

    private void processView(String[] commandStrings, String commandString)
    {
        if(commandStrings.length != 1)
        {
            System.out.println("Usage: view");
        }
        else
        {
            commandQueue.add(new ViewCommand());
        }
    }

    private void processCommand(String commandString)
    {
        String[] subStrings = commandString.split(" ");
        switch(subStrings[0].toLowerCase())
        {
            case "tweet":
                processTweet(subStrings, commandString);
                break;
            case "block":
                processBlock(subStrings, commandString);
                break;
            case "unblock":
                processUnblock(subStrings, commandString);
                break;
            case "view":
                processView(subStrings, commandString);
                break;
            default:
                invalidCommand(subStrings[0]);
                break;
        }
    }

    private void invalidCommand(String commandString)
    {
        System.out.println("Invalid command: " + commandString);
    }

    @Override
    public void run()
    {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            if(sc.hasNextLine())
            {
                processCommand(sc.nextLine());
            }
        }
    }
}
