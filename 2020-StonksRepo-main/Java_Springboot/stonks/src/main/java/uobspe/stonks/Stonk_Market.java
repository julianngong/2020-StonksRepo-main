package uobspe.stonks;

import java.io.*;
import java.util.*;


public class Stonk_Market {
    private List<Stonk> aliveStonks;
    private List<Stonk> deletedStonks; // Might have some functionality later
    private HashMap<Stonk, Integer> stonkTransactionCounter;
    private List<Recipt> transactionHistory;
    private List<String> sectors;
    private HashMap<String, List<Stonk>> sectorsMap;
    private long startTime;
    private Trade_Bot_Management_System system;
    private Newsfeed newsfeed;

    public Stonk_Market(File stonksFile, File newsFeedFile) {
        aliveStonks = new ArrayList<>();
        transactionHistory = new ArrayList<>();
        deletedStonks = new ArrayList<>();
        sectors = new ArrayList<>();
        sectorsMap = new HashMap<>();
        stonkTransactionCounter = new HashMap<>();
        this.newsfeed = new Newsfeed();
        this.startTime = System.currentTimeMillis();
        //sectorsMap = new HashMap<String, List<Stonk>>();
        Website_Interface_Bean.stonkMarket = this;
        Player_Interface_Bean.stonkMarket = this;
        this.system = null;

        createStonksFromCSV(stonksFile);
        newsfeed.createNewsfeedFromCSV(newsFeedFile);
        populateSectorsMap();
        populateStonkTransactionCounter();

    }

    private void dynamicStonkUpdate(Stonk s){
        int updateThreshold = 15;                    // after selling or buying this number of stonks the price of the specific stonk will update ( >= to 8 )

        if(stonkTransactionCounter.get(s) < (updateThreshold * (-1)) || stonkTransactionCounter.get(s) > updateThreshold){
            s.changeDemandValue(stonkTransactionCounter.get(s) / updateThreshold);
            stonkTransactionCounter.replace(s, stonkTransactionCounter.get(s) % updateThreshold);
        }


    }

    public void countTransaction(Stonk s, int x){
        int c = stonkTransactionCounter.get(s) + x;
        stonkTransactionCounter.replace(s, c);

        dynamicStonkUpdate(s);
    }

    private void populateStonkTransactionCounter(){
        for(Stonk s : aliveStonks){
            stonkTransactionCounter.put(s, 0);
        }
    }

    public void setBotSystem(Trade_Bot_Management_System system){
        this.system = system;
    }

    public long getStartSystemMilliseconds(){
        return startTime;
    }

    public List<Stonk> getAliveStonks() {
        return aliveStonks;
    }
/*
    public List<Stonk> getDeletedStonks() {
        return deletedStonks;
    }
*/
    public List<Stonk> getAllStonks(){
        List<Stonk> allStonks = new ArrayList<>(aliveStonks);
        allStonks.addAll(deletedStonks);
        return allStonks;
    }
  /*  public List<String> getSectors(){ return sectors; }
    public HashMap<String, List<Stonk>> getSectorsMap(){return sectorsMap; }
*/
    public List<Recipt> getTransactionHistory() {
        return transactionHistory;
    }
    public void addRecpit(Recipt transaction){
        transactionHistory.add(transaction);
    }

