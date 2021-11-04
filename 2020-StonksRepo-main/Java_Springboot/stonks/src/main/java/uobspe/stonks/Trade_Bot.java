package uobspe.stonks;

import java.util.*;


public class Trade_Bot {

    private float money;
    private Map<Stonk, Integer> ownedStonks;
    private Map<Stonk, Shorted_Stonk_Data> shortedStonks;
    private final Integer id;
    private final Float willingness;                // random number that dictates how willing a bot is to sell or buy a stonk -smaller means more likely-
    private final Float enoughProfit;               // the amount of money (percentage) that a bot make on a stonk to be able to sell it
    private final Float spendingHabit;              // the amount of money (percentage) that a bot will be willing to spend on a specific stonk
    private Boolean isPlayer;

    public Trade_Bot(float money, Integer id) {
        this.money = money;
        this.ownedStonks = new HashMap<>();
        this.shortedStonks = new HashMap<>();
        this.id = id;
        this.isPlayer = false;

        Random r = new Random();
        this.willingness = (float)((r.nextInt(15) + 5) / 100.0);  // selects a number between 2 and 10 and than makes it a percentage (between 2% and 10%)
        this.enoughProfit = (float)((r.nextInt(20) + 25) / 100.0);
        this.spendingHabit = (float)((r.nextInt(21) + 20) / 100.0);
        //System.out.printf("The bot %d has W: %f E: %f S: %f ", willingness, enoughProfit, spendingHabit);
    }

    public void setAsPlayer(){
        this.isPlayer = true;
    }

    public void forceSettleStock(Stonk stonk){
        if(ownedStonks.containsKey(stonk)){
            ownedStonks.remove(stonk);
        }
        //Remove the total value of their debt
        for(Map.Entry<Stonk, Shorted_Stonk_Data> x : shortedStonks.entrySet()){
            if (x.getKey().equals(stonk)) {
                money -= x.getValue().getCostToCover();
                shortedStonks.remove(stonk);
            }
        }
    }

    public Map<Stonk, Integer> getOwnedStonks() {
        return ownedStonks;
    }

    public Map<Stonk, Shorted_Stonk_Data> getShortedStonks(){return shortedStonks;}
/*
    public Integer getName() {
        return id;
    }
*/
    public Boolean getIsPlayer(){
        return isPlayer;
    }

    public float getMoney() {
        return money;
    }

    private float[] getAvgMinMaxFromHistory(List<Float> history){
        float maximum = 0;
        float minimum = Float.MAX_VALUE;
        float avg = 0;

        for(float h : history){
            avg = avg + h;
            if(h > maximum){
                maximum = h;
            }
            if(h < minimum){
                minimum = h;
            }
        }

        avg = avg / history.size();

        float[] result = new float[3];
        result[0] = avg;
        result[1] = minimum;
        result[2] = maximum;

        return result;
    }

    // returns the number of stonks to sell
    private int decideToSell(List<Float> history, int numberOfStonksOwned){
        if(history.get(history.size()-1) <= 0)
            return 0;

        float[] avgMinMax = getAvgMinMaxFromHistory(history);
        float avg = avgMinMax[0];
        float minimum = avgMinMax[1];
        float maximum = avgMinMax[2];

        if(minimum <= history.get(history.size() - 1) - history.get(history.size() - 1) * enoughProfit){
            return numberOfStonksOwned/2 + 1;
        }

        if(avg > history.get(history.size() - 1)){

            if(maximum >= history.get(history.size() - 1) + history.get(history.size() - 1) * willingness){
                int toSell = (int)((maximum - history.get(history.size() - 1)) / (willingness * history.get(history.size() - 1)));
                if(numberOfStonksOwned > toSell)
                    return numberOfStonksOwned;
                else
                    return  toSell;
            }

        }


        return 0;
    }


    // returns the history form the last 15 updates
    private ArrayList<Float> getRecentHistory(Stonk s){
        ArrayList<Stonk.History> history = new ArrayList<>(s.getHistory());
        ArrayList<Float> recentHistory = new ArrayList<>();

        for(int i = history.size()-15; i < history.size(); i++){
            recentHistory.add(history.get(i).getValue());
        }

        return recentHistory;
    }

