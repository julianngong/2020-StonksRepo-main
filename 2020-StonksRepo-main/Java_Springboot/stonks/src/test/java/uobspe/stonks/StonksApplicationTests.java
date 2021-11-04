package uobspe.stonks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest

class StonksApplicationTests {

	@Autowired
	Player_Interface_Bean player_interface_bean;
	@Autowired
	Website_Interface_Bean webBean;

	@Test
	void contextLoads() {
	}

	@Test
	void short_stonk_test(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		Player_Bot_Management_System players = new Player_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		List<Stonk> alivestonks = new ArrayList<>();
		alivestonks = market.getAliveStonks();
		//creating the bot
		player_interface_bean.addBot("TestTeam1","TestCode1","validbot1");
		//shorting Apple and Nokia
		player_interface_bean.shortStonk("TestTeam1","TestCode1","Apple","2");
		player_interface_bean.shortStonk("TestTeam1","TestCode1","Nokia","2");

		//getting the bot
		Player_Bot bot = players.getPlayers().get(0);
		//shorted Apple and Nokia, so they are included in the shortedstonks map
		System.out.println(alivestonks);
		System.out.println(bot.getShortedStonks());
		assertEquals(bot.getShortedStonks().containsKey(alivestonks.get(0)),true);
		assertEquals(bot.getShortedStonks().containsKey(alivestonks.get(1)),true);
		//Samsung and Tesla is not in the map since it hasn't been shorted
		assertEquals(bot.getShortedStonks().containsKey(alivestonks.get(2)),false);
		assertEquals(bot.getShortedStonks().containsKey(alivestonks.get(3)),false);

	}

	@Test
	void cover_or_short_stonk_Test(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		List<Stonk> alivestonks = new ArrayList<>();
		alivestonks = market.getAliveStonks();

		assert(player_interface_bean.shortStonk("noBot","noBot1","Apple","1")).equals("{\"succeeded\": N/A,\"stockExisted\":N/A,\"minutesUntilInterest\":N/A,\"errorMessage\":\"Invalid Bot\"}");
		player_interface_bean.addBot("TestTeam1","TestCode1","bot1");
		Trade_Bot bot = bots.getLiveBots().get(0);
		assert(player_interface_bean.shortStonk("TestTeam1", "TestCode1", "Apple", "100")).equals("{\"succeeded\": true,\"stockExisted\":true,\"minutesUntilInterest\":"+bot.getShortedStonks().get(alivestonks.get(0)).minsLeftInterest().toString()+",\"errorMessage\":\"\"}");
		player_interface_bean.buyStonks("TestTeam1", "TestCode1", "Nokia","2");
		assert(player_interface_bean.shortStonk("TestTeam1", "TestCode1", "Nokia", "1")).equals("{\"succeeded\": false,\"stockExisted\":true,\"minutesUntilInterest\":N/A,\"errorMessage\":\"Cant short a stonk you own\"}");
		assert(player_interface_bean.coverStonk("TestTeam1", "TestCode1", "Apple")).equals("{\"succeeded\": true,\"stockExisted\":true,\"hadSufficientFunds\":true,\"errorMessage\":\"\"}");
		//buying some more stonks to reduce money, so the stonks shorted earlier won't be able to be covered.
		player_interface_bean.buyStonks("TestTeam1", "TestCode1", "eBay","5");
		assert(player_interface_bean.coverStonk("TestTeam1", "TestCode1", "Apple")).equals("{\"succeeded\": false,\"stockExisted\":true,\"hadSufficientFunds\":false,\"errorMessage\":\"Insufficient Funds\"}");

	}


	@Test
	void cover_stonk_test(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		Player_Bot_Management_System players = new Player_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		List<Stonk> alivestonks = new ArrayList<>();
		alivestonks = market.getAliveStonks();
		//creating the bot
		player_interface_bean.addBot("TestTeam1","TestCode1","validbot1");
		//getting the bot
		Player_Bot bot = players.getPlayers().get(0);
		//shorting Apple
		player_interface_bean.shortStonk("TestTeam1","TestCode1","Apple","2");
		//Apple exists in shortedStonks
		assertEquals(bot.getShortedStonks().containsKey(alivestonks.get(0)),true);
		//covering Apple
		player_interface_bean.coverStonk("TestTeam1","TestCode1","Apple");
		//Apple should no longer exist in shortedStonks.
		assertEquals(bot.getShortedStonks().containsKey(alivestonks.get(0)),false);


	}

