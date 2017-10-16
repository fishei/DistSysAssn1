package FileManagement;

import Models.BlockEvent;
import Models.Tweet;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.*;

import Models.TwitterEvent;
import Models.User;
import org.joda.time.DateTime;

public class FileManager implements IFileManager
{
    int n;

    public static final String fileDirectory = "../../../data/";
    public static final String tweetFile = fileDirectory + "tweets.txt";
    public static final String ClockFile = fileDirectory + "siteClocks.txt";
    public static final String BlockFile = fileDirectory + "blockList.txt";
    public static final String PlFile = fileDirectory + "partialLog.txt";
    public static final String UserFile =  fileDirectory + "users.txt";

    private int currentUserId;
    private Map<Integer, User> UserList = null;

    public FileManager(int currentUserId)
    {
        this.currentUserId = currentUserId;
    }

    BufferedReader r = null;
    FileWriter w = null;

    private Map<Integer, User> getUserList(){
        if(UserList == null){
            UserList = loadUsers();
        }
        return  UserList;
    }

    public Collection<Tweet> loadTweets() {
        Collection<Tweet> result = new ArrayList<Tweet>();

        try {
            r = new BufferedReader(new FileReader(tweetFile));
            String userTweets = "";
            while ((userTweets = r.readLine()) != null){
                String[] tweetsData = userTweets.split(";");
                int m = Integer.parseInt(tweetsData[0]);
                for(int i =1; i< tweetsData.length; i++){
                    String[] tweetData = tweetsData[i].split(",");
                    int lTS = Integer.parseInt(tweetData[0]);
                    DateTime TS = new DateTime(tweetData[2]);

                    Tweet nT = new Tweet(m, lTS, tweetData[1], TS);
                    result.add(nT);
                }
            }
        }
        catch (FileNotFoundException e){
            //create file
        }
        catch (IOException e){
            //error handaling
        }
        if(r != null){
            try{
                r.close();
            }
            catch (IOException e){
                //error hanaling
            }
        }
        return result;
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> loadClocks() {
        Map<Integer, Map<Integer, Integer>> result = new HashMap<Integer, Map<Integer, Integer>>();
        Map<Integer, User> UL = getUserList();

        try {
            r = new BufferedReader(new FileReader(ClockFile));
            String row = "";
            int i = 0;
            while ((row = r.readLine()) != null){
                Map <Integer, Integer> subMap = new HashMap<Integer, Integer>();
                String[] timeStamps = row.split(";");
                for(int j =0; j < n; j++){
                    int timestamp = Integer.parseInt(timeStamps[j]);
                    subMap.put(UL.get(j).getId(), timestamp);
                }
                result.put(UL.get(i).getId(), subMap);
                i++;
            }
        }
        catch (FileNotFoundException e){
            for(int i = 0; i < n; i++){
                Map <Integer, Integer> subMap = new HashMap<Integer, Integer>();
                for(int j = 0; j < n; j++){
                    subMap.put(UL.get(j).getId(), 0);
                }
                result.put(UL.get(i).getId(), subMap);
            }
        }
        catch (IOException e){
            //error handaling
        }
        if(r != null){
            try{
                r.close();
            }
            catch (IOException e){
                //error hanaling
            }
        }
        return result;
    }

    @Override
    public Map<Integer, Set<Integer>> loadBlockList() {
        Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();
        Map<Integer, User> UL = getUserList();

        try {
            r = new BufferedReader(new FileReader(BlockFile));
            String uBlock = "";
            int i = 0;
            while ((uBlock = r.readLine()) != null){
                Set <Integer> BlockList = new HashSet<Integer>();

                String[] Blocks = uBlock.split(";");
                for(int j = 0; j < Blocks.length ; j++){
                    int Block = Integer.parseInt(Blocks[j]);
                    BlockList.add(Block);
                }
                result.put(UL.get(i).getId(), BlockList);
                i++;
            }
        }
        catch (FileNotFoundException e){
            for(int i = 0; i < n; i++){
                Set<Integer> temp = new HashSet<Integer>();
                result.put(UL.get(i).getId(), temp);
            }
        }
        catch (IOException e){
            //error handaling
        }
        if(r != null){
            try{
                r.close();
            }
            catch (IOException e){
                //error hanaling
            }
        }
        return result;
    }

    @Override
    public Collection<TwitterEvent> loadPartialLog() {
        Collection<TwitterEvent> result = new ArrayList<TwitterEvent>();

        try {
            r = new BufferedReader(new FileReader(PlFile));
            String userEvents = "";
            int m = 0;
            while ((userEvents = r.readLine()) != null){
                String[] EventsData = userEvents.split(";");
                for(int i =0; i< EventsData.length; i++){
                    TwitterEvent nE = parseTwitterEvent(EventsData[i]);
                    result.add(nE);
                }
                m++;
            }
        }
        catch (FileNotFoundException e){
            //create file
        }
        catch (IOException e){
            //error handaling
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(r != null){
            try{
                r.close();
            }
            catch (IOException e){
                //error hanaling
            }
        }
        return result;
    }

    @Override
    public void updatePartialLog(Collection<TwitterEvent> newPartialLog) {

    }

    @Override
    public void addTweet(Tweet tweet) {
        int target = tweet.getOriginatorId();
        Collection<Tweet> oldTweets = loadTweets();

        //delete old file and create new one

        try{
            w = new FileWriter(tweetFile);

            Iterator<Tweet> tweetList = oldTweets.iterator();
            int currID = -1;
            String row = "";

            while (tweetList.hasNext()){
                Tweet currTweet = tweetList.next();

                if(currID < 0){
                    currID = currTweet.getOriginatorId();
                }
                else if(currID != currTweet.getOriginatorId()){
                    if(currID == target){
                        row += tweet.getLogicalTimeStamp()+","+tweet.getText()+","+tweet.getUtcTimeStamp()+";";
                    }

                    w.write(row);
                    row = "";
                    currID = currTweet.getOriginatorId();
                }

                row += currTweet.getLogicalTimeStamp()+","+currTweet.getText()+","+currTweet.getUtcTimeStamp()+";";

            }
        }
        catch (IOException e){

        }
    }

    private TwitterEvent parseTwitterEvent(String eventString) throws Exception
    {
        String[] eventArray = eventString.split(",");
        int originatorId = Integer.parseInt(eventArray[0]);
        int logicalTimeStamp = Integer.parseInt(eventArray[1]);
        if(eventArray[2].equals("tweet"))
        {
            return new Tweet(
                     originatorId
                    ,logicalTimeStamp
                    ,eventArray[3]
                    ,DateTime.parse(eventArray[4])
            );
        }
        else if(eventArray[2].equals("block"))
        {
            return new BlockEvent(
                     originatorId
                    ,logicalTimeStamp
                    ,Integer.parseInt(eventArray[3])
                    ,true
            );
        }
        else if(eventArray[2].equals("unblock"))
        {
            return new BlockEvent(
                    originatorId
                    ,logicalTimeStamp
                    ,Integer.parseInt(eventArray[3])
                    ,false
            );
        }
        else
        {
            throw new Exception("Invalid event type: " + eventArray[2]);

        }
    }

    @Override
    public void updateBlockList(HashMap<Integer, HashSet<Integer>> newBlockList) {
        //delete old file and create new one

        try{
            w = new FileWriter(BlockFile);

            for(int i = 0; i < newBlockList.size(); i++){
                String row = "";

                Set<Integer> sublist = newBlockList.get(i);
                Iterator<Integer> listB = sublist.iterator();
                while(listB.hasNext()){
                    row += listB.next()+";";
                }
                w.write(row);
            }
        }
        catch (IOException e){

        }
    }

    public User loadCurrentUser(){
        Map<Integer, User> UL = getUserList();
        return (UL.get(currentUserId));
    }

    @Override
    public Map<Integer, User> loadUsers() {
        Map<Integer, User> result = new HashMap<Integer, User>();

        try {
            r = new BufferedReader(new FileReader(UserFile));
            String userData = "";
            while ((userData = r.readLine()) != null){
                String[] CurrData = userData.split(",");
                int currID = Integer.parseInt(CurrData[0]);
                User currUser = new User(CurrData[1], currID);

                //is this right?
                result.put(currID, currUser);
            }
        }
        catch (FileNotFoundException e){
            //create file
        }
        catch (IOException e){
            //error handaling
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(r != null){
            try{
                r.close();
            }
            catch (IOException e){
                //error hanaling
            }
        }

        UserList = result;
        return result;
    }

    @Override
    public Map<Integer, InetAddress> loadAddresses() {
        Map<Integer, InetAddress> result = new HashMap<Integer, InetAddress>();

        try {
            r = new BufferedReader(new FileReader(UserFile));
            String userData = "";
            while ((userData = r.readLine()) != null){
                String[] CurrData = userData.split(",");
                int currID = Integer.parseInt(CurrData[0]);
                InetAddress currAdd = InetAddress.getByName(CurrData[2]);

                result.put(currID, currAdd);
            }
        }
        catch (FileNotFoundException e){
            //create file
        }
        catch (IOException e){
            //error handaling
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(r != null){
            try{
                r.close();
            }
            catch (IOException e){
                //error hanaling
            }
        }
        return result;
    }

    @Override
    public void updateClocks(HashMap<Integer, HashMap<Integer, Integer>> clocks) {
        
    }
}