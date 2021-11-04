package uobspe.stonks;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;


@Controller
@CrossOrigin(origins = "*")
public class Player_Interface_Bean {
    //Set these in the main function
    public static Trade_Bot_Management_System botManager;
    public static Player_Bot_Management_System playerManager;
    public static Stonk_Market stonkMarket;

    private Player_Bot getTradeBotFromCredentials(String teamName, String teamCode){
        for(Player_Bot bot : playerManager.getPlayers()){
            if(bot.validate(teamName, teamCode))return bot;
        }

        return null;
    }

    //Returns "true" or "false" based on if that bot name is OK for them to use
    //reject if currently in use, contains characters that aren't a-z, 0-9, or space
    //Maybe think about catching swear words or whatever but you don't need to
    @GetMapping("/is_bot_name_valid")
    @ResponseBody
    public String isBotNameValid(@RequestParam String botName){

        if (playerManager.getPlayerNames().contains(botName)){
            return("{\"result\":\"false\",\"information\":\"Bot name already exists\"}");
        }

        String str = botName.toLowerCase();
        char[] botToChar = str.toCharArray();
        for (int i = 0; i < botToChar.length; i++) {
            char ch = botToChar[i];
            if (!((ch >= 'a' && ch <= 'z') || (Character.getNumericValue(ch) >=0 && Character.getNumericValue(ch) <= 9)) || ch == ' ') {
                return ("{\"result\":\"false\",\"information\":\"Invalid characters input\"}");
            }
        }
        return("{\"result\":\"true\",\"information\":\"Bot name is valid\"}");
    }

    //Add a bot with the given team name, code, etc to bot manager and player manager
    @GetMapping("/add_bot")
    @ResponseBody
    public String addBot(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String botName){
        if(isBotNameValid(botName) == "{\"result\":\"false\",\"information\":\"Invalid characters input\"}"){
            return("{\"result\":\"false\",\"information\":\"Failure to add bot\"}");
        }
        if(isBotNameValid(botName) == "{\"result\":\"false\",\"information\":\"Bot name already exists\"}"){
            return("{\"result\":\"false\",\"information\":\"Failure to add bot, already exists\"}");
        }


        Player_Bot bot = new Player_Bot(10000,botManager.getLiveBots().size()+1,teamName, teamCode,botName,playerManager );
        playerManager.pushPlayerBot(bot);
        botManager.addBot(bot);

        return("{\"result\":\"true\",\"information\":\"Bot successfully added\"}");
    }

    //Return information about the bot. How much money to they have? What value do they have?
    //Which stonks and how many? So far in this project, '#' deliniates sections and ',' for seperating values
    //An example return would be "1023.55#1782.98#Apple,4#Samsung,3"
    //An example return would be "1023.55#1782.98#Apple,4#Samsung,3"
    //- means that you have shorted the stock
    @GetMapping("/get_bot_info")
    @ResponseBody
    public String getBotInfo(@RequestParam String teamName, @RequestParam String teamCode){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        if (bot == null) return ("{\"botExists\":\"false\"}");
        String output = "{\"botExists\":\"true\",\"currentValue\":"+ String.valueOf(bot.getTotalValue()) + ",\"currentCash\":"+String.valueOf(bot.getMoney())+",\"ownedStocks\":[";
        for (Map.Entry<Stonk, Integer> entry : bot.getOwnedStonks().entrySet()) {
            output = output + "{\"stockName\":\""+entry.getKey().getName()+"\",\"numberOwned\":"+entry.getValue().toString()+",\"valueOfOwned\":"+String.valueOf(entry.getValue()*entry.getKey().getValue())+"},";
        }
        if (bot.getOwnedStonks().entrySet().size() != 0)
            output = output.substring(0, output.length() - 1);
        output = output + "],\"shortedStocks\":[";
        for(Map.Entry<Stonk, Shorted_Stonk_Data> entry : bot.getShortedStonks().entrySet()){
            output = output + "{\"stockName\":\""+entry.getKey().getName().toString()+"\",\"numberShorted\":"+entry.getValue().getNumShorted().toString()+",\"costToCoverNow\":"+entry.getValue().getCostToCover().toString()+",\"interestPayedToCover\":"+entry.getValue().howMuchInterest().toString()+"},";
        }
        if (bot.getShortedStonks().entrySet().size() != 0)
            output = output.substring(0, output.length() - 1);
        output = output + "]}";

        System.out.println(output);
        
        return output;
    }


