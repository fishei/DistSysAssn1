package Models;

import java.util.*;

import FileManagement.IFileManager;

public class SiteState implements ISiteState
{
    private User currentUser;
    private int localClock;
    private HashMap<Integer, HashMap<Integer,Integer>> siteClocks;
    private HashMap<Integer, HashSet<Integer>> blockList;
    private HashSet<Tweet> tweetSet;
    private HashSet<TwitterEvent> partialLog;
    private IFileManager fileManager;

    public SiteState(IFileManager fileManager)
    {
        this.fileManager = fileManager;
        this.siteClocks = new HashMap<Integer,HashMap<Integer, Integer>>();
        for(Map.Entry<Integer,Map<Integer,Integer>> entry : fileManager.loadClocks().entrySet())
        {
            siteClocks.put(entry.getKey(), new HashMap<Integer,Integer>(entry.getValue()));
        }
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
        this.localClock = siteClocks.get(currentUser.getId()).get(currentUser.getId());
    }

    public boolean isBlockedBy(int userId, int blockerId)
    {
        if(blockList.get(blockerId) == null)
        {
            return false;
        }
        return blockList.get(blockerId).contains(userId);
    }

    private void processTweet(Tweet tweet)
    {
        tweetSet.add(tweet);
    }

    private void processBlockEvent(BlockEvent e)
    {
        if(!hasRec(e,currentUser.getId()))
        {
            if(e.getIsBlocking())
            {
                blockUser(e.getOriginatorId(), e.getIdToBlock());
            }
            else
            {
                unblockUser(e.getOriginatorId(), e.getIdToBlock());
            }
        }
    }

    private void unblockUser(int userId, int blockerId)
    {
        blockList.get(blockerId).remove(userId);
        if(blockList.get(blockerId).isEmpty())
        {
            blockList.remove(blockerId);
        }
    }

    private void blockUser(int userId, int blockerId)
    {
        if(!blockList.containsKey(blockerId))
        {
            blockList.put(blockerId,new HashSet<>());
        }
        blockList.get(blockerId).add(userId);
    }

    public void onTwitterEvent(TwitterEvent e)
    {
        if(!hasEveryoneRec(e))
        {
            partialLog.add(e);
        }
        if(e instanceof Tweet)
        {
            processTweet((Tweet) e);
        }
        else
        {
            processBlockEvent((BlockEvent) e);
        }
    }

    public boolean hasRec(TwitterEvent e, int siteId)
    {
        return siteClocks.get(siteId).get(e.getOriginatorId()) >= e.getLogicalTimeStamp();
    }

    public Collection<Tweet> getTweets()
    {
        return null;
    }

    public Collection<TwitterEvent> getPartialLog()
    {
        return new HashSet<TwitterEvent>(partialLog);
    }

    private void setMaxRow(Map<Integer, Integer> map1, Map<Integer,Integer> map2)
    {
        for(Map.Entry<Integer,Integer> entry : map1.entrySet())
        {
            map1.put(entry.getKey(), Math.max(entry.getValue(), map2.get(entry.getKey())));
        }
    }

    public boolean hasEveryoneRec(TwitterEvent e)
    {
        for(int i : siteClocks.keySet())
        {
            if(!hasRec(e,i))
            {
                return false;
            }
        }
        return true;
    }

    private void updatePartialLog()
    {
        for(TwitterEvent e : partialLog)
        {
            if(hasEveryoneRec(e))
            {
                partialLog.remove(e);
            }
        }
    }

    public void updateClocks(int clockSenderId, HashMap<Integer,HashMap<Integer,Integer>> clocksReceived)
    {
        setMaxRow(siteClocks.get(currentUser.getId()), clocksReceived.get(clockSenderId));
        for(HashMap.Entry<Integer, HashMap<Integer,Integer>> entry : siteClocks.entrySet())
        {
            setMaxRow(siteClocks.get(entry.getKey()), clocksReceived.get(entry.getKey()));
        }
    }
}
