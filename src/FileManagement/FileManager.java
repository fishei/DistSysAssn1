package FileManagement;

import Models.BlockEvent;
import Models.Tweet;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.*;

import Models.TwitterEvent;
import org.joda.time.DateTime;

public class FileManager implements IFileManager
{
    int n;

    public static final String tweetFile = "tweets.txt";
    public static final String ClockFile = "siteClocks.txt";
    public static final String BlockFile = "blockList.txt";
    public static final String PlFile = "partialLog.txt";
    public static final String UserFile =  "users.txt";

    BufferedReader r = null;
    FileWriter w = null;

    public Collection<Tweet> loadTweets() {
        Collection<Tweet> result = null;

        try {
            r = new BufferedReader(new FileReader(tweetFile));
            String userTweets = "";
            int m = 0;
            while ((userTweets = r.readLine()) != null){
                String[] tweetsData = userTweets.split(";");
                for(int i =0; i< tweetsData.length; i++){
                    String[] tweetData = tweetsData[i].split(",");
                    int lTS = Integer.parseInt(tweetData[0]);
                    DateTime TS = new DateTime(tweetData[2]);

                    Tweet nT = new Tweet(m, lTS, tweetData[1], TS);
                    result.add(nT);
                }
                m++;
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
        Map<Integer, Map<Integer, Integer>> result =  null;

        try {
            r = new BufferedReader(new FileReader(ClockFile));
            String row = "";
            int i = 0;
            while ((row = r.readLine()) != null){
                Map <Integer, Integer> subMap = null;
                String[] timeStamps = row.split(";");
                for(int j =0; j < n; j++){
                    int timestamp = Integer.parseInt(timeStamps[j]);
                    subMap.put(j, timestamp);
                }
                result.put(i, subMap);
                i++;
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
    public Map<Integer, Set<Integer>> loadBlockList() {
        Map<Integer, Set<Integer>> result = null;

        try {
            r = new BufferedReader(new FileReader(BlockFile));
            String uBlock = "";
            int i = 0;
            while ((uBlock = r.readLine()) != null){
                Set <Integer> BlockList = null;

                String[] Blocks = uBlock.split(";");
                for(int j = 0; j < Blocks.length ; j++){
                    int Block = Integer.parseInt(Blocks[j]);
                    BlockList.add(Block);
                }
                result.put(i, BlockList);
                i++;
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
    public Collection<TwitterEvent> loadPartialLog() {
        Collection<TwitterEvent> result = null;

        try {
            r = new BufferedReader(new FileReader(PlFile));
            String userEvents = "";
            int m = 0;
            while ((userEvents = r.readLine()) != null){
                String[] EventsData = userEvents.split(";");
                for(int i =0; i< EventsData.length; i++){
                    int lTS = Integer.parseInt(EventsData[i]);

                    TwitterEvent nE = new TwitterEvent(m, lTS);
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
}
