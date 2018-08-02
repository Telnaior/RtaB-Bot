package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import tel.discord.rtab.enums.BlammoChoices;
import tel.discord.rtab.enums.BombType;
import tel.discord.rtab.enums.Events;
import tel.discord.rtab.enums.GameBot;
import tel.discord.rtab.enums.GameStatus;
import tel.discord.rtab.enums.Games;
import tel.discord.rtab.enums.MoneyMultipliersToUse;
import tel.discord.rtab.enums.PlayerJoinReturnValue;
import tel.discord.rtab.enums.PlayerQuitReturnValue;
import tel.discord.rtab.enums.PlayerStatus;
import tel.discord.rtab.enums.SpaceType;
import tel.discord.rtab.minigames.MiniGame;
import tel.discord.rtab.minigames.SuperBonusRound;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

public class GameController
{
	final static int MAX_PLAYERS = 16;
	public TextChannel channel;
	TextChannel resultChannel;
	int boardSize = 15;
	List<Player> players = new ArrayList<>();
	List<Player> winners = new ArrayList<>();
	int currentTurn = -1;
	int repeatTurn = 0;
	public int playersJoined = 0;
	int playersAlive = 0;
	ListIterator<Games> gamesToPlay;
	public GameStatus gameStatus = GameStatus.SIGNUPS_OPEN;
	boolean[] pickedSpaces;
	int spacesLeft;
	boolean[] bombs;
	Board gameboard;
	public static EventWaiter waiter;
	public Timer timer = new Timer();
	Message waitingMessage;

	private class StartGameTask extends TimerTask
	{
		@Override
		public void run()
		{
			startTheGameAlready();
		}
	}
	private class FinalCallTask extends TimerTask
	{
		@Override
		public void run()
		{
			channel.sendMessage("Thirty seconds before game starts!").queue();
			channel.sendMessage(listPlayers(false)).queue();
		}
	}
	private class ClearMinigameQueueTask extends TimerTask
	{
		@Override
		public void run()
		{
			prepareNextMiniGame();
		}
	}
	private class PickSpaceWarning extends TimerTask
	{
		@Override
		public void run()
		{
			channel.sendMessage(players.get(currentTurn).getSafeMention() + 
					", thirty seconds left to choose a space!").queue();
			displayBoardAndStatus(true,false,false);
		}
	}
	private class MiniGameWarning extends TimerTask
	{
		@Override
		public void run()
		{
			channel.sendMessage(players.get(currentTurn).getSafeMention() + 
					", are you still there? One minute left!").queue();
		}
	}
	private class WaitForNextTurn extends TimerTask
	{
		@Override
		public void run()
		{
			runTurn();
		}
	}
	private class WaitForEndGame extends TimerTask
	{
		@Override
		public void run()
		{
			runNextEndGamePlayer();
		}
	}
	private class RevealTheSBR extends TimerTask
	{
		@Override
		public void run()
		{
			channel.sendMessage(players.get(0).getSafeMention() + "...").complete();
			channel.sendMessage("It is time to enter the Super Bonus Round.").completeAfter(5,TimeUnit.SECONDS);
			channel.sendMessage("...").completeAfter(10,TimeUnit.SECONDS);
			startMiniGame(new SuperBonusRound());
		}
	}
	private class PickSpace extends TimerTask
	{
		final int location;
		private PickSpace(int space)
		{
			location = space;
		}
		@Override
		public void run()
		{
			resolveTurn(location);
		}
	}
	
	public GameController(TextChannel channelID)
	{
		channel = channelID;
	}
	
	void setResultChannel(TextChannel channelID)
	{
		resultChannel = channelID;
	}
	