    // Reades a CSV file and creates the stonks that are in it.
    // The CSV file should have the following format: Stonk_Name;Stonk_Value\n
    private void createStonksFromCSV(File fileName){
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String linie;
            while ((linie = br.readLine()) != null) {
                List<String> attr = Arrays.asList(linie.split(";"));
                aliveStonks.add(new Stonk(attr.get(0), Float.parseFloat(attr.get(1)), Integer.parseInt(attr.get(2)), attr.get(3)));
                if (sectorsMap.containsKey(attr.get(3)) == false) {
                    sectorsMap.put(attr.get(3),null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void populateSectorsMap(){
        //Sector_Container random = new Sector_Container();
        this.sectorsMap = new Sector_Container().allSectors;
    }
    // Returns a random change (with random length and value)
    public void getRandomChanges(){
        Random rand = new Random();
        for(Stonk x : aliveStonks){
            if (x.getActiveChanges().size() <= 100) {
                float maximumValue = 50;
                float minimumValue = -50;
                float change = (float) (Math.random() * (maximumValue + 1 - minimumValue + 1) + minimumValue);
                if (x.getValue() - change <= maximumValue/2) {
                    change = maximumValue * (float)2.5;
                }
                x.queueChange(rand.nextInt(10) + 1, change);
            }
        }
    }

    public Newsfeed getNewsFeed(){
        return(this.newsfeed);
    }
  /*  public Stonk getAliveStonkWithName(String name){
        for(Stonk x : aliveStonks){
            if(x.getName().equals(name))
                return x;
        }
        return null;
    }
*/
    public Stonk getStonkWithName(String name){
        for(Stonk x : aliveStonks){
            if(x.getName().equals(name))
                return x;
        }
        for(Stonk x : deletedStonks){
            if(x.getName().equals(name))
                return x;
        }
        return null;
    }

    // Makes one change for all the stonks (if there is a change avalaible)
    public void updateMarket(){
        long systemTime = System.currentTimeMillis() - startTime;
        List<Stonk> toDelete = new ArrayList<>();
        for(Stonk x : aliveStonks){
            if(x.getValue()<=0) {
                if(!x.hasForcedSell()){
                    x.setForcedSellTrue();
                    system.settleAll(x);
                }
            }
            x.applyChanges(systemTime);
        }
        // deletes all the stonks with negative values
        // Dont think we need the below code anymore but scared to delete it - Julian x
        for(Stonk x : toDelete){
            x.setValue(0);
            aliveStonks.remove(x);
            if(!deletedStonks.contains(x)) // adds the deleted stonks to the deleted array for later use
                deletedStonks.add(x);
        }
    }
    // adds a change in a random position between 5-15th position of the queue
    public void updateStonk(Stonk stonk,Integer lengthoftime,float totalchange){
        stonk.skipQueueTo(lengthoftime,totalchange,(int) (Math.random() * 15) + 5);
        //System.out.println(stonk.getActiveChanges().size());
    }
    // adds a change in a random position between 5-15th position of the queue for a sector
    public void updateSector(String sector,Integer lengthoftime,float totalchange){
        for(Stonk x : sectorsMap.get(sector)){
            x.skipQueueTo(lengthoftime,totalchange,(int) (Math.random() * 15) + 5);
        }
    }


    public class Newsfeed{
        public List<String> possibleNewsStories;
        public List<String> newsStories;
        public List<String> affectedSectors; // sectors that this news story could effect
        public List<Integer> timeOfChanges;
        public List<Float> valueOfChanges;
        public List<String> stonkOrSector; // if the news story changes every stonk in the sector or one stonk in that sector

        public Newsfeed(){
            possibleNewsStories = new ArrayList<>();
            newsStories = new ArrayList<>();
            affectedSectors = new ArrayList<>();
            timeOfChanges = new ArrayList<>();
            valueOfChanges = new ArrayList<>();
            stonkOrSector = new ArrayList<>();
        }

        ArrayList<String> getNewsStoriesPastPoint(Integer index){
            ArrayList<String> ret = new ArrayList<>();

            for(Integer i = index; i < newsStories.size(); i++)
                ret.add(newsStories.get(i));

            return ret;
        }

        private void createNewsfeedFromCSV(File fileName){
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String linie;
                while ((linie = br.readLine()) != null) {
                    List<String> attr = Arrays.asList(linie.split(";"));
                    possibleNewsStories.add(attr.get(0));
                    affectedSectors.add(attr.get(1));
                    timeOfChanges.add(Integer.parseInt(attr.get(2)));
                    valueOfChanges.add(Float.parseFloat(attr.get(3)));
                    stonkOrSector.add(attr.get(4));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void checkNews(){
            Random r = new Random();
            int index = r.nextInt(newsfeed.affectedSectors.size()); // choose a random story
            String newsStory = newsfeed.possibleNewsStories.get(index); // string to print out to the players
            if(newsfeed.stonkOrSector.get(index).equals("stonk")) { // if the news story only effects a single stonk
                if (newsfeed.affectedSectors.get(index).equals("all")) { // if news story effects any one of the possible stonks
                    int ranStonk = r.nextInt(aliveStonks.size()); // choose a random stonk out of all stonks
                    newsStory = newsStory.replaceAll("\\?", aliveStonks.get(ranStonk).getName());
                    updateStonk(aliveStonks.get(ranStonk), newsfeed.timeOfChanges.get(index), newsfeed.valueOfChanges.get(index));
                } else { // if news story effects only a single stonk from a specific sector
                    List<Stonk> availableStonks = new ArrayList<>();
                    for (Stonk x: sectorsMap.get(newsfeed.affectedSectors.get(index))){
                        if(x.getValue() != 0){
                            availableStonks.add(x);
                        }
                    }
                    if (availableStonks.size()>0){
                        int ranStonk = r.nextInt(availableStonks.size());// choose a random stonk out of that sector
                        newsStory = newsStory.replaceAll("\\?", availableStonks.get(ranStonk).getName());
                        updateStonk(availableStonks.get(ranStonk), newsfeed.timeOfChanges.get(index), newsfeed.valueOfChanges.get(index));
                    }
                }
            }
            else{ // if news story effects all stonks of a sector
                List<Stonk> availableStonks2 = new ArrayList<>();
                for (Stonk x: sectorsMap.get(newsfeed.affectedSectors.get(index))){
                    if(x.getValue() != 0){
                        availableStonks2.add(x);
                    }
                }
                if (availableStonks2.size()>0) {
                    updateSector(newsfeed.affectedSectors.get(index), newsfeed.timeOfChanges.get(index), newsfeed.valueOfChanges.get(index));
                }
            }
            System.out.println(newsStory); // print out the news story to the players
            newsStories.add(newsStory);
        }
    }

    public class Sector_Container {
        private HashMap<String, List<Stonk>> allSectors;

        public Sector_Container() {
            this.allSectors = sectorStonks();
        }

        public HashMap<String, List<Stonk>> sectorStonks(){
            HashMap<String, List<Stonk>> sectors = new HashMap<>();
            for(String sector : sectorsMap.keySet()) {
                sectors.put(sector, getsSectorStonks(sector));
            }
            return(sectors);
        }
        private List<Stonk> getsSectorStonks(String sectorName){
            ArrayList<Stonk> sectorStonks = new ArrayList<>();
            for(Stonk stonk : getAllStonks()) {
                if (sectorName.equals(stonk.getSector())) {
                    sectorStonks.add(stonk);
                }
            }
            return(sectorStonks);
        }
    }
}
