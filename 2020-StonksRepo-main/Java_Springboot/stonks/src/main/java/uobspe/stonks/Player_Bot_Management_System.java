package uobspe.stonks;
import uobspe.stonks.Player_Bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player_Bot_Management_System {
    private List<Player_Bot> allPlayers;

    public Player_Bot_Management_System(){
        //Set the interface to query this about the players
        Website_Interface_Bean.playerManager = this;
        Player_Interface_Bean.playerManager = this;
        this.allPlayers = new ArrayList<Player_Bot>();
    }

    //Adds a new player to the system
    public void pushPlayerBot(Player_Bot newPlayer){
        allPlayers.add(newPlayer);
    }

    public HashMap<String, Float> getPlayerValues(){
        HashMap<String, Float> values = new HashMap<>();
        for(Player_Bot bot : allPlayers){
            values.put(bot.getBotName(), bot.getTotalValue());
        }
        return values;
    }

    public List<String> getPlayerNames(){
        List<String> names = new ArrayList<String>();
        for(Player_Bot bot : allPlayers){
            names.add(bot.getBotName());
        }
        return names;
    }


    public List<Player_Bot> getPlayers(){
        return allPlayers;
    }
}