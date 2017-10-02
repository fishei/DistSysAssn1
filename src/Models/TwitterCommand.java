package Models;

/* Represents a command entered by the user to the command line */
public abstract class TwitterCommand
{
    public TwitterCommandTypes type;

    public TwitterCommand(TwitterCommandTypes type)
    {
        this.type = type;
    }

    public TwitterCommandTypes getType()
    {
        return type;
    }
}
