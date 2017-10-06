package Models;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TweetCommand extends TwitterCommand
{
    private String text;

    public TweetCommand(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}