	@Test
	void addNumberOfStonkTest(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		for (int i = 0; i <= 4; i++) {
			bots.addBot(i);
			bots.getLiveBots().get(i).addNumberOfStonk(market.getAliveStonks().get(i),i,market);
		}
		bots.addBot(5);
		bots.addBot(6);
		bots.getLiveBots().get(5).addNumberOfStonk(market.getAliveStonks().get(5),2166,market); //test one under max
		bots.getLiveBots().get(6).addNumberOfStonk(market.getAliveStonks().get(6),91,market); //test one over max

		assertEquals(bots.getLiveBots().get(0).getMoney(), 13000.0);
		assertEquals(bots.getLiveBots().get(1).getMoney(), 11880.0);
		assertEquals(bots.getLiveBots().get(2).getMoney(), 11246.0);
		assertEquals(bots.getLiveBots().get(3).getMoney(), 10567.0);
		assertEquals(bots.getLiveBots().get(4).getMoney(), 12796.0);
		assertEquals(bots.getLiveBots().get(5).getMoney(), 4.0);
		assertEquals(bots.getLiveBots().get(6).getMoney(), 13000.0);

		bots.getLiveBots().get(1).addNumberOfStonk(market.getAliveStonks().get(1),3,market);// buying same stonk again
		bots.getLiveBots().get(2).addNumberOfStonk(market.getAliveStonks().get(1),3,market); //same bot buying multiple stonks

		assertEquals(bots.getLiveBots().get(1).getMoney(), 8520.0); // buying same stonk again check
		assertEquals(bots.getLiveBots().get(2).getMoney(), 7886.0); // same bot buying multiple stonks check

		assertEquals(bots.getLiveBots().get(0).getOwnedStonks().get(market.getAliveStonks().get(0)), 0);
		assertEquals(bots.getLiveBots().get(1).getOwnedStonks().get(market.getAliveStonks().get(1)), 4);
		assertEquals(bots.getLiveBots().get(2).getOwnedStonks().get(market.getAliveStonks().get(2)), 2);
		assertEquals(bots.getLiveBots().get(2).getOwnedStonks().get(market.getAliveStonks().get(1)), 3); // multiple stonk test
		assertEquals(bots.getLiveBots().get(3).getOwnedStonks().get(market.getAliveStonks().get(3)), 3);
		assertEquals(bots.getLiveBots().get(4).getOwnedStonks().get(market.getAliveStonks().get(4)), 4);
		assertEquals(bots.getLiveBots().get(5).getOwnedStonks().get(market.getAliveStonks().get(5)), 2166);
		assertEquals(bots.getLiveBots().get(6).getOwnedStonks().get(market.getAliveStonks().get(6)), null);
	}
	@Test
	void reduceNumberOfStonksTest(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		for (int i = 0; i <= 4; i++) {
			bots.addBot(i);
			bots.getLiveBots().get(i).addNumberOfStonk(market.getAliveStonks().get(i),i,market);
			if (i>0) {
				bots.getLiveBots().get(i).reduceNumberOfStonks(market.getAliveStonks().get(i), i-1, market); // selling one less than max
			}
			else{
				bots.getLiveBots().get(i).reduceNumberOfStonks(market.getAliveStonks().get(i), i, market); // test selling nothing
			}
		}
		//Yes these are unbound checked, but we know test stonks has at least 10 stonks, so this is fine
		bots.addBot(5); // testing selling all stonks you have
		bots.getLiveBots().get(5).addNumberOfStonk(market.getAliveStonks().get(5),5,market);
		bots.getLiveBots().get(5).reduceNumberOfStonks(market.getAliveStonks().get(5), 5, market);
		bots.addBot(6);// testing reducing same stonk twice
		bots.getLiveBots().get(6).addNumberOfStonk(market.getAliveStonks().get(6),6,market);
		bots.getLiveBots().get(6).reduceNumberOfStonks(market.getAliveStonks().get(6),2,market);
		bots.getLiveBots().get(6).reduceNumberOfStonks(market.getAliveStonks().get(6),3,market);
		bots.addBot(7);// testing reducing two different stonkst
		bots.getLiveBots().get(7).addNumberOfStonk(market.getAliveStonks().get(7),7,market);
		bots.getLiveBots().get(7).addNumberOfStonk(market.getAliveStonks().get(8),3,market); // also selling something thats not one less than max
		bots.getLiveBots().get(7).reduceNumberOfStonks(market.getAliveStonks().get(7),4,market);
		bots.getLiveBots().get(7).reduceNumberOfStonks(market.getAliveStonks().get(8),1,market);

		assertEquals(bots.getLiveBots().get(0).getMoney(), 13000.0);

		bots.getLiveBots().get(0).reduceNumberOfStonks(market.getAliveStonks().get(4),4,market); // if you dont have the stonk

		assertEquals(bots.getLiveBots().get(0).getMoney(), 13000.0); // if you dont have the stonk
		assertEquals(bots.getLiveBots().get(1).getMoney(), 11880.0);
		assertEquals(bots.getLiveBots().get(2).getMoney(), 12123.0);

		bots.getLiveBots().get(2).reduceNumberOfStonks(market.getAliveStonks().get(2), 10, market); // dont have enough of that stonk to sell

		assertEquals(bots.getLiveBots().get(2).getMoney(), 12123.0); // dont have enough stonks to sell
		assertEquals(bots.getLiveBots().get(3).getMoney(), 12189.0);
		assertEquals(bots.getLiveBots().get(4).getMoney(), 12949.0);
		assertEquals(bots.getLiveBots().get(5).getMoney(), 13000.0);
		assertEquals(bots.getLiveBots().get(6).getMoney(), 12856.0);
		assertEquals(bots.getLiveBots().get(7).getMoney(), 6437.0);
		assertEquals(bots.getLiveBots().get(0).getOwnedStonks().get(market.getAliveStonks().get(0)), null);
		assertEquals(bots.getLiveBots().get(1).getOwnedStonks().get(market.getAliveStonks().get(1)), 1);
		assertEquals(bots.getLiveBots().get(2).getOwnedStonks().get(market.getAliveStonks().get(2)), 1);
		assertEquals(bots.getLiveBots().get(3).getOwnedStonks().get(market.getAliveStonks().get(3)), 1);
		assertEquals(bots.getLiveBots().get(4).getOwnedStonks().get(market.getAliveStonks().get(4)), 1);
		assertEquals(bots.getLiveBots().get(5).getOwnedStonks().get(market.getAliveStonks().get(5)), null);
		assertEquals(bots.getLiveBots().get(6).getOwnedStonks().get(market.getAliveStonks().get(6)), 1);
		assertEquals(bots.getLiveBots().get(7).getOwnedStonks().get(market.getAliveStonks().get(7)), 3);
		assertEquals(bots.getLiveBots().get(7).getOwnedStonks().get(market.getAliveStonks().get(8)), 2);

	}

