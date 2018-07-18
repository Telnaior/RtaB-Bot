package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tel.discord.rtab.enums.PlayerJoinReturnValue;
import tel.discord.rtab.enums.PlayerQuitReturnValue;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

public class GameController
{
	static final int BOARD_SIZE = 15;
	static final int PLAYER_COUNT = 2;
	static MessageChannel channel = null;
	static Player[] players = new Player[PLAYER_COUNT];
	static int currentTurn = -1;
	public static int playersJoined = 0;
	public static int gameStatus = 0;
	static boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	static int spacesLeft = BOARD_SIZE;
	static boolean[] bombs = new boolean[BOARD_SIZE];
	static Board gameboard;
	public static EventWaiter waiter;
	/*
	 * reset - (re)initialises the game state by removing all players and clearing the board.
	 */
	public static void reset()
	{
		channel = null;
		players = new Player[PLAYER_COUNT];
		currentTurn = -1;
		playersJoined = 0;
		gameStatus = 0;
		pickedSpaces = new boolean[15];
		spacesLeft = BOARD_SIZE;
		bombs = new boolean[15];
		gameboard = null;
	}
	/*
	 * addPlayer - adds a player to the game.
	 * MessageChannel channelID - channel the request took place in (only used to know where to send game details to)
	 * String playerID - ID of player to be added.
	 * Returns an enum which gives the result of the join attempt.
	 */
	public static PlayerJoinReturnValue addPlayer(MessageChannel channelID, Member playerID)
	{
		if(!((players[0] != null && playerID.getUser().equals(players[0].user))||(players[1] != null && playerID.getUser().equals(players[1].user))))
		{
			switch(playersJoined)
			{
			case 0:
				channel = channelID;
				players[0] = new Player(playerID);
				playersJoined++;
				channel.sendMessage(players[0].name + " joined the game. One more player is required.").queue();
				return PlayerJoinReturnValue.JOINED1;
			case 1:
				players[1] = new Player(playerID);
				playersJoined++;
				channel.sendMessage(players[1].name + " joined the game. The game is now starting. Please PM bombs within the next 60 seconds.").queue();
				getBombs();
				return PlayerJoinReturnValue.JOINED2;
			default:
				return PlayerJoinReturnValue.GAMEFULL;
			}
		}
		else
			return PlayerJoinReturnValue.ALREADYIN;
	}
	/*
	 * removePlayer - removes a player from the game.
	 * MessageChannel channelID - channel the request was registered in.
	 * String playerID - ID of player to be removed.
	 */
	public static PlayerQuitReturnValue removePlayer(MessageChannel channelID, User playerID)
	{
		if(gameStatus != 0)
			return PlayerQuitReturnValue.GAMEINPROGRESS;
		if(players[0] != null && playerID == players[0].user)
		{
			players[0] = null;
			playersJoined--;
			switch(playersJoined)
			{
			case 0:
				reset();
				break;
			case 1:
				players[0] = players[1];
				players[1] = null;
				break;
			default:
				return PlayerQuitReturnValue.UNEXPECTEDPLAYERCOUNT;
			}
			return PlayerQuitReturnValue.SUCCESS;
		}
		else if(players[1] != null && playerID == players[1].user)
		{
			players[1] = null;
			playersJoined--;
			if(playersJoined != 1)
				return PlayerQuitReturnValue.UNEXPECTEDPLAYERCOUNT;
			else
				return PlayerQuitReturnValue.SUCCESS;
		}
		else
			return PlayerQuitReturnValue.NOTINGAME;
	}
	/*
	 * runGame - controls the actual game logic once the game is ready to go.
	 */
	static void getBombs()
	{
		//Declare game in progress
		gameStatus = 1;
		//Request players send in bombs
		players[0].user.openPrivateChannel().queue(
				(channel) -> channel.sendMessage("Please PM your bomb by sending a number 1-" + BOARD_SIZE).queue());
		players[1].user.openPrivateChannel().queue(
				(channel) -> channel.sendMessage("Please PM your bomb by sending a number 1-" + BOARD_SIZE).queue());
		//Wait for bombs to return
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Check if right player, and valid bomb pick
				e -> (e.getAuthor().equals(players[0].user) && checkValidNumber(e.getMessage().getContentRaw())),
				//Parse it and update the bomb board
				e -> 
				{
					bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
					players[0].user.openPrivateChannel().queue(
							(channel) -> channel.sendMessage("Bomb placement confirmed.").queue());
					checkReady();
				},
				//Or timeout after a minute
				1, TimeUnit.MINUTES, () ->
				{
					gameStatus = 0;
					checkReady();
				});
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Check if right player, and valid bomb pick
				e -> (e.getAuthor().equals(players[1].user) && checkValidNumber(e.getMessage().getContentRaw())),
				//Parse it and update the bomb board
				e -> 
				{
					bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
					players[1].user.openPrivateChannel().queue(
							(channel) -> channel.sendMessage("Bomb placement confirmed.").queue());
					checkReady();
				},
				//Or timeout after a minute
				1, TimeUnit.MINUTES, () ->
				{
					gameStatus = 0;
					checkReady();
				});
	}
	static void checkReady()
	{
		if(gameStatus == 0)
		{
			channel.sendMessage("Bomb placement timed out.").queue();
		}
		else
			gameStatus++;
		if(gameStatus > 2)
		{
			//Determine first player
			currentTurn = (int)(Math.random()*PLAYER_COUNT);
			gameboard = new Board(BOARD_SIZE);
			channel.sendMessage("Let's go!").queue();
			runTurn();
		}
	}
	static void runTurn()
	{
		channel.sendMessage(players[currentTurn].user.getAsMention() + ", your turn. Choose a space on the board.")
			.completeAfter(3,TimeUnit.SECONDS);
		displayBoardAndStatus();
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Right player and channel
				e ->
				{
					if(e.getAuthor().equals(players[currentTurn].user) && e.getChannel().equals(channel)
							&& checkValidNumber(e.getMessage().getContentRaw()))
					{
							int location = Integer.parseInt(e.getMessage().getContentRaw());
							if(pickedSpaces[location-1])
							{
								channel.sendMessage("That space has already been picked.").queue();
								return false;
							}
							else
								return true;
					}
					return false;
				},
				//Parse it, update the board, and reveal the result
				e -> 
				{
					int location = Integer.parseInt(e.getMessage().getContentRaw())-1;
					pickedSpaces[location] = true;
					channel.sendMessage("Space " + (location+1) + " selected...").completeAfter(1,TimeUnit.SECONDS);
					if(bombs[location])
					{
						if(Math.random()<0.5)
							channel.sendMessage("...").completeAfter(5,TimeUnit.SECONDS);
						channel.sendMessage("**BOOM**").completeAfter(5,TimeUnit.SECONDS);
						channel.sendMessage(players[currentTurn].user.getAsMention() +
								" loses $250,000 as penalty for blowing up.").queue();
						players[currentTurn].addMoney(-250000,false);
						players[currentTurn].booster = 100;
						players[currentTurn].winstreak = 0;
						gameStatus = 4;
					}
					else
					{
						if((Math.random()*spacesLeft)<1)
							channel.sendMessage("...").completeAfter(5,TimeUnit.SECONDS);
						//Figure out what space we got
						StringBuilder resultString = new StringBuilder();
						switch(gameboard.typeBoard[location])
						{
						case CASH:
							//On cash, update the player's score and tell them how much they won
							int cashWon = gameboard.cashBoard[location];
							resultString.append("**");
							if(cashWon<0)
								resultString.append("-");
							resultString.append("$");
							resultString.append(String.format("%,d",Math.abs(cashWon)));
							resultString.append("**");
							players[currentTurn].addMoney(cashWon,false);
							break;
						case BOOSTER:
							//On cash, update the player's booster and tell them what they found
							int boostFound = gameboard.boostBoard[location];
							resultString.append("A **" + boostFound + "%** Booster!");
							players[currentTurn].addBooster(boostFound);
							break;
						default:
							//This will never happen
							resultString.append("**An error!** @Atia#2084 fix pls");
							break;
						}
						channel.sendMessage(resultString).completeAfter(5,TimeUnit.SECONDS);
					}
					//Advance turn to next player
					currentTurn++;
					currentTurn = currentTurn % PLAYER_COUNT;
					
					if(gameStatus == 4)
					{
						channel.sendMessage("Game Over. " + players[currentTurn].user.getAsMention() + " Wins!")
							.completeAfter(3,TimeUnit.SECONDS);
						players[currentTurn].winstreak ++;
						//Award $20k for each space picked, and double it if every space was picked
						int winBonus = 20000*(BOARD_SIZE-spacesLeft);
						if(spacesLeft == 0)
							winBonus *= 2;
						channel.sendMessage(players[currentTurn].name + " receives a win bonus of $"
								+ String.format("%,d",winBonus) + ".").queue();
						players[currentTurn].addMoney(winBonus,true);
						displayBoardAndStatus();
						saveData();
						reset();
					}
					else
					{
						runTurn();
					}
				});
	}
	static boolean checkValidNumber(String message)
	{
		try
		{
			int location = Integer.parseInt(message);
			return (location > 0 && location <= BOARD_SIZE);
		}
		catch(NumberFormatException e1)
		{
			return false;
		}
	}
	static void displayBoardAndStatus()
	{
		//Build up board display
		StringBuilder board = new StringBuilder().append("```\n");
		board.append("     RtaB     \n");
		for(int i=0; i<BOARD_SIZE; i++)
		{
			if(pickedSpaces[i])
			{
				board.append("  ");
			}
			else
			{
				board.append(String.format("%02d",(i+1)));
			}
			if(i%5==4)
				board.append("\n");
			else
				board.append(" ");
		}
		board.append("\n");
		//Next the status line
		//Start by getting the lengths so we can pad the status bars appropriately
		//Add one extra to name length because we want one extra space between name and cash
		int nameLength = players[0].name.length();
		for(int i=1; i<PLAYER_COUNT; i++)
			nameLength = Math.max(nameLength,players[i].name.length());
		nameLength ++;
		//And ignore the negative sign if there is one
		int moneyLength = String.valueOf(Math.abs(players[0].money)).length();
		for(int i=1; i<PLAYER_COUNT; i++)
			moneyLength = Math.max(moneyLength, String.valueOf(Math.abs(players[i].money)).length());
		//Do we need to worry about negatives?
		boolean negativeExists = false;
		for(int i=0; i<PLAYER_COUNT;i++)
			if(players[i].money<0)
			{
				negativeExists = true;
				break;
			}
		//Make a little extra room for the commas
		moneyLength += (moneyLength-1)/3;
		//Then start printing - including pointer if currently their turn
		for(int i=0; i<PLAYER_COUNT; i++)
		{
			if(currentTurn == i)
				board.append("> ");
			else
				board.append("  ");
			board.append(String.format("%-"+nameLength+"s",players[i].name));
			//Now figure out if we need a negative sign, a space, or neither
			if(players[i].money<0)
				board.append("-");
			else if(negativeExists)
				board.append(" ");
			//Then print the money itself
			board.append("$");
			board.append(String.format("%,"+moneyLength+"d",Math.abs(players[i].money)));
			//Now the booster display
			board.append(" [");
			board.append(String.format("%03d",players[i].booster));
			board.append("%]\n");
		}
		//Close it off and print it out
		board.append("```");
		channel.sendMessage(board.toString()).queue();
	}
	static void saveData()
	{
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
			//Replace the records of the players if they're there, otherwise add them
			for(int i=0; i<PLAYER_COUNT; i++)
			{
				int location = findUserInList(list,players[i].uID,false);
				String toPrint = players[i].uID+":"+players[i].name+":"+players[i].money
						+":"+players[0].booster+":"+players[0].winstreak;
				if(location == -1)
					list.add(toPrint);
				else
					list.set(location,toPrint);
			}
			//Then sort and rewrite it
			DescendingScoreSorter sorter = new DescendingScoreSorter();
			list.sort(sorter);
			Path file = Paths.get("scores.csv");
			Path fileOld = Paths.get("scoresOld.csv");
			Files.delete(fileOld);
			Files.copy(file,fileOld);
			Files.delete(file);
			Files.write(file, list);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static int findUserInList(List<String> list, String userID, boolean isName)
	{
		int field;
		if(isName)
			field = 1;
		else
			field = 0;
		/*
		 * record format:
		 * record[0] = uID
		 * record[1] = name
		 * record[2] = money
		 * record[3] = booster
		 * record[4] = winstreak
		 */
		String[] record;
		for(int i=0; i<list.size(); i++)
		{
			record = list.get(i).split(":");
			if(record[field].equals(userID))
				return i;
		}
		return -1;
	}
}
