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
	static final int boardSize = 15;
	static MessageChannel channel = null;
	static Player playerA = null;
	static Player playerB = null;
	static Player currentPlayer = null;
	public static int playersJoined = 0;
	public static int gameStatus = 0;
	static boolean[] pickedSpaces = new boolean[boardSize];
	static int spacesLeft = boardSize;
	static boolean[] bombs = new boolean[boardSize];
	public static EventWaiter waiter;
	/*
	 * reset - (re)initialises the game state by removing all players and clearing the board.
	 */
	public static void reset()
	{
		channel = null;
		playerA = null;
		playerB = null;
		currentPlayer = null;
		playersJoined = 0;
		gameStatus = 0;
		pickedSpaces = new boolean[15];
		spacesLeft = boardSize;
		bombs = new boolean[15];
	}
	/*
	 * addPlayer - adds a player to the game.
	 * MessageChannel channelID - channel the request took place in (only used to know where to send game details to)
	 * String playerID - ID of player to be added.
	 * Returns an enum which gives the result of the join attempt.
	 */
	public static PlayerJoinReturnValue addPlayer(MessageChannel channelID, Member playerID)
	{
		if(!((playerA != null && playerID.getUser().equals(playerA.user))||(playerB != null && playerID.getUser().equals(playerB.user))))
		{
			switch(playersJoined)
			{
			case 0:
				channel = channelID;
				playerA = new Player(playerID);
				playersJoined++;
				channel.sendMessage(playerA.name + " joined the game. One more player is required.").queue();
				return PlayerJoinReturnValue.JOINED1;
			case 1:
				playerB = new Player(playerID);
				playersJoined++;
				channel.sendMessage(playerB.name + " joined the game. The game is now starting. Please PM bombs within the next 60 seconds.").queue();
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
		if(playerA != null && playerID == playerA.user)
		{
			playerA = null;
			playersJoined--;
			switch(playersJoined)
			{
			case 0:
				reset();
				break;
			case 1:
				playerA = playerB;
				playerB = null;
				break;
			default:
				return PlayerQuitReturnValue.UNEXPECTEDPLAYERCOUNT;
			}
			return PlayerQuitReturnValue.SUCCESS;
		}
		else if(playerB != null && playerID == playerB.user)
		{
			playerB = null;
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
		playerA.user.openPrivateChannel().queue(
				(channel) -> channel.sendMessage("Please PM your bomb by sending a number 1-" + boardSize).queue());
		playerB.user.openPrivateChannel().queue(
				(channel) -> channel.sendMessage("Please PM your bomb by sending a number 1-" + boardSize).queue());
		//Wait for bombs to return
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Check if right player, and valid bomb pick
				e -> (e.getAuthor().equals(playerA.user) && checkValidNumber(e.getMessage().getContentRaw())),
				//Parse it and update the bomb board
				e -> 
				{
					bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
					playerA.user.openPrivateChannel().queue(
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
				e -> (e.getAuthor().equals(playerB.user) && checkValidNumber(e.getMessage().getContentRaw())),
				//Parse it and update the bomb board
				e -> 
				{
					bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
					playerB.user.openPrivateChannel().queue(
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
			if(Math.random() < 0.5)
				currentPlayer = playerA;
			else
				currentPlayer = playerB;
			channel.sendMessage("Let's go!").queue();
			runTurn();
		}
	}
	static void runTurn()
	{
		channel.sendMessage(currentPlayer.user.getAsMention() + ", your turn. Choose a space on the board.")
			.completeAfter(3,TimeUnit.SECONDS);
		displayBoardAndStatus();
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Right player and channel
				e ->
				{
					if(e.getAuthor().equals(currentPlayer.user) && e.getChannel().equals(channel)
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
							channel.sendMessage("...").completeAfter(3,TimeUnit.SECONDS);
						channel.sendMessage("**BOOM**").completeAfter(3,TimeUnit.SECONDS);
						channel.sendMessage(currentPlayer.user.getAsMention() +
								" loses $250,000 as penalty for blowing up.").queue();
						currentPlayer.money -= 250000;
						gameStatus = 4;
					}
					else
					{
						if((Math.random()*spacesLeft)<1)
							channel.sendMessage("...").completeAfter(3,TimeUnit.SECONDS);
						channel.sendMessage("**$100,000**").completeAfter(3,TimeUnit.SECONDS);
						currentPlayer.money += 100000;
					}
					if(currentPlayer.user.equals(playerA.user))
					{
						playerA.money = currentPlayer.money;
						currentPlayer = playerB;
					}
					else
					{
						playerB.money = currentPlayer.money;
						currentPlayer = playerA;
					}
					if(gameStatus == 4)
					{
						channel.sendMessage("Game Over. " + currentPlayer.user.getAsMention() + " Wins!")
							.completeAfter(3,TimeUnit.SECONDS);
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
			return (location > 0 && location <= boardSize);
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
		for(int i=0; i<boardSize; i++)
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
		String nameA = playerA.name;
		String nameB = playerB.name;
		int moneyA = playerA.money;
		int moneyB = playerB.money;
		//Add one extra to name length because we want one extra space between name and cash
		int nameLength = Math.max(nameA.length(),nameB.length())+1;
		//And ignore the negative sign if there is one
		int moneyLength = Math.max(String.valueOf(Math.abs(moneyA)).length(),String.valueOf(Math.abs(moneyB)).length());
		//Make a little extra room for the commas
		moneyLength += (moneyLength-1)/3;
		//Then start printing, Player A first - including pointer if currently their turn
		if(currentPlayer.equals(playerA))
			board.append("> ");
		else
			board.append("  ");
		board.append(String.format("%-"+nameLength+"s",nameA));
		//Now figure out if we need a negative sign, a space, or neither
		if(moneyA<0)
			board.append("-");
		else if(moneyB<0)
			board.append(" ");
		//Then print the money itself
		board.append("$");
		board.append(String.format("%,"+moneyLength+"d",Math.abs(moneyA)));
		board.append("\n");
		//Do the whole thing again for Player B!
		if(currentPlayer.equals(playerB))
			board.append("> ");
		else
			board.append("  ");
		board.append(String.format("%-"+nameLength+"s",nameB));
		//Now figure out if we need a negative sign, a space, or neither
		if(moneyB<0)
			board.append("-");
		else if(moneyA<0)
			board.append(" ");
		//Then print the money itself
		board.append("$");
		board.append(String.format("%,"+moneyLength+"d",Math.abs(moneyB)));
		board.append("\n");
		//Close it off and print it out
		board.append("```");
		channel.sendMessage(board.toString()).queue();
	}
	static void saveData()
	{
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
			String[] record;
			boolean aFound = false;
			boolean bFound = false;
			//Replace the records of the players if they're there, otherwise add them
			for(int i=0; i<list.size(); i++)
			{
				/*
				 * record format:
				 * record[0] = uID
				 * record[1] = name
				 * record[2] = money
				 */
				record = list.get(i).split(":");
				if(record[0].equals(playerA.uID))
				{
					list.set(i,playerA.uID+":"+playerA.name+":"+playerA.money);
					aFound = true;
				}
				else if(record[0].equals(playerB.uID))
				{
					list.set(i,playerB.uID+":"+playerB.name+":"+playerB.money);
					bFound = true;
				}
				//No need to continue if we've already found them both
				if(aFound && bFound)
					break;
			}
			//Add them to the end if they haven't been found
			if(!aFound)
				list.add(playerA.uID+":"+playerA.name+":"+playerA.money);
			if(!bFound)
				list.add(playerB.uID+":"+playerB.name+":"+playerB.money);
			//Then rewrite it
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
}