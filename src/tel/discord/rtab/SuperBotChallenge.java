package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tel.discord.rtab.enums.GameBot;
import tel.discord.rtab.enums.GameStatus;

public class SuperBotChallenge
{
	GameController gameHandler;
	public TextChannel channel;
	public ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);
	public boolean loadingHumanGame;
	List<int[]> gameList = new LinkedList<>();
	int runDemos;
	int gamesRun;
	int totalGames;
	int gameToLoad;
	List<String> missingPlayers;
	
	public GameController initialise(TextChannel channelID)
	{
		channel = channelID;
		gameHandler = new GameController(channel,false,false,0,true);
		return gameHandler;
	}
	public void loadGames(int demoDelay)
	{
		//Format is "~ CHALLENGE CHANNEL ~ XX Players Remain"
		int playersLeft = Integer.parseInt(channel.getTopic().substring(22,24));
		int multiplier = getMultiplier(playersLeft);
		gameHandler.setMultiplier(multiplier);
		timer.shutdownNow();
		gameList.clear();
		loadingHumanGame = false;
		timer = new ScheduledThreadPoolExecutor(1);
		List<String> list = null;
		try
		{
			list = Files.readAllLines(Paths.get("schedule"+channel.getId()+".csv"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		ListIterator<String> schedule = list.listIterator(0);
		gamesRun = 0;
		totalGames = 0;
		while(schedule.hasNext())
		{
			String[] record = schedule.next().split("	");
			int[] players = new int[record.length];
			for(int i=0; i<record.length;i++)
				players[i] = Integer.parseInt(record[i]);
			gameList.add(players);
			totalGames ++;
		}
		if(totalGames > 0)
		{
			runDemos = demoDelay;
			timer.schedule(() -> loadDemoGame(), demoDelay, TimeUnit.MINUTES);
			channel.sendMessage(totalGames + " games loaded.").queue();
		}
	}
	
	void loadDemoGame()
	{
		//If the season's over, just abort this and don't schedule another
		if(gameHandler.gameStatus == GameStatus.SEASON_OVER)
			return;
		//Run through the list of games
		ListIterator<int[]> currentGame = gameList.listIterator(0);
		while(currentGame.hasNext())
		{
			//Check to make sure there's only bots in this game
			boolean botsOnly = true;
			int[] players = currentGame.next();
			for(int next : players)
				if(GameBot.values()[next].getHuman() != null)
				{
					botsOnly = false;
					break;
				}
			//If there are, load it, pop it off the list, and schedule to look for another demo later
			if(botsOnly)
			{
				prepGame(players);
				currentGame.remove();
				timer.schedule(() -> loadDemoGame(), runDemos, TimeUnit.MINUTES);
				break;
			}
		}
		//If we never found a good demo game that means we're out of demos, so we don't schedule another
	}
	
	public void searchForHumanGame(String humanID)
	{
		//If the season's over, just tell them and exit
		if(gameHandler.gameStatus == GameStatus.SEASON_OVER)
		{
			channel.sendMessage("The season is already over!").queue();
			return;
		}
		//Check which bot they represent, and cut it off early if they aren't any of them
		int botNumber = GameBot.getBotFromHuman(humanID);
		if(botNumber == -1)
		{
			channel.sendMessage("You are not a player in the Super Bot Challenge.").queue();
			return;
		}
		//Run through the list of games
		ListIterator<int[]> currentGame = gameList.listIterator(0);
		List<Integer> gamesWithPlayer = new ArrayList<>(4);
		while(currentGame.hasNext())
		{
			//Check to see if the command caller is here
			boolean playerInGame = false;
			int[] players = currentGame.next();
			for(int next : players)
				if(next == botNumber)
				{
					playerInGame = true;
					break;
				}
			//If they're here, add them to the set
			if(playerInGame)
			{
				gamesWithPlayer.add(currentGame.previousIndex());
			}
		}
		//Now we've got a list of games with the command caller, switch based on how many we found
		switch(gamesWithPlayer.size())
		{
		case 0:
			//If we didn't find any, what are they doing? Just exit
			channel.sendMessage("No scheduled games found.").queue();
			break;
		case 1:
			//If we found exactly one, load it up right away
			loadHumanGame(gamesWithPlayer.get(0), humanID);
			break;
		default:
			//If we found multiple games, list them and ask which they want to run
			channel.sendMessage("Multiple games found. Which one would you like to play now?").queue();
			for(int i=0; i<gamesWithPlayer.size(); i++)
			{
				StringBuilder output = new StringBuilder();
				output.append(i+1);
				int[] botList = gameList.get(gamesWithPlayer.get(i));
				for(int next : botList)
				{
					output.append(" | ");
					output.append(GameBot.values()[next].name);
				}
				channel.sendMessage(output).queue();
			}
			GameController.waiter.waitForEvent(MessageReceivedEvent.class,
					//Right player and channel
					e ->
					{
						if(e.getAuthor().getId().equals(humanID) && e.getChannel().equals(channel))
						{
							//Make sure it's a number and actually fits the range
							try
							{
								int index = Integer.parseInt(e.getMessage().getContentRaw());
								return index > 0 && index <= gamesWithPlayer.size();
							}
							catch(NumberFormatException ex)
							{
								return false;
							}
						}
						return false;
					},
					e -> loadHumanGame(gamesWithPlayer.get(Integer.parseInt(e.getMessage().getContentRaw())-1),humanID),
					30,TimeUnit.SECONDS, () -> channel.sendMessage("Timed out. !ready again when you decide.").queue());
			break;
		}
	}
	
	void loadHumanGame(int index, String humanID)
	{
		//Get a list of other humans in the game
		missingPlayers = new ArrayList<>(3);
		int[] players = gameList.get(index);
		for(int next : players)
		{
			String playerID = GameBot.values()[next].getHuman();
			if(playerID != null && !playerID.equals(humanID))
				missingPlayers.add(playerID);
		}
		//If there aren't any, just load it up
		if(missingPlayers.size() == 0)
		{
			gameList.remove(index);
			prepGame(players);
		}
		//If there are, give them 30 seconds to confirm that they're here too
		else
		{
			loadingHumanGame = true;
			gameToLoad = index;
			//Ping everyone missing
			for(String nextPlayer : missingPlayers)
			{
				channel.sendMessage(String.format("<@!%s>, are you there? Type !ready if you are!", nextPlayer)).queue();
			}
			//Then if they aren't here, reset the whole thing
			timer.schedule(() -> 
			{
				if(loadingHumanGame)
				{
					channel.sendMessage("Other players aren't here. Game aborted.").queue();
					loadingHumanGame = false;
				}
			}, 30, TimeUnit.SECONDS);
		}
	}
	
	public void readyUp(String humanID)
	{
		//Strike them off the list of people we're waiting on, then start the game if everyone's here
		for(int i=0; i<missingPlayers.size(); i++)
			if(missingPlayers.get(i).equals(humanID))
			{
				missingPlayers.remove(i);
				if(missingPlayers.size() == 0)
				{
					prepGame(gameList.get(gameToLoad));
					gameList.remove(gameToLoad);
					loadingHumanGame = false;
				}
				break;
			}
	}
	
	void prepGame(int[] players)
	{
		for(int next : players)
			gameHandler.addBot(next,true);
		channel.sendMessage("Next game starting in five minutes:").queue();
		gamesRun++;
		channel.sendMessage(String.format("**Game %2d/%2d**", gamesRun, totalGames)).queue();
		channel.sendMessage(gameHandler.listPlayers(false)).queue();
		gameHandler.runPingList();
		timer.schedule(() -> gameHandler.startTheGameAlready(), 5, TimeUnit.MINUTES);
	}
	
	int getMultiplier(int playersLeft)
	{
		int multiplier;
		switch(playersLeft)
		{
		case 4:
			multiplier = 10;
			break;
		case 8:
			multiplier = 9;
			break;
		case 12:
			multiplier = 8;
			break;
		case 16:
		case 20:
			multiplier = 7;
			break;
		case 24:
		case 28:
			multiplier = 6;
			break;
		case 32:
		case 36:
			multiplier = 5;
			break;
		case 40:
		case 44:
			multiplier = 4;
			break;
		case 48:
		case 52:
		case 56:
			multiplier = 3;
			break;
		case 60:
		case 64:
		case 68:
			multiplier = 2;
			break;
		case 72:
		case 76:
		case 80:
			multiplier = 1;
			break;
		default:
			multiplier = 1;
			channel.sendMessage("Multiplier not initialised properly!").queue();
		}
		return multiplier;
	}
}