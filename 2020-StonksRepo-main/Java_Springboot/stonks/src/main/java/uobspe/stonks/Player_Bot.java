package uobspe.stonks;
// Give a team a sum of money and let them create the number of bots that they want [...]
// [...] as long as the sum of money of all their bots added is == money. (Max 100 bots)

import java.util.ArrayList;
import java.util.List;

public class Player_Bot extends Trade_Bot{

    private final String botName;
    private final String teamName;
    private final String teamCode;

    // Information needed for the websockets
    private List<Stonk> subscribedStonks;

    public Player_Bot(float money, Integer id, String teamName, String teamCode, String botName, Player_Bot_Management_System system) {
        super(money, id);
        this.setAsPlayer();
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.botName = botName;
        this.subscribedStonks = new ArrayList<>();
       // system.pushPlayerBot(this);
    }
    public String getBotName(){ return botName; }
/*
    public String getTeamName() {
        return teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }
*/
    public List<Stonk> getSubscribedStonks() {
        return subscribedStonks;
    }
/*
    public void setSubscribedStonks(List<Stonk> subscribedStonks) {
        this.subscribedStonks = subscribedStonks;
    }
*/
    public void addSubscribeStonk(Stonk s){
        this.subscribedStonks.add(s);
    }

    public void removeSubscribedStonk(Stonk s){
        this.subscribedStonks.remove(s);
    }

    public boolean validate(String tName, String tCode){
        if(tName.equals(teamName) && tCode.equals((teamCode))){
            return true;
        }
        return false;
    }
}
