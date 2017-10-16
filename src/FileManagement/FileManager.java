package FileManagement;

import Models.BlockEvent;
import Models.Tweet;
import Models.TwitterEvent;
import Models.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

/*
    partial IFileManager implementation that loads an initial state every time
    this means that BasicFileManager treats every startup like an initial startup
    (as opposed to a recovery from a site crash)
*/
public class FileManager implements IFileManager
{
    public static final String tweetsFile = "tweets.txt";
    public static final String userFile = "users.txt";
    public static final String clockFile = "clocks.txt";
    public static final String partialLogFile = "partialLog.txt";
    public static final String blockListFile = "blockList.txt";
    public static final String dateFormat = "DDDhhmmss";

    private int userId;
    private String dataDirectory;

    private HashMap<Integer, User> userMap;
    private HashMap<Integer, InetAddress> addressMap;

    public FileManager(int userId, String dataDirectory)
    {
        this.userId = userId;
        this.dataDirectory = dataDirectory;
        this.userMap = null;
        this.addressMap = null;
    }

    private BufferedWriter getBufferedWriter(String fileName, boolean append)
    {
        try{
            FileWriter fileWriter = new FileWriter(dataDirectory + "/" + fileName, append);
            return new BufferedWriter(fileWriter);
        }
        catch(IOException e)
        {
            System.out.println("Error opening " + fileName + " for writing");
            return null;
        }
    }
    private BufferedReader getBufferedReader(String fileName)
    {
        try
        {
            FileReader fileReader = new FileReader(dataDirectory + "/" + fileName);
            return new BufferedReader(fileReader);
        }
        catch(FileNotFoundException fileNotFoundException)
        {
            return null;
        }
    }

    private void ioError(String fileName)
    {
        System.out.println("Error reading " + fileName);
    }

    public Collection<Tweet> loadTweets()
    {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>();
        BufferedReader bufferedReader = getBufferedReader(tweetsFile);
        if(bufferedReader == null)
        {
            return tweets;
        }
        String line;
        try{
            while((line = bufferedReader.readLine()) != null) {
                tweets.add(parseTweet(line));
            }
            return tweets;
        }
        catch(IOException ioException)
        {
            ioError(tweetsFile);
            return null;
        }
    }

    private Tweet parseTweet(String tweetFileLine)
    {
        String[] tweetArray = tweetFileLine.split(",");
        int userId = Integer.valueOf(tweetArray[0]);
        int logicalTimeStamp = Integer.valueOf(tweetArray[1]);
        DateTime utcTimeStamp = DateTime.parse(tweetArray[3], DateTimeFormat.forPattern(dateFormat));
        return new Tweet(userId,logicalTimeStamp,tweetArray[2],utcTimeStamp);
    }

    /* loads map of site clocks from disk if present, else initialize map with all clocks at zero */
    public Map<Integer, Map<Integer,Integer>> loadClocks()
    {
        HashMap<Integer,Map<Integer,Integer>> clocks = new HashMap<>();
        BufferedReader bufferedReader = getBufferedReader(clockFile);
        if(bufferedReader == null)
        {
            return initializeClocks();
        }
        String line;
        try{
            while((line = bufferedReader.readLine()) != null)
            {
                parseClocksLine(line, clocks);
            }
            return clocks;
        }
        catch(IOException e)
        {
            ioError(clockFile);
            return null;
        }
    }

    private void parseClocksLine(String line, Map<Integer,Map<Integer,Integer>> clocks)
    {
        String[] arr1 = line.split(";");
        int userId = Integer.valueOf(arr1[0]);
        HashMap<Integer,Integer> subMap = new HashMap<>();
        for(int i = 1; i< arr1.length; i++)
        {
            String[] arr2 = arr1[i].split(",");
            subMap.put(Integer.valueOf(arr2[0]),Integer.valueOf(arr2[1]));
        }
        clocks.put(userId, subMap);
    }

    private Map<Integer,Map<Integer,Integer>> initializeClocks()
    {
        HashMap<Integer,Map<Integer,Integer>> clocks = new HashMap<>();
        for(int i : loadUsers().keySet())
        {
            HashMap<Integer,Integer> subMap = new HashMap<>();
            for(int j: loadUsers().keySet())
            {
                subMap.put(j,0);
            }
            clocks.put(i,subMap);
        }
        return clocks;
    }

    /* loads blocklist from disk if present, else returtn empty collection */
    public Map<Integer, Set<Integer>> loadBlockList()
    {
        Map<Integer, Set<Integer>> blockList = new HashMap<Integer, Set<Integer>>();
        BufferedReader bufferedReader = getBufferedReader(blockListFile);
        if(bufferedReader == null)
        {
            return blockList;
        }
        String line;
        try{
            while((line = bufferedReader.readLine()) != null)
            {
                parseBlockListLine(line, blockList);
            }
            return blockList;
        }
        catch(IOException e)
        {
            ioError(blockListFile);
            return null;
        }
    }

    private void parseBlockListLine(String line, Map<Integer,Set<Integer>> blockList)
    {
        String[] lineArray = line.split(",");
        HashSet<Integer> blockSet = new HashSet<>();
        int userId = Integer.valueOf(lineArray[0]);
        for(int i = 1; i<lineArray.length; i++)
        {
            blockSet.add(Integer.valueOf(lineArray[i]));
        }
        blockList.put(userId, blockSet);
    }

