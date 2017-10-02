package Models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface ISiteState
{
    /* returns all tweets that the current user is allowed to view */
    Collection<Tweet> getTweets();

    /* processes a twitter event */
    void onTwitterEvent(TwitterEvent e);

    /* return true if this site knows that site with siteId has received event e */
    boolean hasRec(TwitterEvent e, int siteId);

    /* return true if user with userId is blocked by user with blockerId */
    boolean isBlockedBy(int userId, int blockerId);

    Collection<TwitterEvent> getPartialLog();

    /* updates the clocks at the current site based on clocks received in a message from site
       with clockSenderId */
    void updateClocks(int clockSenderId, HashMap<Integer, HashMap<Integer,Integer>> clocksReceived);

}
