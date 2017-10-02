package Models;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Tweet extends TwitterEvent
{
    private String text;
    private DateTime utcTimeStamp;

    public Tweet(int originatorId, int logicalTimeStamp, String text)
    {
        super(originatorId, logicalTimeStamp);
        this.text = text;
        this.utcTimeStamp = new DateTime(DateTimeZone.UTC);
    }

    public String getText()
    {
        return text;
    }

    public DateTime getUtcTimeStamp()
    {
        return utcTimeStamp;
    }
}