    //Also functions for buying and selling
    @GetMapping("/buy_stonks")
    @ResponseBody
    public String buyStonks(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String stonkName,@RequestParam String stonkAmount){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        if(bot == null) return("{\"succeeded\":false,\"errorMessage\": \"Invalid Team Details\"}") ;

        for(Stonk stonk : stonkMarket.getAllStonks()) {
            if (stonkName.equals(stonk.getName())) {
                if (bot.addNumberOfStonk(stonk, Integer.valueOf(stonkAmount), stonkMarket)) {
                    if (Integer.valueOf(stonkAmount) != 0) {
                        return("{\"succeeded\": true,\"stockExisted\":true,\"hadRequisitFunds\":true,\"errorMessage\":\"\"}");
                    }
                    return ("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":true,\"errorMessage\":\"Must buy more than 0 stonks\"}");
                } else {
                    if(bot.getShortedStonks().containsKey(stonk)) return ("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":false,\"errorMessage\":\"Cant buy stonk you have shorted\"}");
                    return ("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":false,\"errorMessage\":\"Insufficient Funds\"}");
                }
            }
        }
        return("{\"succeeded\": false,\"stockExisted\":false,\"hadRequisitFunds\":N/A,\"errorMessage\":\"Stonk Does Not Exist\"}");
    }


    @GetMapping("/sell_stonks")
    @ResponseBody
    public String sellStonks(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String stonkName,@RequestParam String stonkAmount){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        if(bot == null) return("{\"succeeded\":false,\"errorMessage\": \"Invalid Team Details\"}") ;

        for(Stonk stonk : stonkMarket.getAllStonks()) {
            if (stonkName.equals(stonk.getName())) {
                if (bot.reduceNumberOfStonks(stonk, Integer.valueOf(stonkAmount), stonkMarket)){
                    return("{\"succeeded\": true,\"stockExisted\":true,\"hadRequisitNumOfStocks\":true,\"errorMessage\":\"\"}");

                }
                else{
                    if (Integer.valueOf(stonkAmount) != 0) {
                        return ("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitNumOfStocks\":false,\"errorMessage\":\"Insufficient Stocks\"}");
                    }
                    else{
                        return ("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitNumOfStocks\":true,\"errorMessage\":\"Must Sell more than 0 stocks\"}");
                    }
                }
            }

        }
        return("{\"succeeded\": false,\"stockExisted\":false,\"hadRequisitNumOfStocks\":N/A,\"errorMessage\":\"Stonk Does Not Exist\"}");
    }

    //Allows the players to subscribe or unsubscribe to a stonk
    @GetMapping("/subscribe_to_stonks")
    @ResponseBody
    public String subscribeToStonk(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String stonkName){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        if(bot == null) return "Invalid team details";

        for(Stonk stonk : stonkMarket.getAllStonks()) {
            if (stonkName.equals(stonk.getName())) {
                if(bot.getSubscribedStonks().contains(stonk))
                    return "Already subscribed to ".concat(stonkName);
                bot.addSubscribeStonk(stonk);
                return "You successfully subscribed to ".concat(stonkName);
            }
        }
        return "Invalid stonk name";
    }

    @GetMapping("/unsubscribe_to_stonks")
    @ResponseBody
    public String unsubscribeToStonk(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String stonkName){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        if(bot == null) return "Invalid team details";

        for(Stonk stonk : stonkMarket.getAllStonks()) {
            if (stonkName.equals(stonk.getName())) {
                if(bot.getSubscribedStonks().contains(stonk)){
                    bot.removeSubscribedStonk(stonk);
                    return "Successfully unsubscribed to ".concat(stonkName);
                }
                return "You are not subscribed to ".concat(stonkName);
            }
        }
        return "Invalid stonk name";
    }

    //Returns the price of a single stonk
    @GetMapping("/get_stonk_price")
    @ResponseBody
    public String getStonkPrice(@RequestParam String stonkName){
        Stonk s = stonkMarket.getStonkWithName(stonkName);
        if(s != null) return("{\"stockExisted\":\"true\",\"stonkPrice\":"+s.getValue()+"}");
        else return("{\"stockExisted\":\"false\",\"stonkPrice\":\"nil\"}");
    }

