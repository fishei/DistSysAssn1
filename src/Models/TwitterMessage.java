package Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;


public class TwitterMessage implements Serializable
{
    public int originatorId;
    public Collection<TwitterEvent> eventList;
    public HashMap<Integer, HashMap<Integer,Integer>> siteClocks;
}