    /* loads partial log from disk if present, else return empty collection */
    public Collection<TwitterEvent> loadPartialLog()
    {
        ArrayList<TwitterEvent> partialLog = new ArrayList<TwitterEvent>();
        BufferedReader bufferedReader = getBufferedReader(partialLogFile);
        if(bufferedReader == null)
        {
            return partialLog;
        }
        String line;
        try{
            while((line = bufferedReader.readLine()) != null) {
                partialLog.add(parsePartialLogEntry(line));
            }
            return partialLog;
        }
        catch(IOException ioException)
        {
            ioError(partialLogFile);
            return null;
        }
    }

    private TwitterEvent parsePartialLogEntry(String line)
    {
        String[] lineArray = line.split(",");
        int userId = Integer.valueOf(lineArray[0]);
        int logicalTimeStamp = Integer.valueOf(lineArray[1]);
        if(lineArray[2].equals("tweet"))
        {
            return new Tweet(userId, logicalTimeStamp, lineArray[3],DateTime.parse(lineArray[4]));
        }
        else if(lineArray[2].equals("block"))
        {
            return new BlockEvent(userId, logicalTimeStamp, Integer.valueOf(lineArray[3]), true);
        }
        else if(lineArray[2].equals("unblock"))
        {
            return new BlockEvent(userId, logicalTimeStamp, Integer.valueOf(lineArray[3]), false);
        }
        else return null;
    }

    /* eras
    es the old partial log file if it exists and replaces it with a new one constructed from newPartialLog */
    public void updatePartialLog(Collection<TwitterEvent> newPartialLog)
    {
        BufferedWriter bufferedWriter = getBufferedWriter(partialLogFile, false);
        try
        {
            for(TwitterEvent event : newPartialLog)
            {
                writePartialLogEntry(event, bufferedWriter);
            }
            bufferedWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error writing to " + partialLogFile);
        }
    }

    private void writePartialLogEntry(TwitterEvent event, BufferedWriter bufferedWriter) throws IOException
    {
        bufferedWriter.write(event.getOriginatorId() + "," + event.getLogicalTimeStamp());
        if(event instanceof BlockEvent)
        {
            BlockEvent blockEvent = (BlockEvent) event;
            String typeString = (blockEvent.getIsBlocking() ? "block" : "unblock");
            bufferedWriter.write("," + typeString + "," + blockEvent.getIdToBlock());
        }
        else if(event instanceof Tweet)
        {
            Tweet tweet = (Tweet) event;
            bufferedWriter.write(",tweet," + tweet.getText() + "," + tweet.getUtcTimeStamp().toString(dateFormat));
        }
        bufferedWriter.write("\n");
    }

    /* saves the tweet to disk, creates new file if necessary */
    public void addTweet(Tweet tweet)
    {
        BufferedWriter bufferedWriter = getBufferedWriter(tweetsFile, true);
        try
        {
            bufferedWriter.write(
                    tweet.getOriginatorId()
                    + ","
                    + tweet.getLogicalTimeStamp()
                    + ","
                    + tweet.getText()
                    + ","
                    + tweet.getUtcTimeStamp().toString(dateFormat)
                    + "\n"
            );
            bufferedWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error writing to " + tweetsFile);
        }
    }

    /* erases old blockList if present, saves newBlockList to file */
    public void updateBlockList(HashMap<Integer, HashSet<Integer>> newBlockList)
    {
        BufferedWriter bufferedWriter = getBufferedWriter(blockListFile, false);
        try {
            for(Map.Entry<Integer, HashSet<Integer>> entry : newBlockList.entrySet())
            {
                String line = Integer.toString(entry.getKey());
                for(int i : entry.getValue())
                {
                    line = line + "," + Integer.toString(i);
                }
                bufferedWriter.write(line + "\n");
            }
            bufferedWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error writing to " + blockListFile);
        }
    }

    /* loads the current user from config file */
    public User loadCurrentUser(){
        return loadUsers().get(userId);
    }

    /* loads list of userIds and their corresponding ip addresses from a config file */
    public Map<Integer, InetAddress> loadAddresses()
    {
        if(addressMap == null)
        {
            parseUserFile();
        }
        return new HashMap<>(addressMap);
    }

    /* load list of userIds and the corresponding user objects */
    public Map<Integer, User> loadUsers()
    {
        if(userMap == null)
        {
            parseUserFile();
        }
        return new HashMap<>(userMap);
    }

    private void parseUserFile()
    {
        BufferedReader bufferedReader = getBufferedReader(userFile);
        userMap = new HashMap<>();
        addressMap = new HashMap<>();
        String line;
        try{
            while((line = bufferedReader.readLine()) != null)
            {
                String[] lineArray = line.split(",");
                int userId = Integer.valueOf(lineArray[0]);
                userMap.put(userId, new User(lineArray[1], userId));
                addressMap.put(userId, InetAddress.getByName(lineArray[2]));
            }
        }
        catch(Exception e)
        {
            ioError(userFile);
        }
    }

    public void updateClocks(HashMap<Integer,HashMap<Integer,Integer>> clocks)
    {
        BufferedWriter bufferedWriter = getBufferedWriter(clockFile, false);
        try {
            for(HashMap.Entry<Integer,HashMap<Integer,Integer>> entry : clocks.entrySet())
            {
                String line = entry.getKey() + ";";
                for(HashMap.Entry<Integer,Integer> subEntry : entry.getValue().entrySet())
                {
                    line = line  + subEntry.getKey() + "," + subEntry.getValue() + ";";
                }
                bufferedWriter.write(line + "\n");
            }
            bufferedWriter.close();
        }
        catch(Exception e)
        {
            System.out.println("Error writing to " + clockFile);
        }
    }
}