    //Returns a list of past values
    @GetMapping("/get_stonk_history")
    @ResponseBody
    public String getStonkHistory(@RequestParam String stonkName, @RequestParam String historyLength){
        Stonk s = stonkMarket.getStonkWithName(stonkName);
        if(s == null) return("{\"stockExisted\":\"false\",\"values\": nil }");;

        ArrayList<Float> history = new ArrayList<>();
        ArrayList<Stonk.History> unparsed = new ArrayList<>(s.getHistory());
        for(Stonk.History h : unparsed){
            history.add(h.getValue());
        }

        int count = 0;
        int historyLengthRequested = Integer.parseInt(historyLength);
        String toReturn = "";
        if(history.size() > historyLengthRequested)
            count = history.size() - historyLengthRequested;

        for(;count < history.size()-1; count++){
            toReturn += Float.toString(history.get(count)) + ",";
        }
        toReturn += Float.toString(history.get(history.size() - 1));
        if (historyLengthRequested == 0){
            toReturn = "";
        }
        return("{\"stockExisted\":\"true\",\"values\": ["+toReturn+"]}");
    }

    //Returns a list of all stonks and their current prices
    @GetMapping("/get_stonk_list")
    @ResponseBody
    public String getStonkList(){
        ArrayList<Stonk> stonks = new ArrayList<>(stonkMarket.getAliveStonks());
        String toReturn = "{\"list\":[";
        for(Stonk s : stonks){
            toReturn += "{\"name\":\""+s.getName()+"\",\"price\":"+s.getValue()+"},";
            //toReturn += s.getName() + "," + Float.toString(s.getValue()) + "#";
        }
        toReturn = toReturn.substring(0, toReturn.length() - 1);
        toReturn += "]}";
        //Get rid of that last '#'
        toReturn.stripTrailing();
        return toReturn;
    }
    @GetMapping("/short_stonk")
    @ResponseBody
    public String shortStonk(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String stonkName, @RequestParam String stonkAmount){
        return (coverOrShortStonk(teamName,teamCode,stonkName,stonkAmount,"short"));
    }

    @GetMapping("/cover_stonk")
    @ResponseBody
    public String coverStonk(@RequestParam String teamName, @RequestParam String teamCode, @RequestParam String stonkName){
        return (coverOrShortStonk(teamName,teamCode,stonkName,"0","cover"));
    }

    private String coverOrShortStonk(String teamName,String teamCode,String stonkName,String stonkAmount, String shortOrCover){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        if(bot == null) return ("{\"succeeded\": N/A,\"stockExisted\":N/A,\"minutesUntilInterest\":N/A,\"errorMessage\":\"Invalid Bot\"}");
        for(Stonk stonk : stonkMarket.getAllStonks()) {
            if (stonkName.equals(stonk.getName())) {
                if (shortOrCover.equals("short")) {
                    Boolean success = bot.shortStonk(stonk,Integer.valueOf(stonkAmount),stonkMarket);
                    if(success)return("{\"succeeded\": true,\"stockExisted\":true,\"minutesUntilInterest\":"+bot.getShortedStonks().get(stonk).minsLeftInterest().toString()+",\"errorMessage\":\"\"}");
                    else return("{\"succeeded\": false,\"stockExisted\":true,\"minutesUntilInterest\":N/A,\"errorMessage\":\"Cant short a stonk you own\"}");

                }
                else {
                    Boolean success = bot.coverShortedStonk(stonk,stonkMarket);
                    if(success)return("{\"succeeded\": true,\"stockExisted\":true,\"hadSufficientFunds\":true,\"errorMessage\":\"\"}");
                    else return("{\"succeeded\": false,\"stockExisted\":true,\"hadSufficientFunds\":false,\"errorMessage\":\"Insufficient Funds\"}");
                }
            }
        }
        return("{\"succeeded\": false,\"stockExisted\":false,\"hadSufficientFunds\":N/A,\"errorMessage\":\"Invalid Stonk Name\"}");

    }
    @GetMapping("/get_interest_on_shorts")
    @ResponseBody
    public String getCurrentInterest(@RequestParam String teamName, @RequestParam String teamCode){
        Player_Bot bot = getTradeBotFromCredentials(teamName, teamCode);
        String output = "";
        for(Map.Entry<Stonk, Shorted_Stonk_Data> entry : bot.getShortedStonks().entrySet()){
            output = "Stonk: " + output + " " + entry.getKey().getName().toString()+ " Time left till interest: " +entry.getValue().minsLeftInterest() +" Current amount of interest: " + entry.getValue().howMuchInterest() + " # ";
        }
        return(output);
    }
}