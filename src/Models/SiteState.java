package Models;

import java.util.*;

import FileManagement.IFileManager;

public class SiteState implements ISiteState
{
    private User currentUser;
    private HashMap<Integer, HashMap<Integer,Integer>> siteClocks;
    private HashMap<Integer, HashSet<Integer>> blockList;
    private HashSet<Tweet> tweetSet;
    private HashSet<TwitterEvent> partialLog;
    private HashMap<Integer,HashMap<Integer, BlockEvent>> partialBlockLog;
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
        for(TwitterEvent e : partialLog)
        {
            if(!hasEveryoneRec(e) && e instanceof BlockEvent)
            {
                updatePartialBlockLog((BlockEvent) e);
            }
        }
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
        if(e.getIsBlocking())
        {
            blockUser(e.getOriginatorId(), e.getIdToBlock());
        }
        else
        {
            unblockUser(e.getOriginatorId(), e.getIdToBlock());
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
        if(hasRec(e,currentUser.getId()))
        {
            return;
        }
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
        ArrayList<Tweet> tweets = new ArrayList<Tweet>();
        for(Tweet tweet : tweetSet)
        {
            if(isBlockedBy(currentUser.getId(), tweet.getOriginatorId()))
            {
                tweets.add(tweet);
            }
        }
        tweets.sort(new Comparator<Tweet>(){
            @Override
            public int compare(Tweet o1, Tweet o2)
            {
                return o1.getUtcTimeStamp().compareTo(o2.getUtcTimeStamp());
            }
        } );
        return tweets;
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

    private boolean tryRemovePartialBlockLogEntry(BlockEvent e)
    {
        if(e.equals(getPartialBlockEntry(e.getOriginatorId(),e.getIdToBlock())))
        {
            partialBlockLog.get(e.getOriginatorId()).remove(e.getIdToBlock());
            return true;
        }
        return false;
    }

    private void cleanPartialLog()
    {
        for(TwitterEvent e : partialLog)
        {
            if(hasEveryoneRec(e))
            {
                partialLog.remove(e);
                if(e instanceof BlockEvent)
                {
                    tryRemovePartialBlockLogEntry((BlockEvent) e);
                }
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
        cleanPartialLog();
    }

    public int getLocalClock()
    {
        return siteClocks.get(currentUser.getId()).get(currentUser.getId());
    }

    public void incrementLocalClock()
    {
        siteClocks.get(currentUser.getId()).put(currentUser.getId(), getLocalClock() + 1);
        fileManager.loadClocks();
    }

    private Collection<TwitterEvent> getEventsForMessage(int siteId)
    {
        if(isBlockedBy(siteId, currentUser.getId()))
        {
            return null;
        }
        ArrayList<TwitterEvent> ret = new ArrayList<>();
        for(TwitterEvent e : partialLog)
        {
            if(includeInMessage(e,siteId))
            {
                ret.add(e);
            }
        }
        return ret.isEmpty() ? null : ret;
    }

    private void updatePartialBlockLog(BlockEvent e)
    {
        HashMap<Integer,BlockEvent> subLog = partialBlockLog.get(e.getOriginatorId());
        if(subLog == null)
        {
            partialBlockLog.put(e.getOriginatorId(), new HashMap<Integer,BlockEvent>());
        }
        BlockEvent prevEvent = subLog.get(e.getIdToBlock());
        if(prevEvent == null || prevEvent.getLogicalTimeStamp() < e.getLogicalTimeStamp())
        {
            subLog.put(e.getIdToBlock(), e);
        }
    }

    private BlockEvent getPartialBlockEntry(int blockerId, int blockedId)
    {
        if(partialBlockLog.get(blockerId) == null)
        {
            return null;
        }
        return(partialBlockLog.get(blockerId).get(blockedId));
    }

    private boolean includeInMessage(TwitterEvent e, int dest)
    {
        if(hasRec(e, dest))
        {
            return false;
        }
        if(e instanceof BlockEvent)
        {
            return true;
        }
        BlockEvent b = getPartialBlockEntry(e.getOriginatorId(),dest);
        return (!b.getIsBlocking() || b.getLogicalTimeStamp() < e.getLogicalTimeStamp());
    }

    public int getUserId()
    {
        return currentUser.getId();
    }

    public Map<Integer,TwitterMessage> generateMessages()
    {
        HashMap<Integer,TwitterMessage> messages = new HashMap<>();
        HashMap<Integer,HashMap<Integer,Integer>> clocks = (HashMap<Integer,HashMap<Integer,Integer>>) siteClocks.clone();
        for(int i : siteClocks.keySet())
        {
            Collection<TwitterEvent> events = getEventsForMessage(i);
            if(events !=null)
            {
                TwitterMessage msg = new TwitterMessage();
                msg.eventList = events;
                msg.siteClocks = clocks;
                msg.originatorId = getUserId();
                messages.put(i,msg);
            }
        }
        return messages;
    }

    public void saveToDisk()
    {
        fileManager.updatePartialLog(this.partialLog);
        fileManager.updateClocks(this.siteClocks);
        fileManager.updateBlockList(this.blockList);
    }
}
