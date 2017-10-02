package FileManagement;

/*
    This interface will handle all reading and writing to files on disk
    Used to load site state and configuration on startup / recovery
    Used to save changes to site state to disk upon reception of TwitterEvents
*/
import Models.*;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IFileManager
{
    /* loads tweets from disk if present, else return empty collection */
    Collection<Tweet> loadTweets();

    /* loads map of site clocks from disk if present, else initialize map with all clocks at zero */
    Map<Integer, Integer> loadClocks();

    /* loads blocklist from disk if present, else return empty collection */
    Map<Integer, Set<Integer>> loadBlockList();

    /* loads partial log from disk if present, else return empty collection */
    Collection<TwitterEvent> loadPartialLog();

    /* erases the old partial log file if it exists and replaces it with a new one constructed from newPartialLog */
    void updatePartialLog(Collection<TwitterEvent> newPartialLog);

    /* saves the tweet to disk, creates new file if necessary */
    void addTweet(Tweet tweet);

    /* erases old blockList if present, saves newBlockList to file */
    void updateBlockList(Map<Integer, Collection<Integer>> newBlockList);

    /* loads the current user from config file */
    User loadCurrentUser();

    /* loads list of users/sites and their corresponding ip addresses from a config file */
    Map<User, InetAddress> loadAddresses();
}
