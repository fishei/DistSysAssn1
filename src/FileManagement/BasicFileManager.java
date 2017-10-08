package FileManagement;

import Models.Tweet;
import Models.TwitterEvent;
import Models.User;

import java.net.InetAddress;
import java.util.*;

public class BasicFileManager implements IFileManager
{
    public Collection<Tweet> loadTweets();

    /* loads map of site clocks from disk if present, else initialize map with all clocks at zero */
    public Map<Integer, Map<Integer,Integer>> loadClocks();

    /* loads blocklist from disk if present, else return empty collection */
    public Map<Integer, Set<Integer>> loadBlockList();

    /* loads partial log from disk if present, else return empty collection */
    public Collection<TwitterEvent> loadPartialLog();

    /* erases the old partial log file if it exists and replaces it with a new one constructed from newPartialLog */
    public void updatePartialLog(Collection<TwitterEvent> newPartialLog);

    /* saves the tweet to disk, creates new file if necessary */
    public void addTweet(Tweet tweet);

    /* erases old blockList if present, saves newBlockList to file */
    public void updateBlockList(HashMap<Integer, HashSet<Integer>> newBlockList);

    /* loads the current user from config file */
    public User loadCurrentUser();

    /* loads list of userIds and their corresponding ip addresses from a config file */
    public Map<Integer, InetAddress> loadAddresses();

    /* load list of userIds and the corresponding user objects */
    public Map<Integer, User> loadUsers();

    public void updateClocks(HashMap<Integer,HashMap<Integer,Integer>> clocks);
}
