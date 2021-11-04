package uobspe.stonks;

import java.util.ArrayList;
import java.util.List;

public class Stonk {

    private final Integer FreezeMax = 100;

    public class History{
        private Float value;
        private long millisecond;
        private String timestamp;
        public History(Float value, long millisecond){
            this.value = value;
            this.millisecond = millisecond;

            Long seconds = millisecond / 1000;
            Long minutes = seconds / 60;
            Long hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            this.timestamp = fts(hours.toString()) + ":" +
                             fts(minutes.toString()) + ":" +
                             fts(seconds.toString());

        }

        public Float getValue(){return value;}
        public String toString(){
            return value.toString() + "&" + timestamp;
        }

        private String fts(String in){
            if(in.length() < 2) in = "0"+in;
            return in;
        }
    }

    private final String name;
    private final String sector;
    private Float value;                                // The present value of the stonk
    private List<History> history;                        // The list with the precedent values of the stonk
    private List<Stonk_Change> activeChanges;           // a list with all the changes that should be made in the future
    private Integer shortingPriceWindow;                // How long an invester has to close a short position on this stock
                                                        // in minutes before acruing interest
    private Boolean frozen;                             //Will freeze if price hits zero
    private Integer freezeTimer;
    private Boolean forcedSell;                         //Has all bots been forced to sell this yet?

    public Stonk(String name, Float value, Integer shortingPriceWindow, String sector) {
        this.name = name;
        this.value = value;
        this.shortingPriceWindow = shortingPriceWindow;
        this.sector = sector;
        activeChanges = new ArrayList<Stonk_Change>();
        history = new ArrayList<History>();

        this.frozen = Boolean.FALSE;
        this.freezeTimer = 0;
        this.forcedSell = false;
    }

    private Float getValueCountingFrozen(){
        if(this.frozen){
            return 0.0f;
        }
        return value;
    }

    @Override
    public String toString() {
        return "Stonk{" +
                "name='" + name + '\'' +
                ", value=" + getValueCountingFrozen() +
                ", sector='" + sector + '\'' +
                '}';
    }

    //A small class used to model requested changes to a Stonk
    public class Stonk_Change{
        //How long the Stonk this belongs to will affect it
        private final int lifespan;
        //How long has this change existed for. Will be deleted by its owner Stonk when it reaches lifespan
        private int age;

        //The amount of change this will affect each time it is called to effect the Stonk
        private final float partialChange;


        //Returns how much the owning Stonk's price should change by
        public Float getChange(){
            return this.partialChange;
        }

        //Tells the change to "age" by 1. When it reaches the end of its lifespan, the change is finished
        public void incrementAge(){
            this.age = this.age + 1;
        }
        //Returns true if the change has reached its end (its "age" is equal to its lifespan)
        public boolean hasReachedEnd(){
            return this.age >= this.lifespan;
        }

        Stonk_Change(int lengthOfTime, float totalChange){
            this.lifespan = lengthOfTime;
            this.age = 0;

            this.partialChange = totalChange / lengthOfTime;
        }
    }
    // function to skip and add change to specific location of queue
    public void skipQueueTo(int lengthOfTime, float totalChange,int index){
        if (activeChanges.size() > index){
            activeChanges.set(index,new Stonk_Change(lengthOfTime, totalChange));
        }
        else{
            if (activeChanges.size() <= 100) {
                queueChange(lengthOfTime, totalChange);
            }
        }
    }

    public String getName() {
        return name;
    }

    public List<Stonk_Change> getActiveChanges(){return activeChanges;}



    public Float getValue() {
        return getValueCountingFrozen();
    }

    public String getSector(){ return sector; }

    public List<History> getHistory() {
        return history;
    }

    public Boolean hasForcedSell(){ return this.forcedSell;}
    public void setForcedSellTrue(){
        this.forcedSell = true;
    }

    public void setValue(float x){
        this.value = x;
    }

    public void changeDemandValue(int x){       // increases (x > 0 - number of changes) or decreases (x < 0 - number of changes) the value according to the demand of the stonk
        float percentage = (float)0.007;         // 0.7 percent increase or decrease
        if(!this.frozen){
            float newValue = getValue() + getValue() * (percentage * x);
            if(newValue >= 0 && newValue < 50000){
                setValue(getValue() + getValue() * (percentage * x));
            }
            else
                setValue(0);
        }
    }

    public Integer getShortingPriceWindow(){return shortingPriceWindow;}

    // Applies one given change (the value of a stonk cannot be negative, but can reach 0)
    private void applyChange(Stonk_Change change){
        change.incrementAge();
        this.value = this.value + change.partialChange;
        if(this.value < 0) {
            this.value = (float) 0;
            this.frozen = true;
            this.freezeTimer = FreezeMax;
            this.activeChanges.clear();
            this.forcedSell = false;
        }
    }
    // If there are changes to be made, make the first one and delete it from the list.
    public void applyChanges(long systemTime){
        if(!this.frozen) {
            if (!activeChanges.isEmpty())
                applyChange(activeChanges.get(0));

            else return;
            if (!activeChanges.isEmpty()) {
                if (activeChanges.get(0).hasReachedEnd())
                    activeChanges.remove(0);
                this.history.add(new History(this.value, systemTime));
            }
        }
        else{
            this.freezeTimer -= 1;
            if(this.freezeTimer == 0){
                this.frozen = false;
                this.value = 500.0f;
            }
        }
    }

    // Adds a change, to the queue, with a given length and total value
    public void queueChange(int lengthOfTime, float totalChange){
        if(this.frozen) return; //Do nothing if it has been frozen
        activeChanges.add(new Stonk_Change(lengthOfTime, totalChange));
    }


}
