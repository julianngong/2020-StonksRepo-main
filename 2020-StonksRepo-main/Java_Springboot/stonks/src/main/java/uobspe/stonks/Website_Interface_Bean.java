package uobspe.stonks;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
public class Website_Interface_Bean {
    public static Player_Bot_Management_System playerManager;
    public static Stonk_Market stonkMarket;

    //Returns all the stocks
    @GetMapping("/get_companies")
    @ResponseBody
    public String getStonks(){
        String response = "<tr>\n" +
                "    <th>Company</th>\n" +
                "    <th>Current Value</th>\n" +
                "    <th>Request status</th>\n" +
                "  </tr>";
        Integer counter = 0;
        for(Stonk x : stonkMarket.getAllStonks()){
            response += getStonkTableHTML(x, counter);
            counter+=1;
        }
        return response;
    }

    //Returns the HTML needed to insert in the list
    @GetMapping("/get_bot_status")
    @ResponseBody
    public String getBotStatuses(){
        System.out.printf("Recieved bot status with parameter nothing\n");
        String response = "";
        for(Map.Entry<String, Float> x : playerManager.getPlayerValues().entrySet()){
            response += getBotStatus(x);
        }

        return response;
    }
/*
    @GetMapping("/get_recent_transactions")
    @ResponseBody
    public String getRecentTransactions(){
        return
    }
*/
    //Returns the maximum bot value
    @GetMapping("/get_max_bot_value")
    @ResponseBody
    public String getMaxBotValue(){
        System.out.println("Got request for max value");
        Float maximum = 0f;
        for(Map.Entry<String, Float> x : playerManager.getPlayerValues().entrySet()){
            if(x.getValue() > maximum){
                maximum = x.getValue();
            }
        }

        return maximum.toString();
    }

    //Returns the histories of the requested stonks
    @GetMapping("/get_history_of_stonks")
    @ResponseBody
    public String getStonkHistorys(@RequestParam String withValues, @RequestParam String requestType){
        if(withValues.equals("null")){
            return "";
        }
        String toret = "";

        ArrayList<Integer> indecies = new ArrayList<Integer>();
        for (String field : withValues.split(",+"))
            indecies.add(Integer.parseInt(field));

        for(Integer index : indecies) {
            toret += getStonkData(stonkMarket.getAllStonks().get(index), requestType) + "#";
        }

        return toret;
    }

    @GetMapping("/get_transactions")
    @ResponseBody
    public String getTransactionsFrom(@RequestParam String from, @RequestParam String to){
        //Only care about the player transactions
        List<Recipt> history = stonkMarket.getTransactionHistory().stream().filter(t -> t.isPlayerTransaction()).collect(Collectors.toList());

        Integer fromIndex = Integer.valueOf(from);
        Integer toIndex = to.equals("end") ? history.size() : Integer.valueOf(to);

        String toReturn = "";
        for(int i = fromIndex; i < toIndex; i++){
            toReturn += history.get(i).getAsHtmlTableRow() + '#';
        }

        return toReturn.stripTrailing();
    }

    @GetMapping("/get_news_from")
    @ResponseBody
    public String getNewsFrom(@RequestParam String index){
        ArrayList<String> newNews = stonkMarket.getNewsFeed().getNewsStoriesPastPoint(Integer.valueOf(index));
        if(newNews.size() == 0)
            return "None";
        else{
            String ret = newNews.get(0);

            for(Integer i = 1; i < newNews.size(); i++)
                ret += "#" + newNews.get(i);

            return ret;
        }
    }

    private String getStonkTableHTML(Stonk x, Integer counter){
        String specialFuncName = "ButtonClick"+counter.toString();

        String toret = "<tr id = \"row" + counter.toString()+"\">";
        toret += "<td>" + x.getName() + "</td>";
        toret += "<td>" + x.getValue().toString() + "</td>";
        //Add a button that will call "addClicked(counter)"
        //The button will have the id "button" and then the counter number
        toret += "<td>" +
                    "<button type=\"button\" id=\"button" + counter.toString()+"\" " +
                        "onclick = \"addClicked(" + counter.toString() + ")\">" +
                        "Toggle on graph" +
                "</button>"
                +"</td>";

        return toret + "</tr>";
    }

    private String getBotStatus(Map.Entry<String, Float> bot){
        String toret = "";

        toret+=bot.getKey();
        toret+=",";
        toret+=bot.getValue().toString();

        toret += "#";
        return toret;
    }

    private String getStonkData(Stonk x, String requestType){
        ArrayList<Stonk.History> history = new ArrayList<>(x.getHistory());
        String toret = "";
        Float accumulator = 0.0f;
        Float incrimentValue = 1.0f;
        Integer canvasSize = 600;

        if(requestType.equals("recent")) {
            //Get this based off of the canvas size we are using for the graph
            if (x.getHistory().size() > canvasSize) {
                history = new ArrayList<>(history.subList(history.size() - (canvasSize + 1), history.size() - 1));
            }
        }
        else if(requestType.equals("long")){
            if(history.size() > (canvasSize * 3)){
                history = new ArrayList<>(history.subList(history.size() - ((canvasSize * 3) + 1), history.size() - 1));
            }
            incrimentValue  =  0.333333f;
        }
        else{
            incrimentValue = canvasSize.floatValue() / history.size();
        }
        //This is also for lifetime
        Integer added = 0;
        for(Stonk.History f : history){
            accumulator += incrimentValue;
            if(accumulator >= 1) {
                toret += f.toString() + " ";
                accumulator -= 1.0f;
                added += 1;
            }
        }
        System.out.println(added);
        System.out.println(incrimentValue);
        return toret + "#" + x.getName();
    }
}
