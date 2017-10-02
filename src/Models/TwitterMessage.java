package Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TwitterMessage implements Serializable
{
    public int originatorId;
    public ArrayList<TwitterEvent> eventList;
    public HashMap<Integer, HashMap<Integer,Integer>> siteClocks;
}
