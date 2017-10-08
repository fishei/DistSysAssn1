package CommandLine;

import Models.TwitterCommand;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandLineParser implements ICommandLineParser
{
    @Override
    public void listenForCommands(ConcurrentLinkedQueue<TwitterCommand> queue)
    {
        new Thread(new CommandListenerRunnable(queue)).start();
    }
}
