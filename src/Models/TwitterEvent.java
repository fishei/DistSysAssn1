package Models;

public abstract class TwitterEvent
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
}
