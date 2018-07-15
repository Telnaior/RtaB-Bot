package tel.discord.rtab;

import java.util.concurrent.TimeUnit;

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
	static User playerA = null;
	static User playerB = null;
	static User currentPlayer = null;
	public static int playersJoined = 0;
	public static int gameStatus = 0;
	static boolean[] pickedNumbers = new boolean[boardSize];
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
		pickedNumbers = new boolean[15];
		bombs = new boolean[15];
	}
	/*
	 * addPlayer - adds a player to the game.
	 * MessageChannel channelID - channel the request took place in (only used to know where to send game details to)
	 * String playerID - ID of player to be added.
	 * Returns an enum which gives the result of the join attempt.
	 */
	public static PlayerJoinReturnValue addPlayer(MessageChannel channelID, User playerID)
	{
		if(!(playerID.equals(playerA)||playerID.equals(playerB)))
		{
			switch(playersJoined)
			{
			case 0:
				channel = channelID;
				playerA = playerID;
				playersJoined++;
				channel.sendMessage(playerA.getName() + " joined the game. One more player is required.").queue();
				return PlayerJoinReturnValue.JOINED1;
			case 1:
				playerB = playerID;
				playersJoined++;
				channel.sendMessage(playerB.getName() + " joined the game. The game is now starting. Please PM bombs within the next 60 seconds.").queue();
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
		if(playerID == playerA)
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
		else if(playerID == playerB)
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
		playerA.openPrivateChannel().queue(
				(channel) -> channel.sendMessage("Please PM your bomb by sending a number 1-" + boardSize).queue());
		playerB.openPrivateChannel().queue(
				(channel) -> channel.sendMessage("Please PM your bomb by sending a number 1-" + boardSize).queue());
		//Wait for bombs to return
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Check if right player, and valid bomb pick
				e -> (e.getAuthor().equals(playerA) && checkValidNumber(e.getMessage().getContentRaw())),
				//Parse it and update the bomb board
				e -> 
				{
					bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
					playerA.openPrivateChannel().queue(
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
				e -> (e.getAuthor().equals(playerB) && checkValidNumber(e.getMessage().getContentRaw())),
				//Parse it and update the bomb board
				e -> 
				{
					bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
					playerB.openPrivateChannel().queue(
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
		channel.sendMessage(currentPlayer.getName() + ", your turn. Choose a space on the board.")
			.completeAfter(5,TimeUnit.SECONDS);
		//Build up board display
		String board = "```\n";
		for(int i=0; i<boardSize; i++)
		{
			if(pickedNumbers[i])
			{
				board = board.concat("  ");
			}
			else
			{
				board = board.concat(String.format("%02d",(i+1)));
			}
			if(i%5==4)
				board = board.concat("\n");
			else
				board = board.concat(" ");
		}
		board = board.concat("```");
		System.out.println(board);
		channel.sendMessage(board).queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
				//Right player and channel
				e ->
				{
					if(e.getAuthor().equals(currentPlayer) && e.getChannel().equals(channel)
							&& checkValidNumber(e.getMessage().getContentRaw()))
					{
							int location = Integer.parseInt(e.getMessage().getContentRaw());
							if(pickedNumbers[location-1])
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
					pickedNumbers[location] = true;
					channel.sendMessage("Space " + (location+1) + " selected...").completeAfter(1,TimeUnit.SECONDS);
					channel.sendMessage("...").completeAfter(3,TimeUnit.SECONDS);
					if(bombs[location])
					{
						channel.sendMessage("**BOOM**").completeAfter(3,TimeUnit.SECONDS);
						gameStatus = 4;
					}
					else
					{
						channel.sendMessage("SAFE").completeAfter(3,TimeUnit.SECONDS);
					}
					if(currentPlayer.equals(playerA))
						currentPlayer = playerB;
					else
						currentPlayer = playerA;
					if(gameStatus == 4)
					{
						channel.sendMessage("Game Over. " + currentPlayer.getName() + " Wins!").completeAfter(5,TimeUnit.SECONDS);
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
}