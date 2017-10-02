package Models;

import java.util.Collection;

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
}