	@Test
	void updateMarketTest(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		List<Stonk> alivestonks = new ArrayList<>();
		alivestonks = market.getAliveStonks();
		for (int j = 1; j <= 5; j++) { //for 5 possible time of changes
			for (Stonk x : alivestonks) { // for every stonk
				for (int i = 1; i <= 5; i++) { // add 5 changes for each 5 possible times of changes
					x.queueChange(j, 200);
				}
			}
			for (int i = 4; i >= 0; i--) { // for each time of change repeat 5 times for each change
				for (int k=1;k<=j;k++) { // update the market depending on the number for the time of changes
					market.updateMarket();
				}
				for (Stonk x : alivestonks) { // after markets updated check each stonks queue of changes has decreased by 1 as changes have completed
					if (x.getValue() != 0) {
						assertEquals(i, x.getActiveChanges().size());
					}
				}
			}
		}


	}

	@Test
	void stonkChangeTest(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Stonk stonk0 = market.getStonkWithName("Apple");
		Stonk stonk1 = market.getStonkWithName("Samsung");
		Stonk stonk2 = market.getStonkWithName("Nokia");
		Stonk stonk3 = market.getStonkWithName("eBay");
		Stonk stonk4 = market.getStonkWithName("Microsoft");
		Stonk stonk5 = market.getStonkWithName("Amazon");
		Stonk stonk6 = market.getStonkWithName("Dell");
		Float startingvalue0 = stonk0.getValue();
		Float startingvalue1 = stonk1.getValue();
		Float startingvalue2 = stonk2.getValue();
		Float startingvalue3 = stonk3.getValue();
		Float startingvalue4 = stonk4.getValue();
		Float startingvalue5 = stonk5.getValue();
		Float startingvalue6 = stonk6.getValue();
		stonk0.queueChange(1,200);
		stonk1.queueChange(1,-200);
		stonk2.queueChange(3,100);
		stonk3.queueChange(5,99);
		stonk4.queueChange(10,500);
		stonk5.queueChange(7,-250);
		stonk6.queueChange(3,0);
		for(int i=1;i<=5;i++){
			market.updateMarket();
		}
		assertEquals(startingvalue0+200,Math.round(stonk0.getValue()));
		assertEquals(startingvalue1-200,Math.round(stonk1.getValue()));
		assertEquals(startingvalue2+100,Math.round(stonk2.getValue()));
		assertEquals(startingvalue3+99,Math.round(stonk3.getValue()));
		assertEquals(Math.round(startingvalue4+(500*(5.0/10.0))),Math.round(stonk4.getValue()));
		assertEquals(Math.round(startingvalue5+(-250*(5.0/7.0))),Math.round(stonk5.getValue()));
		assertEquals(Math.round(startingvalue6),Math.round(stonk6.getValue()));
	}

