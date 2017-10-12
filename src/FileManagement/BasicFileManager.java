package FileManagement;

import Models.Tweet;
import Models.TwitterEvent;
import Models.User;

import java.net.InetAddress;
import java.util.*;

/*
    partial IFileManager implementation that loads an initial state every time
    this means that BasicFileManager treats every startup like an initial startup
    (as opposed to a recovery from a site crash)
*/
public class BasicFileManager implements IFileManager
{
    private int userId;
    public BasicFileManager(int userId)
    {
        this.userId = userId;
    }
    public Collection<Tweet> loadTweets()
    {
        return new ArrayList<Tweet>();
    }

    /* loads map of site clocks from disk if present, else initialize map with all clocks at zero */
    public Map<Integer, Map<Integer,Integer>> loadClocks()
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
        return new HashMap<Integer, Set<Integer>>();
    }

    /* loads partial log from disk if present, else return empty collection */
    public Collection<TwitterEvent> loadPartialLog()
    {
        return new ArrayList<TwitterEvent>();
    }

    /* erases the old partial log file if it exists and replaces it with a new one constructed from newPartialLog */
    public void updatePartialLog(Collection<TwitterEvent> newPartialLog){}

    /* saves the tweet to disk, creates new file if necessary */
    public void addTweet(Tweet tweet){}

    /* erases old blockList if present, saves newBlockList to file */
    public void updateBlockList(HashMap<Integer, HashSet<Integer>> newBlockList){}

    /* loads the current user from config file */
    public User loadCurrentUser(){
        return loadUsers().get(userId);
    }

    /* loads list of userIds and their corresponding ip addresses from a config file */
    public Map<Integer, InetAddress> loadAddresses()
    {
        HashMap<Integer,InetAddress> addrs =  new HashMap<Integer,InetAddress>();
        try{
            addrs.put(0, InetAddress.getByName("127.0.0.1"));
            addrs.put(1, InetAddress.getByName("127.0.0.2"));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return addrs;
    }

    /* load list of userIds and the corresponding user objects */
    public Map<Integer, User> loadUsers()
    {
        HashMap<Integer,User> userMap = new HashMap<>();
        userMap.put(0,new User("ifisher",0));
        userMap.put(1,new User("mkrutz",1));
        return userMap;
    }

    public void updateClocks(HashMap<Integer,HashMap<Integer,Integer>> clocks){}
}
