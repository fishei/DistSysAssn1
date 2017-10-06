package CommandLine;

import Models.TwitterCommand;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface ICommandLineParser
{
    void listenForCommands(ConcurrentLinkedQueue<TwitterCommand> queue);
}