	@Test
	void is_bot_name_valid_Test(){
		assert(player_interface_bean.isBotNameValid("test123").equals("{\"result\":\"true\",\"information\":\"Bot name is valid\"}"));
		assert(player_interface_bean.isBotNameValid("9999").equals("{\"result\":\"true\",\"information\":\"Bot name is valid\"}"));
		assert(player_interface_bean.isBotNameValid("0000").equals("{\"result\":\"true\",\"information\":\"Bot name is valid\"}"));
		assert(player_interface_bean.isBotNameValid("aaaa").equals("{\"result\":\"true\",\"information\":\"Bot name is valid\"}"));
		assert(player_interface_bean.isBotNameValid("zzzz").equals("{\"result\":\"true\",\"information\":\"Bot name is valid\"}"));
		assert(player_interface_bean.isBotNameValid("/").equals("{\"result\":\"false\",\"information\":\"Invalid characters input\"}"));
		assert(player_interface_bean.isBotNameValid(":").equals("{\"result\":\"false\",\"information\":\"Invalid characters input\"}"));
		assert(player_interface_bean.isBotNameValid("'").equals("{\"result\":\"false\",\"information\":\"Invalid characters input\"}"));
		assert(player_interface_bean.isBotNameValid("}").equals("{\"result\":\"false\",\"information\":\"Invalid characters input\"}"));
		assert(player_interface_bean.isBotNameValid("test 2").equals("{\"result\":\"false\",\"information\":\"Invalid characters input\"}"));
		assert(player_interface_bean.isBotNameValid("test2%").equals("{\"result\":\"false\",\"information\":\"Invalid characters input\"}"));

	}
	@Test
	void add_bot_Test(){
		player_interface_bean.botManager = new Trade_Bot_Management_System();
		player_interface_bean.playerManager = new Player_Bot_Management_System();
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		Player_Bot_Management_System players = new Player_Bot_Management_System(/*market*/);
		assert(player_interface_bean.addBot("abcd","defg","validbot").equals("{\"result\":\"true\",\"information\":\"Bot successfully added\"}"));
		assert(player_interface_bean.addBot("team1","teamcode1","examplebot123").equals("{\"result\":\"true\",\"information\":\"Bot successfully added\"}"));
		assert(player_interface_bean.addBot("The Best Team","password123","BestBotEver").equals("{\"result\":\"true\",\"information\":\"Bot successfully added\"}"));
		assert(player_interface_bean.addBot("team2","teamcode2","examplebot123").equals("{\"result\":\"false\",\"information\":\"Failure to add bot, already exists\"}"));
		assert(player_interface_bean.addBot("testteam3","teamcode3","BestBotEver").equals("{\"result\":\"false\",\"information\":\"Failure to add bot, already exists\"}"));
		assert(player_interface_bean.addBot("alpha","beta","example bot #$%").equals("{\"result\":\"false\",\"information\":\"Failure to add bot\"}"));
		assert(player_interface_bean.addBot("alphabeta","betagamma","example bot").equals("{\"result\":\"false\",\"information\":\"Failure to add bot\"}"));
		assertEquals(bots.getLiveBots().get(0).getBotName(),"validbot");
		assertEquals(bots.getLiveBots().get(1).getBotName(),"examplebot123");
		assertEquals(bots.getLiveBots().get(2).getBotName(),"BestBotEver");
		assertEquals(players.getPlayerNames().get(0),"validbot");
		assertEquals(players.getPlayerNames().get(1),"examplebot123");
		assertEquals(players.getPlayerNames().get(2),"BestBotEver");
		assertEquals(bots.getLiveBots().get(0).getMoney(),10000);
		assertEquals(bots.getLiveBots().get(1).getMoney(),10000);
		assertEquals(bots.getLiveBots().get(2).getMoney(),10000);
	}