	/*
	 * reset - (re)initialises the game state by removing all players and clearing the board.
	 */
	public void reset()
	{
		players.clear();
		currentTurn = -1;
		playersJoined = 0;
		playersAlive = 0;
		gameStatus = GameStatus.SIGNUPS_OPEN;
		gameboard = null;
		repeatTurn = 0;
		timer.cancel();
		timer = new Timer();
	}
	/*
	 * addPlayer - adds a player to the game, or updates their name if they're already in.
	 * MessageChannel channelID - channel the request took place in (only used to know where to send game details to)
	 * String playerID - ID of player to be added.
	 * Returns an enum which gives the result of the join attempt.
	 */
	public PlayerJoinReturnValue addPlayer(Member playerID)
	{
		//Make sure game isn't already running
		if(gameStatus != GameStatus.SIGNUPS_OPEN)
			return PlayerJoinReturnValue.INPROGRESS;
		//Watch out for too many players
		if(playersJoined >= MAX_PLAYERS)
			return PlayerJoinReturnValue.TOOMANYPLAYERS;
		//Create player object
		Player newPlayer = new Player(playerID,channel);
		if(newPlayer.name.contains(":") || newPlayer.name.contains("#") || newPlayer.name.startsWith("!"))
			return PlayerJoinReturnValue.BADNAME;
		//If they're out of lives, remind them of the risk
		if(newPlayer.lives <= 0 && newPlayer.newbieProtection <= 0)
		{
			channel.sendMessage(newPlayer.getSafeMention() + ", you are out of lives. "
					+ "Your gains for the rest of the day will be reduced by 80%.").queue();
		}
		//Dumb easter egg
		if(newPlayer.money <= -1000000000)
			return PlayerJoinReturnValue.ELIMINATED;
		//Look for match already in player list
		for(int i=0; i<playersJoined; i++)
		{
			if(players.get(i).uID.equals(newPlayer.uID))
			{
				//Found them, check if we should update their name or just laugh at them
				if(players.get(i).name == newPlayer.name)
					return PlayerJoinReturnValue.ALREADYIN;
				else
				{
					players.set(i,newPlayer);
					return PlayerJoinReturnValue.UPDATED;
				}
			}
		}
		//Haven't found one, add them to the list
		players.add(newPlayer);
		playersJoined++;
		if(newPlayer.money > 900000000)
		{
			channel.sendMessage(String.format("%1$s needs only $%2$,d more to reach the goal!",
					newPlayer.name,(1000000000-newPlayer.money)));
		}
		if(playersJoined == 1)
		{
			timer.schedule(new FinalCallTask(),  90000);
			timer.schedule(new StartGameTask(), 120000);
			return PlayerJoinReturnValue.CREATED;
		}
		else
			return PlayerJoinReturnValue.JOINED;
	}
	/*
	 * removePlayer - removes a player from the game.
	 * MessageChannel channelID - channel the request was registered in.
	 * String playerID - ID of player to be removed.
	 */
	public PlayerQuitReturnValue removePlayer(MessageChannel channelID, User playerID)
	{
		//Make sure game isn't running, too late to quit now
		if(gameStatus != GameStatus.SIGNUPS_OPEN)
			return PlayerQuitReturnValue.GAMEINPROGRESS;
		//Search for player
		for(int i=0; i<playersJoined; i++)
		{
			if(players.get(i).uID.equals(playerID.getId()))
			{
				//Found them, get rid of them and call it a success
				players.remove(i);
				playersJoined --;
				//Abort the game if everyone left
				if(playersJoined == 0)
					reset();
				return PlayerQuitReturnValue.SUCCESS;
			}
		}
		//Didn't find them, why are they trying to quit in the first place?
		return PlayerQuitReturnValue.NOTINGAME;
	}
	/*
	 * startTheGameAlready - prompts for players to choose bombs.
	 */
	public void startTheGameAlready()
	{
		//If the game's already running or no one's in it, just don't
		if((gameStatus != GameStatus.SIGNUPS_OPEN && gameStatus != GameStatus.ADD_BOT_QUESTION) || playersJoined < 1)
		{
			return;
		}
		if(playersJoined == 1)
		{
			//Didn't get players? How about a bot?
			channel.sendMessage(players.get(0).getSafeMention()+", would you like to play against a bot? (Y/N)").queue();
			gameStatus = GameStatus.ADD_BOT_QUESTION;
			waiter.waitForEvent(MessageReceivedEvent.class,
					//Right player and channel
					e ->
					{
						if(e.getAuthor().equals(players.get(0).user) && e.getChannel().equals(channel))
						{
							String firstLetter = e.getMessage().getContentRaw().toUpperCase().substring(0,1);
							return(firstLetter.startsWith("Y") || firstLetter.startsWith("N"));
						}
						return false;
					},
					//Parse it and call the method that does stuff
					e -> 
					{
						if(e.getMessage().getContentRaw().toUpperCase().startsWith("Y"))
						{
							addRandomBot();
							startTheGameAlready();
						}
						else
						{
							channel.sendMessage("Very well. Game aborted.").queue();
							reset();
						}
					},
					30,TimeUnit.SECONDS, () ->
					{
						channel.sendMessage("Game aborted.").queue();
						reset();
					});
			return;
		}
		//Declare game in progress so we don't get latecomers
		channel.sendMessage("Starting game...").queue();
		gameStatus = GameStatus.IN_PROGRESS;
		//Initialise stuff that needs initialising
		boardSize = 5 + (5*playersJoined);
		spacesLeft = boardSize;
		pickedSpaces = new boolean[boardSize];
		bombs = new boolean[boardSize];
		//Get the "waiting on" message going
		waitingMessage = channel.sendMessage(listPlayers(true)).complete();
		//Request players send in bombs, and set up waiter for them to return
		for(int i=0; i<playersJoined; i++)
		{
			final int iInner = i;
			if(players.get(iInner).isBot)
			{
				int bombPosition = (int) (Math.random() * boardSize);
				players.get(iInner).knownBombs.add(bombPosition);
				bombs[bombPosition] = true;
				players.get(iInner).status = PlayerStatus.ALIVE;
				playersAlive ++;
			}
			else
			{
				players.get(iInner).user.openPrivateChannel().queue(
						(channel) -> channel.sendMessage("Please place your bomb within the next 60 seconds "
								+ "by sending a number 1-" + boardSize).queue());
				waiter.waitForEvent(MessageReceivedEvent.class,
						//Check if right player, and valid bomb pick
						e -> (e.getAuthor().equals(players.get(iInner).user)
								&& checkValidNumber(e.getMessage().getContentRaw())),
						//Parse it and update the bomb board
						e -> 
						{
							bombs[Integer.parseInt(e.getMessage().getContentRaw())-1] = true;
							players.get(iInner).knownBombs.add(Integer.parseInt(e.getMessage().getContentRaw())-1);
							players.get(iInner).user.openPrivateChannel().queue(
									(channel) -> channel.sendMessage("Bomb placement confirmed.").queue());
							players.get(iInner).status = PlayerStatus.ALIVE;
							playersAlive ++;
							checkReady();
						},
						//Or timeout after a minute
						1, TimeUnit.MINUTES, () ->
						{
							gameStatus = GameStatus.SIGNUPS_OPEN;
							checkReady();
						});
			}
			checkReady();
		}

	}
	void checkReady()
	{
		if(gameStatus == GameStatus.SIGNUPS_OPEN)
		{
			channel.sendMessage("Bomb placement timed out. Game aborted.").queue();
			reset();
		}
		else
		{
			//If everyone has sent in, what are we waiting for?
			if(playersAlive == playersJoined)
			{
				//Delete the "waiting on" message
				waitingMessage.delete().queue();
				//Determine first player and player order
				currentTurn = (int)(Math.random()*playersJoined);
				gameboard = new Board(boardSize);
				Collections.shuffle(players);
				//Let's get things rolling!
				channel.sendMessage("Let's go!").queue();
				runTurn();
			}
			//If they haven't, update the message to tell us who we're still waiting on
			else
			{
				waitingMessage.editMessage(listPlayers(true)).queue();
			}
		}
	}
	void runTurn()
	{
		if(repeatTurn > 0)
		{
			repeatTurn --;
			if(!(players.get(currentTurn).isBot))
				channel.sendMessage(players.get(currentTurn).getSafeMention() + ", pick again.")
					.completeAfter(3,TimeUnit.SECONDS);
		}
		else if(repeatTurn == 0 && !players.get(currentTurn).isBot)
		{
			channel.sendMessage(players.get(currentTurn).getSafeMention() + ", your turn. Choose a space on the board.")
				.completeAfter(3,TimeUnit.SECONDS);
		}
		displayBoardAndStatus(true, false, false);
		if(players.get(currentTurn).isBot)
		{
			//Get safe spaces, starting with all unpicked spaces
			ArrayList<Integer> openSpaces = new ArrayList<>(boardSize);
			for(int i=0; i<boardSize; i++)
				if(!pickedSpaces[i])
					openSpaces.add(i);
			//Remove all known bombs
			ArrayList<Integer> safeSpaces = new ArrayList<>(boardSize);
			safeSpaces.addAll(openSpaces);
			for(Integer bomb : players.get(currentTurn).knownBombs)
				safeSpaces.remove(bomb);
			//If there's any pick one at random and resolve it
			if(safeSpaces.size() > 0)
				resolveTurn(safeSpaces.get((int)(Math.random()*safeSpaces.size())));
			//Otherwise it sucks to be you, bot, eat bomb!
			else
				resolveTurn(openSpaces.get((int)(Math.random()*openSpaces.size())));
		}
		else
		{
			TimerTask warnPlayer = new PickSpaceWarning();
			timer.schedule(warnPlayer, 60000);
			waiter.waitForEvent(MessageReceivedEvent.class,
					//Right player and channel
					e ->
					{
						if(e.getAuthor().equals(players.get(currentTurn).user) && e.getChannel().equals(channel)
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
					//Parse it and call the method that does stuff
					e -> 
					{
						warnPlayer.cancel();
						int location = Integer.parseInt(e.getMessage().getContentRaw())-1;
						//If they just picked their own bomb, what do they think they're doing?
						if(players.get(currentTurn).knownBombs.contains(location) 
								&& spacesLeft > players.get(currentTurn).knownBombs.size())
						{
							System.out.println(Instant.now().toString() + " - " + players.get(currentTurn).name + 
									": known bomb pick, " + spacesLeft + " spaces left.");
						}
						//Anyway go play out their turn
						timer.schedule(new PickSpace(location),1000);
					},
					90,TimeUnit.SECONDS, () ->
					{
						timeOutTurn();
					});
		}
	}
	private void timeOutTurn()
	{
		//If they haven't been warned, play nice and just pick a random space for them
		if(!players.get(currentTurn).warned)
		{
			players.get(currentTurn).warned = true;
			channel.sendMessage(players.get(currentTurn).getSafeMention() + 
					" is out of time. Wasting a random space.").queue();
			//Get unpicked spaces
			ArrayList<Integer> spaceCandidates = new ArrayList<>(boardSize);
			for(int i=0; i<boardSize; i++)
				if(!pickedSpaces[i])
					spaceCandidates.add(i);
			//Pick one at random
			int spaceChosen = spaceCandidates.get((int) (Math.random() * spaceCandidates.size()));
			//If it's a bomb, it sucks to be them
			if(bombs[spaceChosen])
			{
				resolveTurn(spaceChosen);
			}
			//If it isn't, throw out the space and let the players know what's up
			else
			{
				pickedSpaces[spaceChosen] = true;
				spacesLeft --;
				channel.sendMessage("Space " + (spaceChosen+1) + " selected...").completeAfter(1,TimeUnit.SECONDS);
				//Don't forget the threshold
				if(players.get(currentTurn).threshold)
				{
					channel.sendMessage("(-$50,000)").queueAfter(1,TimeUnit.SECONDS);
					players.get(currentTurn).addMoney(-50000,MoneyMultipliersToUse.NOTHING);
				}
				channel.sendMessage("It's not a bomb, so its contents are lost.").completeAfter(5,TimeUnit.SECONDS);
				runEndTurnLogic();
			}
		}
		//If they've been warned, it's time to BLOW STUFF UP!
		else
		{
			channel.sendMessage(players.get(currentTurn).getSafeMention() + 
					" is out of time. Eliminating them.").queue();
			//Jokers? GET OUT OF HERE!
			players.get(currentTurn).jokers = 0;
			//Look for a bomb
			ArrayList<Integer> bombCandidates = new ArrayList<>(boardSize);
			for(int i=0; i<boardSize; i++)
				if(bombs[i] && !pickedSpaces[i])
					bombCandidates.add(i);
			int bombChosen;
			//Got bomb? Pick one to detonate
			if(bombCandidates.size() > 0)
			{
				bombChosen = (int) (Math.random() * bombCandidates.size());
			}
			//No bomb? WHO CARES, THIS IS RACE TO A BILLION, WE'RE BLOWING THEM UP ANYWAY!
			else
			{
				//Get unpicked spaces
				ArrayList<Integer> spaceCandidates = new ArrayList<>(boardSize);
				for(int i=0; i<boardSize; i++)
					if(!pickedSpaces[i])
						spaceCandidates.add(i);
				//Pick one and turn it into a BOMB
				bombChosen = (int) (Math.random() * spaceCandidates.size());
				bombs[bombChosen] = true;
			}
			//NO DUDS ALLOWED
			if(gameboard.bombBoard[bombChosen] == BombType.DUD)
				gameboard.bombBoard[bombChosen] = BombType.NORMAL;
			//KABOOM KABOOM KABOOM KABOOM
			resolveTurn(bombCandidates.get(bombChosen));
		}
	}
	void resolveTurn(int location)
	{
		pickedSpaces[location] = true;
		spacesLeft--;
		if(players.get(currentTurn).isBot)
		{
			channel.sendMessage(players.get(currentTurn).name + " selects space " + (location+1) + "...")
				.complete();
		}
		else
		{
			channel.sendMessage("Space " + (location+1) + " selected...").completeAfter(1,TimeUnit.SECONDS);
		}
		if(players.get(currentTurn).threshold)
		{
			players.get(currentTurn).addMoney(-50000,MoneyMultipliersToUse.NOTHING);
			channel.sendMessage("(-$50,000)").queueAfter(1,TimeUnit.SECONDS);
		}
		if(bombs[location])
		{
			runBombLogic(location);
		}
		else
		{
			runSafeLogic(location);
		}
	}
	void runEndTurnLogic()
	{
		//Test if game over
		if(spacesLeft <= 0 || playersAlive == 1)
		{
			gameStatus = GameStatus.END_GAME;
			if(spacesLeft < 0)
				channel.sendMessage("An error has occurred, ending the game, @Atia#2084 fix pls").queue();
			channel.sendMessage("Game Over.").completeAfter(3,TimeUnit.SECONDS);
			timer.schedule(new WaitForEndGame(), 1000);
		}
		else
		{
			//Advance turn to next player if there isn't a repeat going
			if(repeatTurn == 0)
				advanceTurn(false);
			timer.schedule(new WaitForNextTurn(), 1000);
		}
	}
	void runBombLogic(int location)
	{
		channel.sendMessage("...").completeAfter(5,TimeUnit.SECONDS);
		channel.sendMessage("It's a **BOMB**.").completeAfter(5,TimeUnit.SECONDS);
		//If player has a joker, force it to not explode
		//This is a really ugly way of doing it though
		if(players.get(currentTurn).jokers > 0)
		{
			channel.sendMessage("But you have a joker!").queueAfter(2,TimeUnit.SECONDS);
			players.get(currentTurn).jokers --;
			gameboard.bombBoard[location] = BombType.DUD;
		}
		else if(playersJoined == 2 && gameboard.bombBoard[location] == BombType.DUD)
		{
			//No duds in 2p, but jokers still override that
			gameboard.bombBoard[location] = BombType.NORMAL;
		}
		//But is it a special bomb?
		StringBuilder extraResult = null;
		int penalty = Player.BOMB_PENALTY;
		if(players.get(currentTurn).newbieProtection > 0)
			penalty = Player.NEWBIE_BOMB_PENALTY;
		switch(gameboard.bombBoard[location])
		{
		case NORMAL:
			channel.sendMessage(String.format("It goes **BOOM**. $%,d lost as penalty.",Math.abs(penalty)))
				.completeAfter(5,TimeUnit.SECONDS);
			extraResult = players.get(currentTurn).blowUp(1,false);
			break;
		case BANKRUPT:
			int amountLost = players.get(currentTurn).bankrupt();
			if(amountLost == 0)
			{
				channel.sendMessage(String.format("It goes **BOOM**. $%,d lost as penalty.",Math.abs(penalty)))
					.completeAfter(5,TimeUnit.SECONDS);
			}
			else
			{
				channel.sendMessage("It goes **BOOM**...")
						.completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("It also goes **BANKRUPT**. _\\*whoosh*_")
						.completeAfter(5,TimeUnit.SECONDS);
				if(amountLost < 0)
				{
					channel.sendMessage(String.format("**$%1$,d** *returned*, plus $%2$,d penalty.",
							Math.abs(amountLost),Math.abs(penalty)))
							.completeAfter(3,TimeUnit.SECONDS);
				}
				else
				{
					channel.sendMessage(String.format("**$%1$,d** lost, plus $%2$,d penalty.",
							amountLost,Math.abs(penalty)))
							.completeAfter(3,TimeUnit.SECONDS);
				}
			}
			extraResult = players.get(currentTurn).blowUp(1,false);
			break;
		case BOOSTHOLD:
			StringBuilder resultString = new StringBuilder().append("It ");
			if(players.get(currentTurn).booster != 100)
				resultString.append("holds your boost, then ");
			resultString.append(String.format("goes **BOOM**. $%,d lost as penalty.",Math.abs(penalty)));
			channel.sendMessage(resultString)
					.completeAfter(5,TimeUnit.SECONDS);
			extraResult = players.get(currentTurn).blowUp(1,true);
			break;
		case CHAIN:
			channel.sendMessage("It goes **BOOM**...")
					.completeAfter(5,TimeUnit.SECONDS);
			int chain = 1;
			do
			{
				chain *= 2;
				StringBuilder nextLevel = new StringBuilder();
				nextLevel.append("**");
				for(int i=0; i<chain; i++)
				{
					nextLevel.append("BOOM");
					if(i+1 < chain)
						nextLevel.append(" ");
				}
				nextLevel.append("**");
				if(chain < 8)
					nextLevel.append("...");
				else
					nextLevel.append("!!!");
				channel.sendMessage(nextLevel).completeAfter(5,TimeUnit.SECONDS);
			}
			while(Math.random() * chain < 1);
			channel.sendMessage(String.format("**$%,d** penalty!",Math.abs(chain*penalty)))
					.completeAfter(5,TimeUnit.SECONDS);
			extraResult = players.get(currentTurn).blowUp(chain,false);
			break;
		case DUD:
			channel.sendMessage("It goes _\\*fizzle*_.")
				.completeAfter(5,TimeUnit.SECONDS);
			break;
		}
		if(extraResult != null)
			channel.sendMessage(extraResult).queue();
		runEndTurnLogic();
	}
	void runSafeLogic(int location)
	{
		//Always trigger it on a blammo, otherwise based on spaces left and players in game
		if((Math.random()*spacesLeft)<playersJoined || gameboard.typeBoard[location] == SpaceType.BLAMMO)
			channel.sendMessage("...").completeAfter(5,TimeUnit.SECONDS);
		//Figure out what space we got
		StringBuilder resultString = new StringBuilder();
		StringBuilder extraResult = null;
		switch(gameboard.typeBoard[location])
		{
		case CASH:
			//On cash, update the player's score and tell them how much they won
			int cashWon = gameboard.cashBoard[location].getValue();
			resultString.append("**");
			if(cashWon<0)
				resultString.append("-");
			resultString.append("$");
			resultString.append(String.format("%,d",Math.abs(cashWon)));
			resultString.append("**!");
			extraResult = players.get(currentTurn).addMoney(cashWon, MoneyMultipliersToUse.BOOSTER_ONLY);
			break;
		case BOOSTER:
			//On cash, update the player's booster and tell them what they found
			int boostFound = gameboard.boostBoard[location].getValue();
			resultString.append("A **" + String.format("%+d",boostFound) + "%** Booster");
			if(boostFound > 0)
				resultString.append("!");
			else
				resultString.append(".");
			players.get(currentTurn).addBooster(boostFound);
			break;
		case GAME:
			//On a game, announce it and add it to their game pile
			Games gameFound = gameboard.gameBoard[location];
			resultString.append("It's a minigame, **" + gameFound + "**!");
			players.get(currentTurn).games.add(gameFound);
			players.get(currentTurn).games.sort(null);
			break;
		case EVENT:
			activateEvent(gameboard.eventBoard[location],location);
			return;
		case BLAMMO:
			channel.sendMessage("It's a **BLAMMO!** Quick, press a button!").completeAfter(5,TimeUnit.SECONDS);
			channel.sendMessage("```\nBLAMMO\n 1  2 \n 3  4 \n```").queue();
			if(players.get(currentTurn).isBot)
			{
				runBlammo((int) (Math.random() * 4));
			}
			else
			{
				waiter.waitForEvent(MessageReceivedEvent.class,
						//Right player and channel
						e ->
						{
							return (e.getAuthor().equals(players.get(currentTurn).user) && e.getChannel().equals(channel)
									&& checkValidNumber(e.getMessage().getContentRaw()) 
											&& Integer.parseInt(e.getMessage().getContentRaw()) <= 4);
						},
						//Parse it and call the method that does stuff
						e -> 
						{
							int button = Integer.parseInt(e.getMessage().getContentRaw())-1;
							runBlammo(button);
						},
						30,TimeUnit.SECONDS, () ->
						{
							channel.sendMessage("Too slow, autopicking!").queue();
							int button = (int) Math.random() * 4;
							runBlammo(button);
						});
			}
			return;
		}
		channel.sendMessage(resultString).completeAfter(5,TimeUnit.SECONDS);
		if(extraResult != null)
			channel.sendMessage(extraResult).queue();
		runEndTurnLogic();
	}
	private void runBlammo(int buttonPressed)
	{
		//Yes I know it's generating the result after they've already picked
		//But that's the sort of thing a blammo would do so I'm fine with it
		List<BlammoChoices> buttons = Arrays.asList(BlammoChoices.values());
		Collections.shuffle(buttons);
		if(players.get(currentTurn).isBot)
		{
			channel.sendMessage(players.get(currentTurn).name + " presses button " + (buttonPressed+1) + "...").queue();
		}
		else
		{
			channel.sendMessage("Button " + (buttonPressed+1) + " pressed...").queue();
		}
		channel.sendMessage("...").completeAfter(3,TimeUnit.SECONDS);
		StringBuilder extraResult = null;
		int penalty = Player.BOMB_PENALTY;
		switch(buttons.get(buttonPressed))
		{
		case BLOCK:
			channel.sendMessage("You BLOCKED the BLAMMO!").completeAfter(3,TimeUnit.SECONDS);
			break;
		case ELIM_OPP:
			channel.sendMessage("You ELIMINATED YOUR OPPONENT!").completeAfter(3,TimeUnit.SECONDS);
			advanceTurn(false);
			if(players.get(currentTurn).newbieProtection > 0)
				penalty = Player.NEWBIE_BOMB_PENALTY;
			channel.sendMessage("Goodbye, " + players.get(currentTurn).getSafeMention()
					+ String.format("! $%,d penalty!",Math.abs(penalty*4))).queue();
			if(repeatTurn > 0)
				channel.sendMessage("(You also negated the repeat!)").queue();
			extraResult = players.get(currentTurn).blowUp(4,false);
			break;
		case THRESHOLD:
			if(players.get(currentTurn).threshold)
			{
				//You already have a threshold situation? Buh-bye.
				channel.sendMessage("It's a THRESHOLD SITUATION, but you're already in one, so...")
					.completeAfter(3,TimeUnit.SECONDS);
			}
			else
			{
				channel.sendMessage("You're entering a THRESHOLD SITUATION!").completeAfter(3,TimeUnit.SECONDS);
				channel.sendMessage("You'll lose $50,000 for every pick you make, "
						+ "and if you lose the penalty will be four times as large!").queue();
				players.get(currentTurn).threshold = true;
				break;
			}
		case ELIM_YOU:
			channel.sendMessage("You ELIMINATED YOURSELF!").completeAfter(3,TimeUnit.SECONDS);
			if(players.get(currentTurn).newbieProtection > 0)
				penalty = Player.NEWBIE_BOMB_PENALTY;
			channel.sendMessage(String.format("$%,d penalty!",Math.abs(penalty*4))).queue();
			extraResult = players.get(currentTurn).blowUp(4,false);
			break;
		}
		if(extraResult != null)
			channel.sendMessage(extraResult).queue();
		runEndTurnLogic();
	}
	void activateEvent(Events event, int location)
	{
		switch(event)
		{
		case BOOST_DRAIN:
			channel.sendMessage("It's a **Boost Drain**, which cuts your booster in half...")
				.completeAfter(5,TimeUnit.SECONDS);
			players.get(currentTurn).booster /= 2;
			if(players.get(currentTurn).booster < Player.MIN_BOOSTER)
				players.get(currentTurn).booster = Player.MIN_BOOSTER;
			break;
		case MINEFIELD:
			channel.sendMessage("Oh no, it's a **Minefield**! Adding up to " + playersJoined + " more bombs...")
				.completeAfter(5,TimeUnit.SECONDS);
			for(int i=0; i<playersJoined; i++)
				bombs[(int)(Math.random()*boardSize)] = true;
			break;
		case LOCKDOWN:
			channel.sendMessage("It's a **Lockdown**, all non-bomb spaces on the board are now becoming cash!")
				.completeAfter(5,TimeUnit.SECONDS);
			for(int i=0; i<boardSize; i++)
			{
				//Blammos aren't affected
				if(gameboard.typeBoard[i] != SpaceType.BLAMMO)
					gameboard.typeBoard[i] = SpaceType.CASH;
			}
			break;
		case STARMAN:
			channel.sendMessage("Hooray, it's a **Starman**, here to destroy all the bombs!")
				.completeAfter(5,TimeUnit.SECONDS);
			for(int i=0; i<boardSize; i++)
				if(bombs[i] && !pickedSpaces[i])
				{
					channel.sendMessage("Bomb in space " + (i+1) + " destroyed.")
						.queueAfter(2,TimeUnit.SECONDS);
					pickedSpaces[i] = true;
					spacesLeft --;
				}
			break;
		case REPEAT:
			channel.sendMessage("It's a **Repeat**, you need to pick two more spaces in a row!")
				.completeAfter(5,TimeUnit.SECONDS);
			repeatTurn += 2;
			break;
		case JOKER:
			channel.sendMessage("Congratulations, you found a **Joker**, protecting you from a single bomb!")
				.completeAfter(5,TimeUnit.SECONDS);
			players.get(currentTurn).jokers ++;
			break;
		case GAME_LOCK:
			if(!players.get(currentTurn).minigameLock)
			{
				channel.sendMessage("It's a **Minigame Lock**, you'll get to play any minigames you have even if you bomb!")
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).minigameLock = true;
				break;
			}
			else
			{
				channel.sendMessage("It's a **Minigame Lock**, but you already have one.")
					.completeAfter(5,TimeUnit.SECONDS);
				Games gameFound = gameboard.gameBoard[location];
				channel.sendMessage("Instead, let's give you a **" + gameFound + "** to use it with!")
					.completeAfter(3,TimeUnit.SECONDS);
				players.get(currentTurn).games.add(gameFound);
				players.get(currentTurn).games.sort(null);
				return;
			}
		case SPLIT_SHARE:
			if(!(players.get(currentTurn).splitAndShare))
			{
				channel.sendMessage("It's a **Split & Share**, "
						+ "don't lose the round now or you'll lose 10% of your total, "
						+ "approximately $" + String.format("%,d",(players.get(currentTurn).money/10)) + "!")
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).splitAndShare = true;
			}
			else
			{
				channel.sendMessage("It's a **Split & Share**, but you already have one...")
					.completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("Well then, how about we activate it~?").completeAfter(3,TimeUnit.SECONDS);
				players.get(currentTurn).blowUp(0,true);
			}
			break;
		case JACKPOT:
			if(!(players.get(currentTurn).jackpot))
			{
				channel.sendMessage("You found the $25,000,000 **JACKPOT**, win the round to claim it!")
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).jackpot = true;
			}
			else
			{
				channel.sendMessage("You found a **JACKPOT**, but you already have one!")
					.completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("So you can cash this one in right away!").completeAfter(3,TimeUnit.SECONDS);
				players.get(currentTurn).addMoney(25000000,MoneyMultipliersToUse.NOTHING);
			}
			break;
		case STREAKP1:
			players.get(currentTurn).winstreak += 1;
			channel.sendMessage(String.format("It's a **+1 Streak Bonus**, raising you to x%d!",
					players.get(currentTurn).winstreak))
				.completeAfter(5,TimeUnit.SECONDS);
			break;
		case STREAKP2:
			players.get(currentTurn).winstreak += 2;
			channel.sendMessage(String.format("It's a **+2 Streak Bonus**, raising you to x%d!",
					players.get(currentTurn).winstreak))
				.completeAfter(5,TimeUnit.SECONDS);
			break;
		case STREAKP3:
			players.get(currentTurn).winstreak += 3;
			channel.sendMessage(String.format("It's a **+3 Streak Bonus**, raising you to x%d!",
					players.get(currentTurn).winstreak))
				.completeAfter(5,TimeUnit.SECONDS);
			break;
		case BLAMMO_FRENZY:
			channel.sendMessage("It's a **Blammo Frenzy**, good luck!!")
				.completeAfter(5,TimeUnit.SECONDS);
			for(int i=0; i<boardSize; i++)
			{
				//Switch cash to blammo with 1/3 chance
				if(gameboard.typeBoard[i] == SpaceType.CASH && Math.random()*3 < 1)
					gameboard.typeBoard[i] = SpaceType.BLAMMO;
			}
			break;
		case COMMUNISM:
			channel.sendMessage("It's a **Bowser Revolution**, everyone's money is now equalised!")
				.completeAfter(5,TimeUnit.SECONDS);
			//Get the total money added during the round
			int delta = 0;
			for(Player next : players)
			{
				//Add their delta to the pile
				delta += (next.money - next.oldMoney);
				//And reset their delta to +$0
				next.money = next.oldMoney;
			}
			//Divide the total by the number of players
			delta /= playersJoined;
			//And give it to each of them
			for(Player next : players)
			{
				next.addMoney(delta,MoneyMultipliersToUse.NOTHING);
			}
			break;
		case SHUFFLE_ORDER:
			channel.sendMessage("It's a **Scramble**, everybody get up and change position!")
				.completeAfter(5,TimeUnit.SECONDS);
			Collections.shuffle(players);
			break;
		case END_ROUND:
			channel.sendMessage("It's the **End** of the **Round**!")
				.completeAfter(5,TimeUnit.SECONDS);
			gameStatus = GameStatus.END_GAME;
			channel.sendMessage("Game Over.").completeAfter(3,TimeUnit.SECONDS);
			timer.schedule(new WaitForEndGame(), 1000);
			return;
		case MYSTERY_MONEY:
			channel.sendMessage("It's **Mystery Money**, which today awards you...")
				.completeAfter(5,TimeUnit.SECONDS);
			int cashWon = (int)Math.pow((Math.random()*39)+1,4);
			StringBuilder resultString = new StringBuilder();
			resultString.append(String.format("**$%,d**!",Math.abs(cashWon)));
			StringBuilder extraResult = players.get(currentTurn).addMoney(cashWon, MoneyMultipliersToUse.BOOSTER_ONLY);
			channel.sendMessage(resultString.toString()).completeAfter(2,TimeUnit.SECONDS);
			if(extraResult != null)
				channel.sendMessage(extraResult.toString()).queue();
			break;
		}
		runEndTurnLogic();
	}
	void runNextEndGamePlayer()
	{
		//Are there any winners left to loop through?
		advanceTurn(true);
		//If we're out of people to run endgame stuff with, get outta here after displaying the board
		if(currentTurn == -1)
		{
			saveData();
			players.sort(null);
			displayBoardAndStatus(false, true, true);
			reset();
			if(winners.size() > 0)
			{
				//Got a single winner, crown them!
				if(winners.size() == 1)
				{
					players.addAll(winners);
					currentTurn = 0;
					for(int i=0; i<3; i++)
						channel.sendMessage("**" + players.get(0).name.toUpperCase() + " WINS RACE TO A BILLION!**")
							.completeAfter(2,TimeUnit.SECONDS);
					gameStatus = GameStatus.SEASON_OVER;
					if(!players.get(0).isBot)
					{
						timer.schedule(new RevealTheSBR(), 60000);
					}
				}
				//Hold on, we have *multiple* winners? ULTIMATE SHOWDOWN HYPE
				else
				{
					//Tell them what's happening
					StringBuilder announcementText = new StringBuilder();
					for(Player next : winners)
					{
						next.resetPlayer();
						announcementText.append(next.getSafeMention() + ", ");
					}
					announcementText.append("you have reached the goal together.");
					channel.sendMessage(announcementText.toString()).completeAfter(5,TimeUnit.SECONDS);
					channel.sendMessage("BUT THERE CAN BE ONLY ONE.").completeAfter(5,TimeUnit.SECONDS);
					channel.sendMessage("PREPARE FOR THE FINAL SHOWDOWN!").completeAfter(5,TimeUnit.SECONDS);
					//Prepare the game
					players.addAll(winners);
					winners.clear();
					playersJoined = players.size();
					startTheGameAlready();
				}
			}
			return;
		}
		//No? Good. Let's get someone to reward!
		//If they're a winner, boost their winstreak (folded players don't get this)
		if(players.get(currentTurn).status == PlayerStatus.ALIVE)
		{
			channel.sendMessage(players.get(currentTurn).getSafeMention() + " Wins!")
				.completeAfter(1,TimeUnit.SECONDS);
			//Boost winstreak by number of opponents defeated
			players.get(currentTurn).winstreak += (playersJoined - playersAlive);
		}
		//Now the winstreak is right, we can display the board
		displayBoardAndStatus(false, false, false);
		//Check to see if any bonus games have been unlocked - folded players get this too
		//Search every multiple of 5 to see if we've got it
		for(int i=5; i<=players.get(currentTurn).winstreak;i+=5)
		{
			if(players.get(currentTurn).oldWinstreak < i)
				switch(i)
				{
				case 5:
					players.get(currentTurn).games.add(Games.SUPERCASH);
					break;
				case 10:
					players.get(currentTurn).games.add(Games.DIGITAL_FORTRESS);
					break;
				case 15:
					players.get(currentTurn).games.add(Games.SPECTRUM);
					break;
				case 20:
				default:
					players.get(currentTurn).games.add(Games.HYPERCUBE);
					break;
				}
		}
		//If they're a winner, give them a win bonus (folded players don't get this)
		if(players.get(currentTurn).status == PlayerStatus.ALIVE)
		{
			//Award $20k for each space picked, double it if every space was picked, then share with everyone in
			int winBonus = 20000*(boardSize-spacesLeft);
			if(spacesLeft <= 0)
				winBonus *= 2;
			winBonus /= playersAlive;
			if(spacesLeft <= 0 && playersAlive == 1)
				channel.sendMessage("**SOLO BOARD CLEAR!**").queue();
			channel.sendMessage(players.get(currentTurn).name + " receives a win bonus of **$"
					+ String.format("%,d",winBonus) + "**.").queue();
			StringBuilder extraResult = null;
			extraResult = players.get(currentTurn).addMoney(winBonus,MoneyMultipliersToUse.BOOSTER_AND_BONUS);
			if(extraResult != null)
				channel.sendMessage(extraResult).queue();
			//Don't forget about the jackpot
			if(players.get(currentTurn).jackpot)
			{
				channel.sendMessage("You won the $25,000,000 **JACKPOT**!").queue();
				players.get(currentTurn).addMoney(25000000,MoneyMultipliersToUse.NOTHING);
			}
		}
		//Then, folded or not, play out any minigames they've won
		if(players.get(currentTurn).status == PlayerStatus.FOLDED)
			players.get(currentTurn).status = PlayerStatus.OUT;
		else
			players.get(currentTurn).status = PlayerStatus.DONE;
		gamesToPlay = players.get(currentTurn).games.listIterator(0);
		timer.schedule(new ClearMinigameQueueTask(), 1000);
	}
	void prepareNextMiniGame()
	{
		if(gamesToPlay.hasNext())
		{
			//Get the minigame
			Games nextGame = gamesToPlay.next();
			MiniGame currentGame = nextGame.getGame();
			//Don't bother printing messages for bots
			if(!players.get(currentTurn).isBot)
			{
				StringBuilder gameMessage = new StringBuilder();
				gameMessage.append(players.get(currentTurn).getSafeMention());
				if(currentGame.isBonusGame())
					gameMessage.append(", you've unlocked a bonus game: ");
				else
					gameMessage.append(", time for your next minigame: ");
				gameMessage.append(nextGame + "!");
				channel.sendMessage(gameMessage).queue();
			}
			startMiniGame(currentGame);
		}
		else
		{
			//Check for winning the game
			if(players.get(currentTurn).money >= 1000000000 && players.get(currentTurn).status == PlayerStatus.DONE)
			{
				winners.add(players.get(currentTurn));
			}
			runNextEndGamePlayer();
		}
	}
	void startMiniGame(MiniGame currentGame)
	{
		LinkedList<String> result = currentGame.initialiseGame();
		//Don't print minigame messages for bots
		if(!players.get(currentTurn).isBot)
		{
			for(String output : result)
			{
				channel.sendMessage(output).completeAfter(2,TimeUnit.SECONDS);
			}
		}
		runNextMiniGameTurn(currentGame);
	}
	void runNextMiniGameTurn(MiniGame currentGame)
	{
		if(players.get(currentTurn).isBot)
		{
			//Get their pick from the game and use it to play their next turn
			currentGame.playNextTurn(currentGame.getBotPick());
			
			
			//Check if the game's over
			if(currentGame.isGameOver())
			{
				completeMiniGame(currentGame);
			}
			else
			{
				runNextMiniGameTurn(currentGame);
			}
		}
		else
		{
			//Let's get more input to give it
			TimerTask warnPlayer = new MiniGameWarning();
			timer.schedule(warnPlayer,120000);
			waiter.waitForEvent(MessageReceivedEvent.class,
					//Right player and channel
					e ->
					{
						return (e.getChannel().equals(channel) && e.getAuthor().equals(players.get(currentTurn).user));
					},
					//Parse it and call the method that does stuff
					e -> 
					{
						warnPlayer.cancel();
						String miniPick = e.getMessage().getContentRaw();
						//Keep printing output until it runs out of output
						LinkedList<String> result = currentGame.playNextTurn(miniPick);
						for(String output : result)
						{
							channel.sendMessage(output).completeAfter(2,TimeUnit.SECONDS);
						}
						//Check if the game's over
						if(currentGame.isGameOver())
						{
							completeMiniGame(currentGame);
						}
						else
						{
							runNextMiniGameTurn(currentGame);
						}
					},
					180,TimeUnit.SECONDS, () ->
					{
						channel.sendMessage(players.get(currentTurn).getSafeMention() + 
								" has gone missing. Cancelling their minigames.").queue();
						players.get(currentTurn).games.clear();
						completeMiniGame(currentGame);
					});
		}
	}
	void completeMiniGame(MiniGame currentGame)
	{
		//Cool, game's over now, let's grab their winnings
		int moneyWon = currentGame.getMoneyWon();
		//Only the Super Bonus Round will do this
		if(moneyWon == -1000000000)
			return;
		int multiplier = 1;
		//Did they have multiple copies of the game?
		while(gamesToPlay.hasNext())
		{
			//Move the iterator back one, to the first instance of the game
			gamesToPlay.previous();
			//If it matches (ie multiple copies), remove one and add it to the multiplier
			if(gamesToPlay.next() == gamesToPlay.next())
			{
				multiplier++;
				gamesToPlay.remove();
			}
			//Otherwise we'd better out it back where it belongs
			else
			{
				gamesToPlay.previous();
				break;
			}
		}
		StringBuilder resultString = new StringBuilder();
		if(players.get(currentTurn).isBot)
		{
			resultString.append(players.get(currentTurn).name + String.format(" won **$%,d** from ",moneyWon));
			if(multiplier > 1)
				resultString.append(String.format("%d copies of ",multiplier));
			resultString.append(currentGame.toString() + ".");
		}
		else
		{
			resultString.append(String.format("Game Over. You won **$%,d**",moneyWon));
			if(multiplier > 1)
				resultString.append(String.format(" times %d copies!",multiplier));
			else
				resultString.append(".");
		}
		StringBuilder extraResult = null;
		//Bypass the usual method if it's a bonus game so we don't have booster or winstreak applied
		if(currentGame.isBonusGame())
			players.get(currentTurn).addMoney(moneyWon*multiplier,MoneyMultipliersToUse.NOTHING);
		else
			extraResult = players.get(currentTurn).addMoney((moneyWon*multiplier),MoneyMultipliersToUse.BOOSTER_AND_BONUS);
		channel.sendMessage(resultString).queue();
		if(extraResult != null)
			channel.sendMessage(extraResult).queue();
		//Off to the next minigame! (After clearing the queue)
		timer.schedule(new ClearMinigameQueueTask(), 1000);
	}
	void advanceTurn(boolean endGame)
	{
		//Keep spinning through until we've got someone who's still in the game, or until we've checked everyone
		int triesLeft = playersJoined;
		boolean isPlayerGood = false;
		do
		{
			currentTurn++;
			triesLeft --;
			currentTurn = currentTurn % playersJoined;
			//Is this player someone allowed to play now?
			switch(players.get(currentTurn).status)
			{
			case ALIVE:
				isPlayerGood = true;
				break;
			case FOLDED:
				if(endGame)
					isPlayerGood = true;
				break;
			default:
				break;
			}
		}
		while(!isPlayerGood && triesLeft > 0);
		//If we've checked everyone and no one is suitable anymore, whatever
		if(triesLeft == 0 && !isPlayerGood)
			currentTurn = -1;
	}
	boolean checkValidNumber(String message)
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
	public void displayBoardAndStatus(boolean printBoard, boolean totals, boolean copyToResultChannel)
	{
		if(gameStatus == GameStatus.SIGNUPS_OPEN)
		{
			//No board to display if the game isn't running!
			return;
		}
		StringBuilder board = new StringBuilder().append("```\n");
		//Board doesn't need to be displayed if game is over
		if(printBoard)
		{
			//Do we need a complex header, or should we use the simple one?
			int boardWidth = Math.max(5,playersJoined+1);
			if(boardWidth < 6)
				board.append("     RtaB     \n");
			else
			{
				for(int i=7; i<=boardWidth; i++)
				{
					//One space for odd numbers, two spaces for even numbers
					board.append(" ");
					if(i%2==0)
						board.append(" ");
				}
				//Then print the first part
				board.append("Race to ");
				//Extra space if it's odd
				if(boardWidth%2 == 1)
					board.append(" ");
				//Then the rest of the header
				board.append("a Billion\n");
			}
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
				if((i%boardWidth) == (boardWidth-1))
					board.append("\n");
				else
					board.append(" ");
			}
			board.append("\n");
		}
		//Next the status line
		//Start by getting the lengths so we can pad the status bars appropriately
		//Add one extra to name length because we want one extra space between name and cash
		int nameLength = players.get(0).name.length();
		for(int i=1; i<playersJoined; i++)
			nameLength = Math.max(nameLength,players.get(i).name.length());
		nameLength ++;
		//And ignore the negative sign if there is one
		int moneyLength;
		if(totals)
		{
			moneyLength = String.valueOf(Math.abs(players.get(0).money)).length();
			for(int i=1; i<playersJoined; i++)
				moneyLength = Math.max(moneyLength, String.valueOf(Math.abs(players.get(i).money)).length());
		}
		else
		{
			moneyLength = String.valueOf(Math.abs(players.get(0).money-players.get(0).oldMoney)).length();
			for(int i=1; i<playersJoined; i++)
				moneyLength = Math.max(moneyLength,
						String.valueOf(Math.abs(players.get(i).money-players.get(i).oldMoney)).length());		
		}
		//Make a little extra room for the commas
		moneyLength += (moneyLength-1)/3;
		//Then start printing - including pointer if currently their turn
		for(int i=0; i<playersJoined; i++)
		{
			if(currentTurn == i)
				board.append("> ");
			else
				board.append("  ");
			board.append(String.format("%-"+nameLength+"s",players.get(i).name));
			//Now figure out if we need a negative sign, a space, or neither
			int playerMoney = (players.get(i).money - players.get(i).oldMoney);
			//What sign to print?
			if(playerMoney<0)
				board.append("-");
			else
				board.append("+");
			//Then print the money itself
			board.append("$");
			board.append(String.format("%,"+moneyLength+"d",Math.abs(playerMoney)));
			//Now the booster display
			switch(players.get(i).status)
			{
			case ALIVE:
			case DONE:
				board.append(" [");
				board.append(String.format("%3d",players.get(i).booster));
				board.append("%");
				if(players.get(i).status == PlayerStatus.DONE || (gameStatus == GameStatus.END_GAME && currentTurn == i))
				{
					board.append("x");
					board.append(players.get(i).winstreak);
				}
				board.append("]");
				break;
			case OUT:
			case FOLDED:
				board.append(" [OUT] ");
				break;
			}
			//If they have any games, print them too
			if(players.get(i).games.size() > 0)
			{
				board.append(" {");
				for(Games minigame : players.get(i).games)
				{
					board.append(" " + minigame.getShortName());
				}
				board.append(" }");
			}
			board.append("\n");
			//If we want the totals as well, do them on a second line
			if(totals)
			{
				//Get to the right spot in the line
				for(int j=0; j<(nameLength-4); j++)
					board.append(" ");
				board.append("Total:");
				//Print sign
				if(players.get(i).money<0)
					board.append("-");
				else
					board.append(" ");
				//Then print the money itself
				board.append("$");
				board.append(String.format("%,"+moneyLength+"d\n\n",Math.abs(players.get(i).money)));
			}
		}
		//Close it off and print it out
		board.append("```");
		channel.sendMessage(board.toString()).queue();
		if(copyToResultChannel && resultChannel != null)
			resultChannel.sendMessage(board.toString()).queue();
	}
	void saveData()
	{
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores"+channel.getId()+".csv"));
			//Replace the records of the players if they're there, otherwise add them
			for(int i=0; i<playersJoined; i++)
			{
				if(players.get(i).newbieProtection == 1)
					channel.sendMessage(players.get(i).getSafeMention() + ", your newbie protection has expired. "
							+ "From now on, bomb penalties will be $250,000.").queue();
				int location = findUserInList(list,players.get(i).uID,false);
				StringBuilder toPrint = new StringBuilder();
				toPrint.append(players.get(i).uID+"#");
				toPrint.append(players.get(i).name+"#");
				toPrint.append(players.get(i).money+"#");
				toPrint.append(players.get(i).booster+"#");
				toPrint.append(players.get(i).winstreak+"#");
				toPrint.append(Math.max(players.get(i).newbieProtection-1,0)+"#");
				toPrint.append(players.get(i).lives+"#");
				toPrint.append(players.get(i).lifeRefillTime);
				if(location == -1)
					list.add(toPrint.toString());
				else
					list.set(location,toPrint.toString());
			}
			//Then sort and rewrite it
			DescendingScoreSorter sorter = new DescendingScoreSorter();
			list.sort(sorter);
			Path file = Paths.get("scores"+channel.getId()+".csv");
			Files.delete(file);
			Files.write(file, list);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static int findUserInList(List<String> list, String userID, boolean searchByName)
	{
		int field;
		if(searchByName)
			field = 1;
		else
			field = 0;
		/*
		 * record format:
		 * record[0] = uID
		 * record[1] = name
		 */
		String[] record;
		for(int i=0; i<list.size(); i++)
		{
			record = list.get(i).split("#");
			if(record[field].compareToIgnoreCase(userID) == 0)
				return i;
		}
		return -1;
	}
	public String listPlayers(boolean waitingOn)
	{
		StringBuilder resultString = new StringBuilder();
		if(waitingOn)
			resultString.append("**WAITING ON**");
		else
			resultString.append("**PLAYERS**");
		for(Player next : players)
		{
			if(!waitingOn || (waitingOn && next.status == PlayerStatus.OUT))
			{
				resultString.append(" | ");
				resultString.append(next.name);
			}
		}
		return resultString.toString();
	}
	public void splitAndShare(int totalToShare)
	{
		channel.sendMessage("Because " + players.get(currentTurn).getSafeMention() + " had a split and share, "
				+ "10% of their total will be split between the other players.").queueAfter(1,TimeUnit.SECONDS);
		for(int i=0; i<playersJoined; i++)
			//Don't pass money back to the player that hit it
			if(i != currentTurn)
			{
				//And divide the amount given by how many players there are to receive it
				players.get(i).addMoney(totalToShare / (playersJoined-1),MoneyMultipliersToUse.NOTHING);
			}
	}
	public String checkLives(String userID) {
		StringBuilder output = new StringBuilder();
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores"+channel.getId()+".csv"));
			int index = findUserInList(list,userID,false);
			if(index < 0)
			{
				output.append("You have not played yet this season, so you have " + Player.MAX_LIVES + " lives.");
				return output.toString();
			}
			String[] record = list.get(index).split("#");
			output.append(record[1] + ": ");
			if(Instant.parse(record[7]).isBefore(Instant.now()) && Integer.parseInt(record[6]) < Player.MAX_LIVES)
				output.append(Player.MAX_LIVES + " lives left.");
			else
			{
				output.append(record[6]);
				if(Integer.parseInt(record[6]) == 1)
					output.append(" life left.");
				else
					output.append(" lives left.");
				if(Integer.parseInt(record[6]) < Player.MAX_LIVES)
				{
					output.append(" Lives refill in ");
					//Check hours, then minutes, then seconds
					OffsetDateTime lifeRefillTime = Instant.parse(record[7]).minusSeconds(Instant.now().getEpochSecond())
							.atOffset(ZoneOffset.UTC);
					int hours = lifeRefillTime.getHour();
					if(hours>0)
					{
						output.append(hours + " hours, ");
					}
					int minutes = lifeRefillTime.getMinute();
					if(hours>0 || minutes>0)
					{
						output.append(minutes + " minutes, ");
					}
					int seconds = lifeRefillTime.getSecond();
					if(hours>0 || minutes>0 || seconds>0)
					{
						output.append(seconds + " seconds");
					}
					output.append(".");
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return output.toString();
	}
	public void addBot(int botNumber)
	{
		//Only do this if we're in signups!
		if(gameStatus != GameStatus.SIGNUPS_OPEN)
			return;
		GameBot chosenBot = GameBot.values()[botNumber];
		Player newPlayer = new Player(chosenBot,channel);
		players.add(newPlayer);
		playersJoined ++;
		if(newPlayer.money > 900000000)
		{
			channel.sendMessage(String.format("%1$s needs only $%2$,d more to reach the goal!",
					newPlayer.name,(1000000000-newPlayer.money)));
		}
	}
	public void addRandomBot()
	{
		//Only do this if we're in signups!
		if(gameStatus != GameStatus.SIGNUPS_OPEN && gameStatus != GameStatus.ADD_BOT_QUESTION)
			return;
		GameBot chosenBot = GameBot.values()[(int)(Math.random()*GameBot.values().length)];
		Player newPlayer;
		int triesLeft = GameBot.values().length;
		//Start looping through until we get a good one (one that hasn't exploded today)
		boolean goodPick;
		do
		{
			triesLeft --;
			chosenBot = chosenBot.next();
			newPlayer = new Player(chosenBot,channel);
			goodPick = true;
			for(int i=0; i<playersJoined; i++)
			{
				if(players.get(i).uID.equals(newPlayer.uID))
				{
					goodPick = false;
					break;
				}
			}
		}
		while((newPlayer.lives != Player.MAX_LIVES || !goodPick) && triesLeft > 0);
		if(newPlayer.lives != Player.MAX_LIVES)
		{
			//If we've checked EVERY bot...
			channel.sendMessage("No bots currently available. Game aborted.").queue();
			reset();
		}
		else
		{
			//But assuming we found one, add them in and get things rolling!
			players.add(newPlayer);
			playersJoined++;
			if(newPlayer.money > 900000000)
			{
				channel.sendMessage(String.format("%1$s needs only $%2$,d more to reach the goal!",
						newPlayer.name,(1000000000-newPlayer.money)));
			}
		}
	}
}