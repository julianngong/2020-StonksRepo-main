package uobspe.stonks;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class Shorted_Stonk_Data {
    private Integer numberShorted;  //The number of the stock you have shorted
    private Stonk shortedStonk;     //The stonk that has been shorted
    private Float initialPrice;     //The profit will be determined from this
    private long systemMillisWhenBought; //Age of the java whatever when bought

    Shorted_Stonk_Data(Stonk stonk, Integer numberShorted){
        this.shortedStonk = stonk;
        this.numberShorted = numberShorted;
        this.initialPrice = stonk.getValue();
        this.systemMillisWhenBought = System.currentTimeMillis();
    }

    public Integer getNumShorted(){return numberShorted;}

    //How many minutes until interest will need to be payed
    public Float minsLeftInterest(){
        return Float.valueOf(shortedStonk.getShortingPriceWindow().floatValue()) -
                ((System.currentTimeMillis() - systemMillisWhenBought)/ 18000);
    }
/*
    public Stonk getReleventStonk(){
        return shortedStonk;
    }
*/
    //How much interest is being payed right now
    public Float howMuchInterest(){
        Float timeWithInterest = minsLeftInterest() * -1;
        if(timeWithInterest < 0) return 0.0f; //There is no interest yet

        double interest = initialPrice * numberShorted * Math.pow(shortedInterestLevel, timeWithInterest);
        return Double.valueOf(interest).floatValue();
    }
/*
    //How much money would you make if you covered now without interest?
    public Float getProfitIfCoveredNow(){
        return (initialPrice - shortedStonk.getValue()) * numberShorted;
    }
*/
    //How much money would you make if you covered now including the interest you have
    public Float getCostToCover(){
        return (shortedStonk.getValue() * numberShorted) + howMuchInterest();
    }

    //The interest level of the stonk. When you "re-buy" a shorted stonk
    static private Float shortedInterestLevel = 0.5f;
    /*public static void setInterestRate(Float newRate){
        shortedInterestLevel = newRate;
    }
    public static Float getInterestrate(){
        return shortedInterestLevel;
    }*/
}
