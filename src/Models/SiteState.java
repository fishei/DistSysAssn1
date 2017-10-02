package Models;

import java.util.*;

import FileManagement.IFileManager;

public class SiteState implements ISiteState
{
    private User currentUser;
    private int localClock;
    private HashMap<Integer, Integer> siteClocks;
    private HashMap<Integer, HashSet<Integer>> blockList;
    private HashSet<Tweet> tweetSet;
    private HashSet<TwitterEvent> partialLog;
    private IFileManager fileManager;

    public SiteState(IFileManager fileManager)
    {
        this.fileManager = fileManager;
        this.siteClocks = new HashMap<Integer,Integer>(fileManager.loadClocks());
        this.partialLog = new HashSet<TwitterEvent>(fileManager.loadPartialLog());
        this.blockList = new HashMap<Integer, HashSet<Integer>>();
        for(Map.Entry<Integer, Set<Integer>> entry : fileManager.loadBlockList().entrySet())
        {
            if(entry.getValue().size() > 0)
            {
                blockList.put(entry.getKey(), new HashSet<Integer>(entry.getValue()));
            }
        }
        this.tweetSet = new HashSet<>(fileManager.loadTweets());
        this.currentUser = fileManager.loadCurrentUser();
        this.localClock = siteClocks.get(currentUser.getId());
    }

    public boolean isBlockedBy(int userId, int blockerId)
    {
        if(blockList.get(blockerId) == null)
        {
            return false;
        }
        return blockList.get(blockerId).contains(userId);
    }

    public void onTwitterEvent(TwitterEvent e)
    {

    }

    public boolean hasRec(TwitterEvent e, int siteId)
    {
        return siteClocks.get(siteId) >= e.getLogicalTimeStamp();
    }

    public Collection<Tweet> getTweets()
    {
        return null;
    }

    public Collection<TwitterEvent> getPartialLog()
    {
        return new HashSet<TwitterEvent>(partialLog);
    }

}
