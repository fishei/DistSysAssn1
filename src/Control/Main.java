package Control;

import CommandLine.CommandLineParser;
import CommandLine.ICommandLineParser;
import FileManagement.BasicFileManager;
import FileManagement.FileManager;
import FileManagement.IFileManager;
import Models.ISiteState;
import Networking.INetworkingProvider;
import Networking.NetworkProvider;

public class Main
{
    public static IFileManager buildFileManager(int i)
    {
        return new FileManager(i);
    }

    public static ICommandLineParser buildCommandLineParser()
    {
        return new CommandLineParser();
    }
    public static void main(String [] args)
    {
        ICommandLineParser parser = buildCommandLineParser();
        IFileManager fileManager = buildFileManager(Integer.parseInt(args[0]));
        INetworkingProvider networkingProvider = new NetworkProvider(fileManager);
        Controller controller = new Controller(networkingProvider,fileManager, parser);
        controller.run();
    }
}