    //decides if the bot should sell some of his stonks and then sell them.
    private void sellDecision(Stonk_Market market){
        HashMap<Stonk, Integer> ownedStonksCopy = new HashMap<>(this.ownedStonks);


        for(Map.Entry<Stonk, Integer> entry : ownedStonksCopy.entrySet()){
            Stonk s = entry.getKey();

            if(s.getHistory().size() < 15)
                continue;

            ArrayList<Float> recentHistory = getRecentHistory(s);


            int decision = decideToSell(recentHistory, entry.getValue());
            if(decision > 0){
                if(decision > 3)
                    decision = 3;
                this.reduceNumberOfStonks(s, decision, market);
            }

        }
    }

    private int decideToBuy(List<Float> history, float moneyToSpend){

        if(history.get(history.size()-1) <= 0)
            return 0;

        float[] avgMinMax = getAvgMinMaxFromHistory(history);
        float avg = avgMinMax[0];
        float minimum = avgMinMax[1];
        float maximum = avgMinMax[2];

        //System.out.printf("I have %f to spend on thins that cost %f each\n", moneyToSpend, history.get(history.size()-1));

        if(maximum >= history.get(history.size() - 1) + history.get(history.size() - 1) * (enoughProfit)){
            return (int)((moneyToSpend) / history.get(history.size() - 1));
        }


        if(avg < history.get(history.size() - 1)){

            if(minimum <= history.get(history.size() - 1) - history.get(history.size() - 1) * willingness){
                int toBuy = (int)((history.get(history.size() - 1) - minimum) / (willingness * history.get(history.size() - 1)));
                if(moneyToSpend < toBuy * history.get(history.size() - 1)){
                    return (int)(moneyToSpend / history.get(history.size() - 1));
                }
                else{
                    return toBuy;
                }
            }


        }


        return 0;
    }


    //decides if the bot should buy some stonks to make a future profit
    private void buyDecision(Stonk_Market market){

        Random rd = new Random();
        ArrayList<Stonk> stocksOfInterest = new ArrayList<>();

        for(int i = 0; i < market.getAliveStonks().size() / 10; i++){
            int randIndex = rd.nextInt(market.getAliveStonks().size());
            if(!ownedStonks.containsKey(market.getAliveStonks().get(randIndex))){
                stocksOfInterest.add(market.getAliveStonks().get(randIndex));
            }
            else{
                i--;
            }
        }


        for(Stonk s : stocksOfInterest){
            if(s.getHistory().size() < 15)
                continue;


            ArrayList<Float> recentHistory = getRecentHistory(s);

            int decision = decideToBuy(recentHistory, this.getMoney() * spendingHabit);
            if(decision > 0){
                if(decision > 3)
                    decision = 3;
                this.addNumberOfStonk(s, decision, market);
            }

        }
    }

    public void makeDecisions(Stonk_Market market){
        sellDecision(market);
        buyDecision(market);
    }

    //Add the stonks bought by the bot AND reduces the bot's money (returns false if the bot doesn't have enough money).
    //Creates and adds a recipt to the stock market

    public boolean addNumberOfStonk(Stonk stonk, Integer numberOfStonks, Stonk_Market market){
        if(shortedStonks.containsKey(stonk))return false; //You need to cover before you buy

        float valueOfStonks = numberOfStonks * stonk.getValue();
        if(money < valueOfStonks)
            return false;

        money = money - numberOfStonks * stonk.getValue();
        market.addRecpit(new Recipt(getBotName(), stonk.getName(), numberOfStonks, valueOfStonks, Recipt.TransactionType.Buy, this.isPlayer));

        if(ownedStonks.containsKey(stonk))
            ownedStonks.put(stonk, ownedStonks.get(stonk) + numberOfStonks);
        else
            ownedStonks.put(stonk, numberOfStonks);

        market.countTransaction(stonk, numberOfStonks);      // mark numberOfStonks as bought stonks (thus reducing the price of the specific stonk)
        return true;
    }

