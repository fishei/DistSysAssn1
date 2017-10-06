package Control;

import CommandLine.ICommandLineParser;
import FileManagement.IFileManager;
import Models.ISiteState;
import Networking.INetworkingProvider;
import Networking.NetworkProvider;

public class Main
{
    public static IFileManager buildFileManager()
    {
        return null;
    }

    public static ICommandLineParser buildCommandLineParser()
    {
        return null;
    }
    public static void main(String [] args)
    {
        ICommandLineParser parser = buildCommandLineParser();
        IFileManager fileManager = buildFileManager();
        INetworkingProvider networkingProvider = new NetworkProvider(fileManager);
        Controller controller = new Controller(networkingProvider,fileManager);
        controller.run();
    }
}
