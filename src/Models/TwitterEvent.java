package Models;

import java.io.Serializable;

public abstract class TwitterEvent implements Serializable
{
    private int originatorId;
    private int logicalTimeStamp;

    public TwitterEvent(int originatorId, int logicalTimeStamp)
    {
        this.originatorId = originatorId;
        this.logicalTimeStamp = logicalTimeStamp;
    }

    public int getOriginatorId()
    {
        return originatorId;
    }

    public int getLogicalTimeStamp()
    {
        return logicalTimeStamp;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof TwitterEvent)
        {
            TwitterEvent other = (TwitterEvent) o;
            return (other.getLogicalTimeStamp() == logicalTimeStamp
                    && other.getOriginatorId() == originatorId);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return (logicalTimeStamp * 100 + originatorId);
    }
}