	@Test
	void buy_stonks_Test(){
		player_interface_bean.botManager = new Trade_Bot_Management_System();
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		player_interface_bean.addBot("TestTeam1","TestCode1","validbot1");
		player_interface_bean.addBot("TestTeam2","TestCode2","validbot2");
		player_interface_bean.addBot("TestTeam3","TestCode3","validbot3");
		player_interface_bean.addBot("TestTeam4","TestCode4","validbot4");
		player_interface_bean.addBot("TestTeam5","TestCode5","validbot5");
		player_interface_bean.addBot("TestTeam6","TestCode6","validbot6");
		assert(player_interface_bean.buyStonks("TestTeam1","TestCode1","Apple","8").equals("{\"succeeded\": true,\"stockExisted\":true,\"hadRequisitFunds\":true,\"errorMessage\":\"\"}"));
		assert(player_interface_bean.buyStonks("TestTeam1","TestCode1","Apple","0").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":true,\"errorMessage\":\"Must buy more than 0 stonks\"}"));
		assert(player_interface_bean.buyStonks("TestTeam2","TestCode2","Apple","9").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":false,\"errorMessage\":\"Insufficient Funds\"}"));
		assert(player_interface_bean.buyStonks("TestTeam3","TestCode3","FakeApple","1").equals("{\"succeeded\": false,\"stockExisted\":false,\"hadRequisitFunds\":N/A,\"errorMessage\":\"Stonk Does Not Exist\"}"));
		assert(player_interface_bean.buyStonks("FakeTestTeam3","FakeTestCode3","Apple","1").equals("{\"succeeded\":false,\"errorMessage\": \"Invalid Team Details\"}"));
		assert(player_interface_bean.buyStonks("TestTeam4","TestCode4","Amazon","3").equals("{\"succeeded\": true,\"stockExisted\":true,\"hadRequisitFunds\":true,\"errorMessage\":\"\"}"));
		assert(player_interface_bean.buyStonks("TestTeam4","TestCode4","Amazon","0").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":true,\"errorMessage\":\"Must buy more than 0 stonks\"}"));
		assert(player_interface_bean.buyStonks("TestTeam5","TestCode5","Amazon","4").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitFunds\":false,\"errorMessage\":\"Insufficient Funds\"}"));
		assert(player_interface_bean.buyStonks("TestTeam6","TestCode6","FakeAmazon","1").equals("{\"succeeded\": false,\"stockExisted\":false,\"hadRequisitFunds\":N/A,\"errorMessage\":\"Stonk Does Not Exist\"}"));
		assert(player_interface_bean.buyStonks("FakeTestTeam7","FakeTestCode7","Amazon","1").equals("{\"succeeded\":false,\"errorMessage\": \"Invalid Team Details\"}"));
	}
	@Test
	void sell_stonks_Test(){
		//player_interface_bean.botManager = new Trade_Bot_Management_System();
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		player_interface_bean.addBot("TestTeam7","TestCode7","validbot7");
		player_interface_bean.addBot("TestTeam8","TestCode8","validbot8");
		player_interface_bean.buyStonks("TestTeam7","TestCode7","Apple","8");
		player_interface_bean.buyStonks("TestTeam8","TestCode8","Amazon","3");
		assert(player_interface_bean.sellStonks("TestTeam7","TestCode7","Apple","8").equals("{\"succeeded\": true,\"stockExisted\":true,\"hadRequisitNumOfStocks\":true,\"errorMessage\":\"\"}"));
		assert(player_interface_bean.sellStonks("TestTeam7","TestCode7","Apple","0").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitNumOfStocks\":true,\"errorMessage\":\"Must Sell more than 0 stocks\"}"));
		assert(player_interface_bean.sellStonks("TestTeam7","TestCode7","Apple","9").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitNumOfStocks\":false,\"errorMessage\":\"Insufficient Stocks\"}"));
		assert(player_interface_bean.sellStonks("TestTeam7","TestCode7","FakeApple","1").equals("{\"succeeded\": false,\"stockExisted\":false,\"hadRequisitNumOfStocks\":N/A,\"errorMessage\":\"Stonk Does Not Exist\"}"));
		assert(player_interface_bean.sellStonks("FakeTestTeam7","FakeTestCode7","Apple","1").equals("{\"succeeded\":false,\"errorMessage\": \"Invalid Team Details\"}"));
		assert(player_interface_bean.sellStonks("TestTeam8","TestCode8","Amazon","3").equals("{\"succeeded\": true,\"stockExisted\":true,\"hadRequisitNumOfStocks\":true,\"errorMessage\":\"\"}"));
		assert(player_interface_bean.sellStonks("TestTeam8","TestCode8","Amazon","0").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitNumOfStocks\":true,\"errorMessage\":\"Must Sell more than 0 stocks\"}"));
		assert(player_interface_bean.sellStonks("TestTeam8","TestCode8","Amazon","4").equals("{\"succeeded\": false,\"stockExisted\":true,\"hadRequisitNumOfStocks\":false,\"errorMessage\":\"Insufficient Stocks\"}"));
		assert(player_interface_bean.sellStonks("TestTeam8","TestCode8","FakeAmazon","1").equals("{\"succeeded\": false,\"stockExisted\":false,\"hadRequisitNumOfStocks\":N/A,\"errorMessage\":\"Stonk Does Not Exist\"}"));
		assert(player_interface_bean.sellStonks("FakeTestTeam8","FakeTestCode8","Amazon","1").equals("{\"succeeded\":false,\"errorMessage\": \"Invalid Team Details\"}"));
	}
	@Test
	void get_Stonk_Price_Test(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		assert(player_interface_bean.getStonkPrice("Apple")).equals("{\"stockExisted\":\"true\",\"stonkPrice\":"+"1230.0"+"}");
		assert(player_interface_bean.getStonkPrice("Samsung")).equals("{\"stockExisted\":\"true\",\"stonkPrice\":"+"877.0"+"}");
		assert(player_interface_bean.getStonkPrice("Facebook")).equals("{\"stockExisted\":\"true\",\"stonkPrice\":"+"270.0"+"}");
		assert(player_interface_bean.getStonkPrice("DankMemer")).equals("{\"stockExisted\":\"true\",\"stonkPrice\":"+"800.0"+"}");
		assert(player_interface_bean.getStonkPrice("IBM")).equals("{\"stockExisted\":\"true\",\"stonkPrice\":"+"150.0"+"}");
		assert(player_interface_bean.getStonkPrice("FakeApple")).equals("{\"stockExisted\":\"false\",\"stonkPrice\":\"nil\"}");
		assert(player_interface_bean.getStonkPrice("FakeSamsung")).equals("{\"stockExisted\":\"false\",\"stonkPrice\":\"nil\"}");
		assert(player_interface_bean.getStonkPrice("FakeFacebook")).equals("{\"stockExisted\":\"false\",\"stonkPrice\":\"nil\"}");
	}
	@Test
	void get_Stonk_History_Test(){
		Stonk_Market market = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System bots = new Trade_Bot_Management_System(/*market*/);
		market.setBotSystem(bots);
		List<Stonk> alivestonks = new ArrayList<>();
		alivestonks = market.getAliveStonks();
		Stonk stonk1 = alivestonks.get(1);
		Stonk stonk2 = alivestonks.get(2);
		for(int i=1;i<=10;i++){
			stonk1.queueChange(1, 15*i);
			stonk2.queueChange(1, -20*i);
			market.updateMarket();
		}
		assert(player_interface_bean.getStonkHistory(stonk1.getName(),"2").equals("{\"stockExisted\":\"true\",\"values\": [1795.0,1945.0]}"));
		assert(player_interface_bean.getStonkHistory(stonk1.getName(),"10").equals("{\"stockExisted\":\"true\",\"values\": [1135.0,1165.0,1210.0,1270.0,1345.0,1435.0,1540.0,1660.0,1795.0,1945.0]}"));
		assert(player_interface_bean.getStonkHistory(stonk1.getName(),"5").equals("{\"stockExisted\":\"true\",\"values\": [1435.0,1540.0,1660.0,1795.0,1945.0]}"));
		assert(player_interface_bean.getStonkHistory(stonk1.getName(),"0").equals("{\"stockExisted\":\"true\",\"values\": []}"));
		assert(player_interface_bean.getStonkHistory("FakeStonk","8").equals("{\"stockExisted\":\"false\",\"values\": nil }"));
		assert(player_interface_bean.getStonkHistory(stonk2.getName(),"3").equals("{\"stockExisted\":\"true\",\"values\": [457.0,317.0,157.0]}"));
		assert(player_interface_bean.getStonkHistory(stonk2.getName(),"7").equals("{\"stockExisted\":\"true\",\"values\": [817.0,757.0,677.0,577.0,457.0,317.0,157.0]}"));
		assert(player_interface_bean.getStonkHistory(stonk2.getName(),"4").equals("{\"stockExisted\":\"true\",\"values\": [577.0,457.0,317.0,157.0]}"));
		assert(player_interface_bean.getStonkHistory(stonk2.getName(),"6").equals("{\"stockExisted\":\"true\",\"values\": [757.0,677.0,577.0,457.0,317.0,157.0]}"));
		assert(player_interface_bean.getStonkHistory("FakeStonk2","12").equals("{\"stockExisted\":\"false\",\"values\": nil }"));


	}
	@Test
	void get_Bot_Info_Test(){
		player_interface_bean.botManager = new Trade_Bot_Management_System();
		Stonk_Market market = new Stonk_Market(new File("data/stonks.csv"), new File("data/newsfeed.csv"));
		player_interface_bean.addBot("TestTeam9","TestCode9","validbot9");
		player_interface_bean.addBot("TestTeam10","TestCode10","validbot10");
		player_interface_bean.buyStonks("TestTeam9","TestCode9","Apple","7");
		player_interface_bean.buyStonks("TestTeam10","TestCode10","Amazon","3");
		player_interface_bean.addBot("TestTeam11","TestCode11","validbot11");
		player_interface_bean.botManager = new Trade_Bot_Management_System();
		System.out.println(player_interface_bean.getBotInfo("TestTeam12","TestCode12"));
		assert(player_interface_bean.getBotInfo("TestTeam9","TestCode9")).equals("{\"botExists\":\"true\",\"currentValue\":10000.0,\"currentCash\":1390.0,\"ownedStocks\":[{\"stockName\":\"Apple\",\"numberOwned\":7,\"valueOfOwned\":8610.0}],\"shortedStocks\":[]}");
		assert(player_interface_bean.getBotInfo("TestTeam10","TestCode10")).equals("{\"botExists\":\"true\",\"currentValue\":10000.0,\"currentCash\":214.0,\"ownedStocks\":[{\"stockName\":\"Amazon\",\"numberOwned\":3,\"valueOfOwned\":9786.0}],\"shortedStocks\":[]}");
		assert(player_interface_bean.getBotInfo("FakeTestTeam9","FakeTestCode9")).equals("{\"botExists\":\"false\"}");
		assert(player_interface_bean.getBotInfo("FakeTestTeam10","FakeTestCode10")).equals("{\"botExists\":\"false\"}");
		assert(player_interface_bean.getBotInfo("TestTeam11","TestCode11")).equals("{\"botExists\":\"true\",\"currentValue\":10000.0,\"currentCash\":10000.0,\"ownedStocks\":[],\"shortedStocks\":[]}");

	}

	//Make sure the website only gets transactions from players
	@Test
	void checkOnlyPlayerTransactionsCount(){
		Stonk_Market stonkMarket = new Stonk_Market(new File("data/test_stonks.csv"), new File("data/newsfeed.csv"));
		Trade_Bot_Management_System system = new Trade_Bot_Management_System();
		//should be no transactions to start with
		assert(webBean.getTransactionsFrom("0", "end").length() == 0);
		player_interface_bean.addBot("alpha","beta","exampleBot");
		player_interface_bean.buyStonks("alpha", "beta", "TestStonk1","5");
		player_interface_bean.buyStonks("alpha", "beta", "TestStonk2","2");
		player_interface_bean.sellStonks("alpha", "beta", "TestStonk1","2");
		String currentTransaction = webBean.getTransactionsFrom("0", "end");
		assert(currentTransaction.split("#").length == 3);
		Recipt testRecipt = new Recipt("exampleBot", "TestStonk1", 5, 500, Recipt.TransactionType.Buy, true);
		assert(webBean.getTransactionsFrom("0", "2").split("#")[0].equals(testRecipt.getAsHtmlTableRow()));

	}

}
