package uobspe.stonks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class StonksApplication {

	static Player_Bot_Management_System playerManager = new Player_Bot_Management_System();

	static Map<Player_Bot, WebSocketSession> webSocketClients = new HashMap<>();

	static void addWebSocketClient(String message, WebSocketSession session){
		List<String> credentials = Arrays.asList(message.split("#"));

		for(Player_Bot bot : playerManager.getPlayers()){
			if(bot.validate(credentials.get(0), credentials.get(1))){
				webSocketClients.put(bot, session);
				return;
			}
		}
	}
	static Boolean checkEndGame(Stonk_Market market,long lengthofgame){
	    if (System.currentTimeMillis() >= market.getStartSystemMilliseconds() + lengthofgame) {
	        return (true);
        }
	    else{
	        return(false);
        }
    }


	public static void main(String[] args) throws InterruptedException, IOException {
        long lengthofgame = 1000000000;
		SpringApplication.run(StonksApplication.class, args);
		Stonk_Market market = new Stonk_Market(new File("data/stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		
		bots.createBots(35, market.getAliveStonks());  	//crate a number of bots that simulate player behaviour
		
		//bots.addBot(50);
		//bots.getLiveBots().get(100).addNumberOfStonk(market.getStonkWithName("Nokia"), 3, market);
		Boolean endgame = false;

		//Player_Bot playerBot1 = new Player_Bot(10000,1,"Test Team", "asd", "asd");

		//playerBot1.setSubscribedStonks(market.getAliveStonks());


		//bots.getLiveBots().get(1).addNumberOfStonk(market.getStonkWithName("Apple"), 5, market);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				market.getNewsFeed().checkNews();
			}
		}, 0, 5000);//wait 0 ms before doing the action and do it every 1000ms (1second)


		while((!market.getAliveStonks().isEmpty()) && (endgame == false)){
			TimeUnit.SECONDS.sleep(1);
			//System.out.println(" ");
			/*if(market.getStonkWithName("Apple") != null){
				System.out.printf("The player who owns 5 Apple: ");
				System.out.printf("%.2f == %.2f\n",bots.getLiveBots().get(1).getTotalValue() - bots.getLiveBots().get(1).getMoney(),market.getStonkWithName("Apple").getValue() * 5);
			}*/
			market.getRandomChanges();
			market.updateMarket();
			endgame = checkEndGame(market,lengthofgame);
			
			bots.updateAI(market);	// runs the ai on all the bots to better simulate a stock market

			for(Player_Bot player : playerManager.getPlayers()){
				if(webSocketClients.containsKey(player)) {
					if(!webSocketClients.get(player).isOpen()){
						webSocketClients.remove(player);
					}
					else{
						String updatedStonks = "";

						for (Stonk subscribedStonk : player.getSubscribedStonks()){
							updatedStonks += subscribedStonk.getName() + "," + Float.toString(subscribedStonk.getValue()) + "#";
						}
						updatedStonks.stripTrailing();

						TextMessage sendUpadatedStonks = new TextMessage(updatedStonks);

						webSocketClients.get(player).sendMessage(sendUpadatedStonks);
					}

				}
			}

		}

	}

}