    //Subtract the stonks sold by the bot (returns false if the bot did not have enough stonks)
    //AND increases the bot's money
    public boolean reduceNumberOfStonks(Stonk stonk, Integer numberOfStonks, Stonk_Market market){
        if(!ownedStonks.containsKey(stonk) || (ownedStonks.get(stonk) < numberOfStonks))
            return false;

        float valueOfStonks = stonk.getValue() * numberOfStonks;
        if(ownedStonks.get(stonk) - numberOfStonks == 0)
            ownedStonks.remove(stonk);
        else
            ownedStonks.put(stonk, ownedStonks.get(stonk) - numberOfStonks);

        money = money + valueOfStonks;

        market.countTransaction(stonk, numberOfStonks * (-1));      // mark numberOfStonks as sold stonks (thus reducing the price of the specific stonk)

        market.addRecpit(new Recipt(getBotName(), stonk.getName(), numberOfStonks, valueOfStonks, Recipt.TransactionType.Sell,this.isPlayer)); // receipt spelt wrong both times!?!?!?!?!?!
        return true;
    }

    //Short a stonk. In otherwords, sell a stonk you don't have, with the promise of buying it back later.
    //To prevent this from being an infinite money exploit, there is interest accrued on this action, so the
    //longer you hold a stonk the more you will have to pay.
    //Returns false if you have already shorted this stock, or own the stock
    public boolean shortStonk(Stonk stonk, Integer numberOfStonks, Stonk_Market market){
        if(shortedStonks.containsKey(stonk) || ownedStonks.containsKey(stonk)) return false;

        float valOfShort = stonk.getValue() * numberOfStonks;
        money = money + valOfShort;
        Shorted_Stonk_Data shortedData = new Shorted_Stonk_Data(stonk, numberOfStonks);
        shortedStonks.put(stonk, shortedData);

        market.addRecpit(new Recipt(getBotName(), stonk.getName(), numberOfStonks, valOfShort, Recipt.TransactionType.Short,this.isPlayer));

        market.countTransaction(stonk, numberOfStonks * (-1));      // mark numberOfStonks as sold stonks (thus reducing the price of the specific stonk)

        return true;
    }

    //Resolve the shorting of a stonk by "rebuying" it, plus the interest you have acrued
    //returns false if you don't have enough money to cover it, or the short doesn't exist
    public boolean coverShortedStonk(Stonk stonk, Stonk_Market market){
        if(!shortedStonks.containsKey(stonk)) return false;
        Float costToCover = shortedStonks.get(stonk).getCostToCover();
        if(money < costToCover)return false;

        money -= costToCover;
        Integer numOfStonksToCover = shortedStonks.get(stonk).getNumShorted();
        shortedStonks.remove(stonk);
        market.addRecpit((new Recipt(getBotName(), stonk.getName(), numOfStonksToCover, costToCover, Recipt.TransactionType.Cover,this.isPlayer)));

        market.countTransaction(stonk, numOfStonksToCover);      // mark numberOfStonks as sold stonks (thus reducing the price of the specific stonk)

        return true;
    }

    // return the total value of a Trade_Bot (all it's money and the value of all the owned stonks)
    float getTotalValue(){
        float sum = 0;

        //Add the value of their current holdings
        for(Map.Entry<Stonk, Integer> x : ownedStonks.entrySet()){
            // If a stonk has 0, or negative value it is not going to be added to the total value
            if(x.getKey().getValue() > 0)
                sum = sum + (x.getValue() * x.getKey().getValue());
        }

        //Remove the total value of their debt
        for(Map.Entry<Stonk, Shorted_Stonk_Data> x : shortedStonks.entrySet()){
            sum -= x.getValue().getCostToCover();
        }

        return sum + this.money;
    }

    //Gives some random stonks to the bot as a starting point
    void giveRandomStonks(List<Stonk> stonks){
        Random rd = new Random();
        int maximum = stonks.size();
        for(int i = 0; i<3; i++){
            int random = rd.nextInt(maximum);
            ownedStonks.put(stonks.get(random), (int)(2001/(stonks.get(random).getValue())));
        }

    }

    String getBotName(){
        return "Automatic Trade Bot No." + id.toString();
    }

}
