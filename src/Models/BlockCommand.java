package Models;

public class BlockCommand extends TwitterCommand
{
    private String userName;
    private boolean blockOrUnblock;

    public BlockCommand(String userName, boolean blockOrUnblock)
    {
        this.userName = userName;
        this.blockOrUnblock = blockOrUnblock;
    }

    public String getUserName()
    {
        return userName;
    }

    public boolean isBlockOrUnblock()
    {
        return blockOrUnblock;
    }
}
