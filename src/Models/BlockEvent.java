package Models;

public class BlockEvent extends TwitterEvent
{
    private boolean isBlocking;
    private int idToBlock;

    public BlockEvent(int originatorId, int localTimeStamp, boolean isBlocking)
    {
        super(originatorId, localTimeStamp);
        this.isBlocking = isBlocking;
    }

    public boolean getIsBlocking()
    {
        return isBlocking;
    }

    public int getIdToBlock()
    {
        return idToBlock;
    }
}
