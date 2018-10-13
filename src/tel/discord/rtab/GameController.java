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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;
import tel.discord.rtab.enums.BlammoChoices;
import tel.discord.rtab.enums.BombType;
import tel.discord.rtab.enums.CashType;
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
	final boolean rankChannel;
	public final boolean runDemo;
	final boolean verboseBotGames;
	public final boolean playersCanJoin;
	public TextChannel channel;
	TextChannel resultChannel;
	public BettingHandler betManager;
	int boardSize = 15;
	public List<Player> players = new ArrayList<>();
	List<Player> winners = new ArrayList<>();
	HashSet<String> pingList = new HashSet<>();
	int currentTurn = -1;
	boolean finalCountdown = false;
	boolean firstPick = true;
	int fcTurnsLeft;
	int repeatTurn = 0;
	boolean reverse = false;
	public int playersJoined = 0;
	int playersAlive = 0;
	int boardMultiplier;
	final int BASE_MULTIPLIER;
	ListIterator<Games> gamesToPlay;
	public GameStatus gameStatus = GameStatus.SIGNUPS_OPEN;
	boolean[] pickedSpaces;
	int spacesLeft;
	boolean[] bombs;
	Board gameboard;
	public static EventWaiter waiter;
	public ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);
	public ScheduledFuture<?> demoMode;
	Message waitingMessage;
	
	public GameController(TextChannel channelID, boolean allowJoining, boolean mainGame, 
			boolean useDemo, boolean verbosity, int globalMultiplier)
	{
		channel = channelID;
		playersCanJoin = allowJoining;
		rankChannel = mainGame;
		runDemo = useDemo;
		verboseBotGames = verbosity;
		BASE_MULTIPLIER = globalMultiplier;
		boardMultiplier = BASE_MULTIPLIER;
		if(runDemo)
		{
			demoMode = timer.schedule(() -> 
			{
				for(int i=0; i<4; i++)
					addRandomBot();
				startTheGameAlready();
			},60,TimeUnit.MINUTES);
		}
		//If they can't join, let them bet!
		if(!playersCanJoin)
			betManager = new BettingHandler(channel);
		else
			betManager = null;
	}
	
	void setResultChannel(TextChannel channelID)
	{
		resultChannel = channelID;
	}
	
	/**
	 * reset - (re)initialises the game state by removing all players and clearing the board.
	 */
	public void reset()
	{
		players.clear();
		currentTurn = -1;
		playersJoined = 0;
		playersAlive = 0;
		boardMultiplier = BASE_MULTIPLIER;
		if(gameStatus != GameStatus.SEASON_OVER)
			gameStatus = GameStatus.SIGNUPS_OPEN;
		gameboard = null;
		finalCountdown = false;
		repeatTurn = 0;
		reverse = false;
		timer.shutdownNow();
		timer = new ScheduledThreadPoolExecutor(1);
		if(runDemo)
		{
			demoMode = timer.schedule(() -> 
			{
				for(int i=0; i<4; i++)
					addRandomBot();
				startTheGameAlready();
			},60,TimeUnit.MINUTES);
		}
	}
	/**
	 * addPlayer - adds a player to the game, or updates their name if they're already in.
	 * MessageChannel channelID - channel the request took place in (only used to know where to send game details to)
	 * String playerID - ID of player to be added.
	 * Returns an enum which gives the result of the join attempt.
	 */
	public int findPlayerInGame(String playerID)
	{
		for(int i=0; i < players.size(); i++)
			if(players.get(i).uID.equals(playerID))
				return i;
		return -1;
	}
	
	public PlayerJoinReturnValue addPlayer(Member playerID)
	{
		//Are player joins even *allowed* here?
		if(!playersCanJoin)
			return PlayerJoinReturnValue.NOTALLOWEDHERE;
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
		//Dumb easter egg
		if(newPlayer.money <= -1000000000)
			return PlayerJoinReturnValue.ELIMINATED;
		//If they're out of lives, charge them and let them know
		if(newPlayer.lives <= 0 && newPlayer.newbieProtection <= 0)
		{
			int entryFee = Math.max(newPlayer.money/100,100000);
			newPlayer.money -= entryFee;
			newPlayer.oldMoney -= entryFee;
			channel.sendMessage(newPlayer.getSafeMention() + String.format(", you are out of lives. "
					+ "Playing this round will incur an entry fee of $%,d.",entryFee)).queue();
		}
		//Look for match already in player list
		int playerLocation = findPlayerInGame(newPlayer.uID);
		if(playerLocation != -1)
		{
			//Found them, check if we should update their name or just laugh at them
			if(players.get(playerLocation).name == newPlayer.name)
				return PlayerJoinReturnValue.ALREADYIN;
			else
			{
				players.set(playerLocation,newPlayer);
				return PlayerJoinReturnValue.UPDATED;
			}
		}
		//Haven't found one, add them to the list
		players.add(newPlayer);
		playersJoined++;
		if(newPlayer.money > 900000000)
		{
			channel.sendMessage(String.format("%1$s needs only $%2$,d more to reach the goal!",
					newPlayer.name,(1000000000-newPlayer.money))).queue();
		}
		if(playersJoined == 1)
		{
			if(runDemo)
				demoMode.cancel(false);
			timer.schedule(() -> 
			{
			channel.sendMessage("Thirty seconds before game starts!").queue();
			channel.sendMessage(listPlayers(false)).queue();
			}, 90, TimeUnit.SECONDS);
			timer.schedule(() -> startTheGameAlready(), 120, TimeUnit.SECONDS);
			return PlayerJoinReturnValue.CREATED;
		}
		else
			return PlayerJoinReturnValue.JOINED;
	}
	/**
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
		int playerLocation = findPlayerInGame(playerID.getId());
		if(playerLocation != -1)
		{
			players.remove(playerLocation);
			playersJoined --;
			//Abort the game if everyone left
			if(playersJoined == 0)
				reset();
			return PlayerQuitReturnValue.SUCCESS;
		}
		//Didn't find them, why are they trying to quit in the first place?
		return PlayerQuitReturnValue.NOTINGAME;
	}
	/**
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
								&& e.getChannel().getType() == ChannelType.PRIVATE
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
				//Determine player order
				Collections.shuffle(players);
				currentTurn = 0;
				gameboard = new Board(boardSize,playersJoined);
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
		//Count down if necessary
		if(finalCountdown)
		{
			//End the game if we're out of turns
			if(fcTurnsLeft == 0)
			{
				gameStatus = GameStatus.END_GAME;
				channel.sendMessage("Game Over.").completeAfter(3,TimeUnit.SECONDS);
				timer.schedule(() -> runNextEndGamePlayer(), 1, TimeUnit.SECONDS);
				return;
			}
			else if(fcTurnsLeft == 1)
				channel.sendMessage("The round will end **after this pick!**").queue();
			else
				channel.sendMessage(String.format("The round will end in **%d picks**.",fcTurnsLeft)).queue();
			//Otherwise subtract one
			fcTurnsLeft --;
		}
		//Figure out who to ping and what to tell them
		if(repeatTurn > 0 && !firstPick)
		{
			if(!(players.get(currentTurn).isBot))
				channel.sendMessage(players.get(currentTurn).getSafeMention() + ", pick again.")
					.completeAfter(4,TimeUnit.SECONDS);
		}
		else
		{
			firstPick = false;
			if(!players.get(currentTurn).isBot)
				channel.sendMessage(players.get(currentTurn).getSafeMention() + ", your turn. Choose a space on the board.")
					.completeAfter(4,TimeUnit.SECONDS);
		}
		if(repeatTurn > 0)
			repeatTurn --;
		//Display the board, then ready up the space picker
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
			ScheduledFuture<?> warnPlayer = timer.schedule(() -> 
			{
				channel.sendMessage(players.get(currentTurn).getSafeMention() + 
						", thirty seconds left to choose a space!").queue();
				displayBoardAndStatus(true,false,false);
			}, 60, TimeUnit.SECONDS);
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
						warnPlayer.cancel(false);
						int location = Integer.parseInt(e.getMessage().getContentRaw())-1;
						//Anyway go play out their turn
						timer.schedule(() -> resolveTurn(location), 1, TimeUnit.SECONDS);
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
					channel.sendMessage(String.format("(-$%d,000)",50*BASE_MULTIPLIER)).queueAfter(1,TimeUnit.SECONDS);
					players.get(currentTurn).addMoney(-50000*BASE_MULTIPLIER,MoneyMultipliersToUse.NOTHING);
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
			if(gameboard.bombBoard.get(bombChosen) == BombType.DUD)
				gameboard.bombBoard.set(bombChosen,BombType.NORMAL);
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
		//Event things
		if(players.get(currentTurn).threshold)
		{
			players.get(currentTurn).addMoney(-50000*BASE_MULTIPLIER,MoneyMultipliersToUse.NOTHING);
			channel.sendMessage(String.format("(-$%d,000)",50*BASE_MULTIPLIER)).queueAfter(1,TimeUnit.SECONDS);
		}
		if(players.get(currentTurn).boostCharge != 0)
		{
			players.get(currentTurn).addBooster(players.get(currentTurn).boostCharge);
			channel.sendMessage(String.format("(%+d%%)",players.get(currentTurn).boostCharge))
				.queueAfter(1,TimeUnit.SECONDS);
		}
		//Alright, moving on
		//Diamond armour check
		if(players.get(currentTurn).jokers == -1)
		{
			//Blammos are still immune :P
			if(gameboard.typeBoard.get(location) != SpaceType.BLAMMO)
				gameboard.typeBoard.set(location,SpaceType.CASH);
			runSafeLogic(location);
		}
		else if(bombs[location])
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
			detonateBombs();
			timer.schedule(() -> runNextEndGamePlayer(), 1, TimeUnit.SECONDS);
		}
		else
		{
			//Advance turn to next player if there isn't a repeat going
			if(repeatTurn == 0)
				advanceTurn(false);
			timer.schedule(() -> runTurn(), 1, TimeUnit.SECONDS);
		}
	}
	void runBombLogic(int location)
	{
		channel.sendMessage("...").completeAfter(5,TimeUnit.SECONDS);
		//Mock them appropriately if they self-bombed
		if(players.get(currentTurn).knownBombs.contains(location))
			channel.sendMessage("It's your own **BOMB**.").completeAfter(5,TimeUnit.SECONDS);
		else
			channel.sendMessage("It's a **BOMB**.").completeAfter(5,TimeUnit.SECONDS);
		//If player has a joker, force it to not explode
		//This is a really ugly way of doing it though
		if(players.get(currentTurn).jokers != 0)
		{
			channel.sendMessage("But you have a joker!").queueAfter(2,TimeUnit.SECONDS);
			//Don't deduct if negative, to allow for unlimited joker
			if(players.get(currentTurn).jokers > 0)
				players.get(currentTurn).jokers --;
			gameboard.bombBoard.set(location,BombType.DUD);
		}
		//But is it a special bomb?
		StringBuilder extraResult = null;
		int penalty = Player.BOMB_PENALTY*BASE_MULTIPLIER;
		if(players.get(currentTurn).newbieProtection > 0)
			penalty = Player.NEWBIE_BOMB_PENALTY*BASE_MULTIPLIER;
		//Reduce penalty for others out
		penalty /= 10;
		penalty *= (10 - Math.min(9,playersJoined-playersAlive));
		switch(gameboard.bombBoard.get(location))
		{
		case NORMAL:
			channel.sendMessage(String.format("It goes **BOOM**. $%,d lost as penalty.",Math.abs(penalty)))
				.completeAfter(5,TimeUnit.SECONDS);
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,(playersJoined-playersAlive));
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
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,(playersJoined-playersAlive));
			break;
		case BOOSTHOLD:
			StringBuilder boostHoldResult = new StringBuilder().append("It ");
			if(players.get(currentTurn).booster != 100)
				boostHoldResult.append("holds your boost, then ");
			boostHoldResult.append(String.format("goes **BOOM**. $%,d lost as penalty.",Math.abs(penalty)));
			channel.sendMessage(boostHoldResult)
					.completeAfter(5,TimeUnit.SECONDS);
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,true,(playersJoined-playersAlive));
			break;
		case GAMELOCK:
			StringBuilder gameLockResult = new StringBuilder().append("It ");
			if(players.get(currentTurn).games.size() > 0)
				gameLockResult.append("locks in your minigame" +
						(players.get(currentTurn).games.size() > 1 ? "s" : "") + ", then ");
			gameLockResult.append(String.format("goes **BOOM**. $%,d lost as penalty.",Math.abs(penalty)));
			channel.sendMessage(gameLockResult)
					.completeAfter(5,TimeUnit.SECONDS);
			players.get(currentTurn).minigameLock = true;
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,(playersJoined-playersAlive));
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
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER*chain,false,(playersJoined-playersAlive));
			break;
		case REVERSE:
			channel.sendMessage("It goes **BOOM**...")
				.completeAfter(5,TimeUnit.SECONDS);
			channel.sendMessage(String.format("But it's a REVERSE bomb. $%,d penalty awarded to living players!",
					Math.abs(penalty))).completeAfter(5,TimeUnit.SECONDS);
			players.get(currentTurn).blowUp(0,false,(playersJoined-playersAlive));
			splitMoney(-penalty,MoneyMultipliersToUse.BOOSTER_ONLY, false);
			break;
		case DETONATION:
			channel.sendMessage("It goes **KABLAM**! "
					+ String.format("$%,d lost as penalty, plus board damage.",Math.abs(penalty)))
				.completeAfter(5,TimeUnit.SECONDS);
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,(playersJoined-playersAlive));
			//Wipe out adjacent spaces
			int boardWidth = Math.max(5,playersJoined+1);
			boolean canAbove = (location >= boardWidth);
			boolean canBelow = (boardSize - location > boardWidth);
			boolean canLeft  = (location % boardWidth != 0);
			boolean canRight = (location % boardWidth != (boardWidth-1));
			//Orthogonal or diagonal?
			if(Math.random() < 0.5)
			{
				//Above
				if(canAbove && !pickedSpaces[location-boardWidth])
				{
					pickedSpaces[location-boardWidth] = true;
					spacesLeft --;
				}
				//Below
				if(canBelow && !pickedSpaces[location+boardWidth])
				{
					pickedSpaces[location+boardWidth] = true;
					spacesLeft --;
				}
				//Left
				if(canLeft && !pickedSpaces[location-1])
				{
					pickedSpaces[location-1] = true;
					spacesLeft --;
				}
				//Right
				if(canRight && !pickedSpaces[location+1])
				{
					pickedSpaces[location+1] = true;
					spacesLeft --;
				}
			}
			else
			{
				//Up-left
				if(canAbove && canLeft && !pickedSpaces[(location-1)-boardWidth])
				{
					pickedSpaces[(location-1)-boardWidth] = true;
					spacesLeft --;
				}
				//Up-right
				if(canAbove && canRight && !pickedSpaces[(location+1)-boardWidth])
				{
					pickedSpaces[(location+1)-boardWidth] = true;
					spacesLeft --;
				}
				//Down-left
				if(canBelow && canLeft && !pickedSpaces[(location-1)+boardWidth])
				{
					pickedSpaces[(location-1)+boardWidth] = true;
					spacesLeft --;
				}
				//Down-right
				if(canBelow && canRight && !pickedSpaces[(location+1)+boardWidth])
				{
					pickedSpaces[(location+1)+boardWidth] = true;
					spacesLeft --;
				}
			}
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
		/*
		 * Suspense rules:
		 * Always trigger on a blammo
		 * Otherwise, don't trigger if they have a joker
		 * Otherwise trigger randomly, chance determined by spaces left and players in the game
		 */
		if(((Math.random()*spacesLeft)<playersJoined && players.get(currentTurn).jokers == 0)
				|| gameboard.typeBoard.get(location) == SpaceType.BLAMMO)
			channel.sendMessage("...").completeAfter(5,TimeUnit.SECONDS);
		//Figure out what space we got
		LinkedList<String> cashOutput;
		ListIterator<String> outputIterator;
		switch(gameboard.typeBoard.get(location))
		{
		case CASH:
			cashOutput = awardCash(location);
			outputIterator = cashOutput.listIterator();
			//Will always have at least one
			channel.sendMessage(outputIterator.next()).completeAfter(5,TimeUnit.SECONDS);
			int i = 0;
			while(outputIterator.hasNext())
			{
				i++;
				channel.sendMessage(outputIterator.next()).queueAfter(750*i,TimeUnit.MILLISECONDS);
			}
			break;
		case BOOSTER:
			channel.sendMessage(awardBoost(location)).completeAfter(5,TimeUnit.SECONDS);
			break;
		case GAME:
			channel.sendMessage(awardGame(location)).completeAfter(5,TimeUnit.SECONDS);
			break;
		case GRAB_BAG:
			channel.sendMessage("It's a **Grab Bag**, you're winning some of everything!").completeAfter(5,TimeUnit.SECONDS);
			//Award everything in quick succession
			channel.sendMessage(awardGame(location)).queueAfter(1,TimeUnit.SECONDS);
			channel.sendMessage(awardBoost(location)).queueAfter(2,TimeUnit.SECONDS);
			cashOutput = awardCash(location);
			outputIterator = cashOutput.listIterator();
			//Will always have at least one
			channel.sendMessage(outputIterator.next()).completeAfter(3,TimeUnit.SECONDS);
			while(outputIterator.hasNext())
			{
				channel.sendMessage(outputIterator.next()).queueAfter(1,TimeUnit.SECONDS);
			}
			//No break, finish with event
		case EVENT:
			activateEvent(gameboard.eventBoard.get(location),location);
			break;
		case BLAMMO:
			channel.sendMessage("It's a **BLAMMO!** Quick " +
					players.get(currentTurn).getSafeMention() + ", press a button!").completeAfter(5,TimeUnit.SECONDS);
			channel.sendMessage("```\nBLAMMO\n 1  2 \n 3  4 \n```").queue();
			if(players.get(currentTurn).isBot)
			{
				runBlammo((int) (Math.random() * 4),false);
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
							timer.schedule(() -> runBlammo(button,false), 1, TimeUnit.SECONDS);
						},
						30,TimeUnit.SECONDS, () ->
						{
							channel.sendMessage("Too slow, autopicking!").queue();
							int button = (int) Math.random() * 4;
							runBlammo(button,false);
						});
			}
			return;
		}
		runEndTurnLogic();
	}

	private LinkedList<String> awardCash(int location)
	{
		LinkedList<String> output = new LinkedList<>();
		int cashWon;
		//Is it Mystery Money? Do that thing instead then
		if(gameboard.cashBoard.get(location) == CashType.MYSTERY)
		{
			output.add("It's **Mystery Money**, which today awards you...");
			if(Math.random() < 0.1)
				cashWon = -1*(int)Math.pow((Math.random()*39)+1,3);
			else
				cashWon = (int)Math.pow((Math.random()*39)+1,4);
		}
		else
		{
			cashWon = gameboard.cashBoard.get(location).getValue();
		}
		//Boost by board multiplier
		cashWon *= boardMultiplier;
		//On cash, update the player's score and tell them how much they won
		StringBuilder resultString = new StringBuilder();
		if(gameboard.cashBoard.get(location).getPrize() != null)
		{
			resultString.append("It's **");
			if(boardMultiplier > 1)
				resultString.append(String.format("%dx ",boardMultiplier));
			resultString.append(gameboard.cashBoard.get(location).getPrize());
			resultString.append("**, worth ");
		}
		resultString.append("**");
		if(cashWon<0)
			resultString.append("-");
		resultString.append(String.format("$%,d**!",Math.abs(cashWon)));
		output.add(resultString.toString());
		StringBuilder extraResult = players.get(currentTurn).addMoney(cashWon, MoneyMultipliersToUse.BOOSTER_ONLY);
		if(extraResult != null)
			output.add(extraResult.toString());
		return output;
	}

	private String awardBoost(int location)
	{
		//On cash, update the player's booster and tell them what they found
		int boostFound = gameboard.boostBoard.get(location).getValue();
		StringBuilder resultString = new StringBuilder();
		resultString.append(String.format("A **%+d%%** Booster",boostFound));
		resultString.append(boostFound > 0 ? "!" : ".");
		players.get(currentTurn).addBooster(boostFound);
		return resultString.toString();
	}

	private String awardGame(int location)
	{
		//On a game, announce it and add it to their game pile
		Games gameFound = gameboard.gameBoard.get(location);
		players.get(currentTurn).games.add(gameFound);
		players.get(currentTurn).games.sort(null);
		return ("It's a minigame, **" + gameFound + "**!");
	}

	private void runBlammo(int buttonPressed, boolean mega)
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
		int penalty = Player.BOMB_PENALTY*BASE_MULTIPLIER;
		switch(buttons.get(buttonPressed))
		{
		case BLOCK:
			channel.sendMessage("You BLOCKED the BLAMMO!").completeAfter(3,TimeUnit.SECONDS);
			break;
		case ELIM_OPP:
			channel.sendMessage("You ELIMINATED YOUR OPPONENT!").completeAfter(3,TimeUnit.SECONDS);
			//Pick a random player
			int playerToKill = (int) ((Math.random() * (playersAlive-1)) + 1);
			for(int i=0; i<playerToKill; i++)
				advanceTurn(false);
			//Kill them dead
			if(players.get(currentTurn).newbieProtection > 0)
				penalty = Player.NEWBIE_BOMB_PENALTY*BASE_MULTIPLIER;
			penalty /= 10;
			penalty *= (10 - Math.min(9,playersJoined-playersAlive));
			channel.sendMessage("Goodbye, " + players.get(currentTurn).getSafeMention()
					+ String.format("! $%,d penalty!",Math.abs(penalty*4))).queue();
			players.get(currentTurn).threshold = true;
			int tempRepeat = repeatTurn;
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,(playersJoined-playersAlive));
			repeatTurn = tempRepeat;
			//Shuffle back to starting player
			for(int i=playerToKill; i<=playersAlive; i++)
				advanceTurn(false);
			break;
		case THRESHOLD:
			if(mega)
			{
				//They actually did it hahahahahahahaha
				channel.sendMessage("You **ELIMINATED EVERYONE**!!").completeAfter(3,TimeUnit.SECONDS);
				while(currentTurn != -1)
				{
					penalty = players.get(currentTurn).newbieProtection > 0 ? Player.NEWBIE_BOMB_PENALTY : Player.BOMB_PENALTY;
					channel.sendMessage(String.format("$%1$,d penalty for %2$s!",
							Math.abs(penalty*4),players.get(currentTurn).getSafeMention())).completeAfter(2,TimeUnit.SECONDS);
					players.get(currentTurn).threshold = true;
					extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,0);
					channel.sendMessage(extraResult).queue();
					advanceTurn(false);
				}
				//Re-null this so we don't get an extra quote of it
				extraResult = null;
				break;
			}
			else if(players.get(currentTurn).threshold)
			{
				//You already have a threshold situation? Time for some fun!
				channel.sendMessage("You **UPGRADED the BLAMMO!** Don't panic, it can still be stopped...").completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("```\n MEGA \nBLAMMO\n 1  2 \n 3  4 \n```").queue();
				if(players.get(currentTurn).isBot)
				{
					runBlammo((int) (Math.random() * 4),true);
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
								timer.schedule(() -> runBlammo(button,true), 1, TimeUnit.SECONDS);
							},
							30,TimeUnit.SECONDS, () ->
							{
								channel.sendMessage("Too slow, autopicking!").queue();
								int button = (int) Math.random() * 4;
								runBlammo(button,true);
							});
				}
				return;
			}
			else
			{
				channel.sendMessage("You're entering a THRESHOLD SITUATION!").completeAfter(3,TimeUnit.SECONDS);
				channel.sendMessage(String.format("You'll lose $%d,000 for every pick you make, ",50*BASE_MULTIPLIER)
						+ "and if you lose the penalty will be four times as large!").queue();
				players.get(currentTurn).threshold = true;
				break;
			}
		case ELIM_YOU:
			channel.sendMessage("You ELIMINATED YOURSELF!").completeAfter(3,TimeUnit.SECONDS);
			if(players.get(currentTurn).newbieProtection > 0)
				penalty = Player.NEWBIE_BOMB_PENALTY*BASE_MULTIPLIER;
			penalty /= 10;
			penalty *= (10 - Math.min(9,playersJoined-playersAlive));
			channel.sendMessage(String.format("$%,d penalty!",Math.abs(penalty*4))).queue();
			players.get(currentTurn).threshold = true;
			extraResult = players.get(currentTurn).blowUp(BASE_MULTIPLIER,false,(playersJoined-playersAlive));
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
			channel.sendMessage("It's the **Triple Deal Lockdown**, "
					+ "it's just bombs, blammos, and tripled cash on the board now!")
				.completeAfter(5,TimeUnit.SECONDS);
			boardMultiplier *= 3;
			for(int i=0; i<boardSize; i++)
			{
				//Blammos aren't affected
				if(gameboard.typeBoard.get(i) != SpaceType.BLAMMO)
					gameboard.typeBoard.set(i,SpaceType.CASH);
			}
			break;
		case STARMAN:
			channel.sendMessage("Hooray, it's a **Starman**, here to destroy all the bombs!")
				.completeAfter(5,TimeUnit.SECONDS);
			detonateBombs();
			break;
		case DRAW_TWO:
			if(repeatTurn > 0)
			{
				channel.sendMessage("It's another **Draw Two**, and that means even more spaces for the next player!")
					.completeAfter(5,TimeUnit.SECONDS);
			}
			else
			{
				channel.sendMessage("It's a **Draw Two**, the next player needs to pick two spaces in a row!")
					.completeAfter(5,TimeUnit.SECONDS);
			}
			advanceTurn(false);
			firstPick = true;
			repeatTurn += 2;
			break;
		case DRAW_FOUR:
			if(repeatTurn > 0)
			{
				channel.sendMessage("It's another **Draw Four**, and that means even more spaces for the next player!")
					.completeAfter(5,TimeUnit.SECONDS);
			}
			else
			{
				channel.sendMessage("It's a **Draw Four**, the next player needs to pick FOUR spaces in a row!")
					.completeAfter(5,TimeUnit.SECONDS);
			}
			advanceTurn(false);
			firstPick = true;
			repeatTurn += 4;
			break;
		case JOKER:
			//This check shouldn't be needed right now, but in case we change things later
			if(players.get(currentTurn).jokers >= 0)
			{
				channel.sendMessage("Congratulations, you found a **Joker**, protecting you from a single bomb!")
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).jokers ++;
			}
			else
			{
				channel.sendMessage("You found a **Joker**, but you don't need it.")
					.completeAfter(5,TimeUnit.SECONDS);
			}
			break;
		case SUPER_JOKER:
			channel.sendMessage("You found the **MIDAS TOUCH**! "
					+ "Every space you pick for the rest of the round (even bombs) will be converted to cash, "
					+ "but you won't receive a win bonus at the end.")
				.completeAfter(5,TimeUnit.SECONDS);
			players.get(currentTurn).jokers = -1;
			break;
		case SPLIT_SHARE:
			if(!players.get(currentTurn).splitAndShare)
			{
				channel.sendMessage("It's a **Split & Share**, "
						+ "if you lose now you'll give 2% of your total to each living player, "
						+ "approximately $" + String.format("%,d",(players.get(currentTurn).money/50)) + "!")
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).splitAndShare = true;
			}
			else
			{
				channel.sendMessage("It's a **Split & Share**, but you already have one...")
					.completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("Well then, how about we activate it~?").completeAfter(3,TimeUnit.SECONDS);
				players.get(currentTurn).blowUp(0,true,(playersJoined-playersAlive));
			}
			break;
		case JACKPOT:
			if(!(players.get(currentTurn).jackpot))
			{
				channel.sendMessage(String.format("You found the $%d,000,000 **JACKPOT**, "
						+ "win the round to claim it!",boardSize*BASE_MULTIPLIER))
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).jackpot = true;
			}
			else
			{
				channel.sendMessage("You found a **JACKPOT**, but you already have one!")
					.completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("So you can cash this one in right away!").completeAfter(3,TimeUnit.SECONDS);
				players.get(currentTurn).addMoney(1000000*boardSize*BASE_MULTIPLIER,MoneyMultipliersToUse.NOTHING);
			}
			break;
		case STREAKPSMALL:
			int smallStreakAwarded = (int) ((Math.random() * 11) + 5);
			players.get(currentTurn).winstreak += smallStreakAwarded;
			channel.sendMessage(String.format("It's a **+%1$d.%2$d Streak Bonus**, raising you to x%3$d.%4$d!",
					smallStreakAwarded/10,smallStreakAwarded%10,
					players.get(currentTurn).winstreak/10,players.get(currentTurn).winstreak%10))
				.completeAfter(5,TimeUnit.SECONDS);
			break;
		case STREAKPLARGE:
			int bigStreakAwarded = (int) ((Math.random() * 15) + 16);
			players.get(currentTurn).winstreak += bigStreakAwarded;
			channel.sendMessage(String.format("It's a **+%1$d.%2$d Streak Bonus**, raising you to x%3$d.%4$d!",
					bigStreakAwarded/10,bigStreakAwarded%10,
					players.get(currentTurn).winstreak/10,players.get(currentTurn).winstreak%10))
				.completeAfter(5,TimeUnit.SECONDS);
			break;
		case BLAMMO_FRENZY:
			channel.sendMessage("It's a **Blammo Frenzy**, good luck!!")
				.completeAfter(5,TimeUnit.SECONDS);
			for(int i=0; i<boardSize; i++)
			{
				//Switch cash to blammo with 1/3 chance
				if(gameboard.typeBoard.get(i) == SpaceType.CASH && Math.random()*3 < 1)
					gameboard.typeBoard.set(i,SpaceType.BLAMMO);
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
			}
			//Divide the total by the number of players
			delta /= playersJoined;
			//And give it to each of them
			for(Player next : players)
			{
				next.addMoney(delta-(next.money-next.oldMoney),MoneyMultipliersToUse.NOTHING);
			}
			break;
		case REVERSE_ORDER:
			if(playersAlive > 2)
			{
				channel.sendMessage("It's a **Reverse**!")
					.completeAfter(5,TimeUnit.SECONDS);
				reverse = reverse ? false : true;
				break;
			}
			//If 2p, treat them as skips instead
		case SKIP_TURN:
			channel.sendMessage("It's a **Skip Turn**!")
				.completeAfter(5,TimeUnit.SECONDS);
			if(playersAlive == 2)
				repeatTurn++;
			else
			{
				if(repeatTurn > 0)
				{
					repeatTurn = 0;
					channel.sendMessage("(You also negated the extra draws!)").queue();
				}
				advanceTurn(false);
			}
			break;
		case END_ROUND:
			if(finalCountdown)
			{
				channel.sendMessage("It's the **Final Countdown**! Turns remaining cut in half!")
					.completeAfter(5,TimeUnit.SECONDS);
				fcTurnsLeft /= 2;
			}
			else
			{
				//Send message with appropriate
				channel.sendMessage("It's the **Final Countdown**!")
					.completeAfter(5,TimeUnit.SECONDS);
				finalCountdown = true;
				//Figure out turns left: max 50% remaining spaces, min players alive (max overrides min)
				if(spacesLeft/2 <= playersAlive)
					fcTurnsLeft = spacesLeft/2;
				else
					fcTurnsLeft = (int) (Math.random() * ((spacesLeft/2) - playersAlive + 1) + playersAlive);
			}
			return;
		case DOUBLE_DEAL:
			channel.sendMessage("It's a **Double Deal**, all cash left on the board is doubled in value!")
				.completeAfter(5,TimeUnit.SECONDS);
			boardMultiplier *= 2;
			break;
		case BOOST_CHARGER:
			channel.sendMessage("It's a **Boost Charger**, you'll gain 5% boost every turn for the rest of the round!")
				.completeAfter(5,TimeUnit.SECONDS);
			players.get(currentTurn).boostCharge += 5;
			break;
		case BRIBE:
			channel.sendMessage("You've been launched out of the round by the **Ejector Seat**!").completeAfter(5,TimeUnit.SECONDS);
			//$10k per space left on the board before the pick
			int bribe = 10000 * (spacesLeft+1) * BASE_MULTIPLIER;
			channel.sendMessage(String.format("You receive **$%,d** as compensation.",bribe)).queue();
			StringBuilder extraResult = players.get(currentTurn).addMoney(bribe,MoneyMultipliersToUse.BOOSTER_ONLY);
			if(extraResult != null)
				channel.sendMessage(extraResult.toString()).queue();
			//Fold if they have minigames, or qualified for a bonus game
			if(players.get(currentTurn).oldWinstreak < 50 * (players.get(currentTurn).winstreak / 50)
					|| players.get(currentTurn).games.size() > 0)
			{
				channel.sendMessage("You'll still get to play your minigames too.").queueAfter(1,TimeUnit.SECONDS);
				players.get(currentTurn).status = PlayerStatus.FOLDED;
			}
			else players.get(currentTurn).status = PlayerStatus.OUT;
			repeatTurn = 0;
			playersAlive --;
			break;
		case BOOST_MAGNET:
			//Get the total boost in play
			int totalBoost = 0;
			for(Player next : players)
			{
				//Add their boost to the pile
				totalBoost += (next.booster - 100)/2;
				next.booster -= (next.booster - 100)/2;
			}
			if(totalBoost != 0)
			{
				//And give it to the current player
				channel.sendMessage("It's a **Boost Magnet**, you get half of everyone's boost!")
					.completeAfter(5,TimeUnit.SECONDS);
				players.get(currentTurn).addBooster(totalBoost);
			}
			else
			{
				//No boost in play? BACKUP PLAN
				channel.sendMessage("It's a **Boost Magnet**, but there's no boost to give you...")
					.completeAfter(5,TimeUnit.SECONDS);
				channel.sendMessage("So you can have this instead.").completeAfter(3,TimeUnit.SECONDS);
				channel.sendMessage(awardBoost(location)).queue();
			}
			break;
		}
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
			//Handle the bets if this a betting channel
			if(betManager != null)
			{
				List<String> playerNames = new ArrayList<>(4);
				for(Player nextPlayer : players)
					playerNames.add(nextPlayer.name);
				betManager.resolveBets(players.get(0).name,playerNames);
			}
			reset();
			if(playersCanJoin)
				runPingList();
			if(winners.size() > 0)
			{
				//Got a single winner, crown them!
				if(winners.size() <= 1)
				{
					players.addAll(winners);
					currentTurn = 0;
					for(int i=0; i<3; i++)
					{
						System.out.println("Let's go #"+i);
						channel.sendMessage("**" + players.get(0).name.toUpperCase() + " WINS RACE TO A BILLION!**")
							.queueAfter(5+(5*i),TimeUnit.SECONDS);
					}
					channel.sendMessage("@everyone").queueAfter(20,TimeUnit.SECONDS);
					gameStatus = GameStatus.SEASON_OVER;
					System.out.println(gameStatus);
					if(!players.get(0).isBot && rankChannel)
					{
						timer.schedule(() -> 
						{
							channel.sendMessage(players.get(0).getSafeMention() + "...").complete();
							channel.sendMessage("It is time to enter the Super Bonus Round.").completeAfter(5,TimeUnit.SECONDS);
							channel.sendMessage("...").completeAfter(10,TimeUnit.SECONDS);
							startMiniGame(new SuperBonusRound());
						}, 90, TimeUnit.SECONDS);
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
					channel.sendMessage("**PREPARE FOR THE FINAL SHOWDOWN!**").completeAfter(5,TimeUnit.SECONDS);
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
			//+1 for first opponent defeated, +0.9 for second opponent, down to +0.1 for 10th+
			for(int i=0; i<(playersJoined-playersAlive); i++)
			{
				players.get(currentTurn).winstreak += Math.max(10-i, 1);
			}
		}
		//Now the winstreak is right, we can display the board
		displayBoardAndStatus(false, false, false);
		//If they're a winner and they weren't running diamond armour, give them a win bonus (folded players don't get this)
		if(players.get(currentTurn).status == PlayerStatus.ALIVE && players.get(currentTurn).jokers >= 0)
		{
			//Award $20k for each space picked, double it if every space was picked, then share with everyone in
			int winBonus = 20000*(boardSize-spacesLeft);
			if(spacesLeft <= 0)
				winBonus *= 2;
			winBonus *= BASE_MULTIPLIER;
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
				channel.sendMessage(String.format("You won the $%d,000,000 **JACKPOT**!",boardSize*BASE_MULTIPLIER)).queue();
				players.get(currentTurn).addMoney(1000000*boardSize*BASE_MULTIPLIER,MoneyMultipliersToUse.NOTHING);
			}
		}
		//Check to see if any bonus games have been unlocked - folded players get this too
		//Search every multiple of 5 to see if we've got it
		for(int i=50; i<=players.get(currentTurn).winstreak;i+=50)
		{
			if(players.get(currentTurn).oldWinstreak < i)
			{
				switch(i)
				{
				case 50:
					players.get(currentTurn).games.add(Games.SUPERCASH);
					break;
				case 100:
					players.get(currentTurn).games.add(Games.DIGITAL_FORTRESS);
					break;
				case 150:
					players.get(currentTurn).games.add(Games.SPECTRUM);
					break;
				case 200:
					players.get(currentTurn).games.add(Games.HYPERCUBE);
					break;
				default:
					channel.sendMessage(String.format("You're still going? Then have a +%d%% Boost!",i)).queue();
					players.get(currentTurn).addBooster(i);
				}
			}
		}
		//Then, folded or not, play out any minigames they've won
		if(players.get(currentTurn).status == PlayerStatus.FOLDED)
			players.get(currentTurn).status = PlayerStatus.OUT;
		else
			players.get(currentTurn).status = PlayerStatus.DONE;
		gamesToPlay = players.get(currentTurn).games.listIterator(0);
		timer.schedule(() -> prepareNextMiniGame(), 1, TimeUnit.SECONDS);
	}
	void prepareNextMiniGame()
	{
		if(gamesToPlay.hasNext())
		{
			//Get the minigame
			Games nextGame = gamesToPlay.next();
			MiniGame currentGame = nextGame.getGame();
			//Don't bother printing messages for bots, unless verbose
			if(!players.get(currentTurn).isBot || verboseBotGames)
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
		if(!players.get(currentTurn).isBot || verboseBotGames)
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
			LinkedList<String> result = currentGame.playNextTurn(currentGame.getBotPick());
			if(verboseBotGames)
			{
				for(String output : result)
				{
					channel.sendMessage(output).completeAfter(2,TimeUnit.SECONDS);
				}
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
		}
		else
		{
			//Let's get more input to give it
			ScheduledFuture<?> warnPlayer = timer.schedule(() -> 
			{
				channel.sendMessage(players.get(currentTurn).getSafeMention() + 
						", are you still there? One minute left!").queue();
			}, 120, TimeUnit.SECONDS);
			waiter.waitForEvent(MessageReceivedEvent.class,
					//Right player and channel
					e ->
					{
						return (e.getChannel().equals(channel) && e.getAuthor().equals(players.get(currentTurn).user));
					},
					//Parse it and call the method that does stuff
					e -> 
					{
						warnPlayer.cancel(false);
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
			resultString.append(players.get(currentTurn).name + String.format(" won **$%,d** from ",moneyWon*multiplier*BASE_MULTIPLIER));
			if(multiplier > 1)
				resultString.append(String.format("%d copies of ",multiplier));
			resultString.append(currentGame.toString() + ".");
		}
		else
		{
			resultString.append(String.format("Game Over. You won **$%,d**",moneyWon*BASE_MULTIPLIER));
			if(multiplier > 1)
				resultString.append(String.format(" times %d copies!",multiplier));
			else
				resultString.append(".");
		}
		StringBuilder extraResult = null;
		//Bypass the usual method if it's a bonus game so we don't have booster or winstreak applied
		if(currentGame.isBonusGame())
			players.get(currentTurn).addMoney(moneyWon*multiplier*BASE_MULTIPLIER,MoneyMultipliersToUse.NOTHING);
		else
			extraResult = players.get(currentTurn).addMoney((moneyWon*multiplier),MoneyMultipliersToUse.BOOSTER_AND_BONUS);
		channel.sendMessage(resultString).queue();
		if(extraResult != null)
			channel.sendMessage(extraResult).queue();
		//Off to the next minigame! (After clearing the queue)
		timer.schedule(() -> prepareNextMiniGame(), 1, TimeUnit.SECONDS);
	}
	void advanceTurn(boolean endGame)
	{
		//Keep spinning through until we've got someone who's still in the game, or until we've checked everyone
		int triesLeft = playersJoined;
		boolean isPlayerGood = false;
		do
		{
			//Subtract rather than add if we're reversed
			currentTurn += reverse ? -1 : 1;
			triesLeft --;
			currentTurn = Math.floorMod(currentTurn,playersJoined);
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
					board.append(i%2==0 ? "  " : " ");
				}
				//Then print the first part
				board.append("Race to ");
				//Extra space if it's odd
				if(boardWidth%2 == 1) board.append(" ");
				//Then the rest of the header
				board.append("a Billion\n");
			}
			for(int i=0; i<boardSize; i++)
			{
				board.append(pickedSpaces[i] ? "  " : String.format("%02d",(i+1)));
				board.append((i%boardWidth) == (boardWidth-1) ? "\n" : " ");
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
			board.append(currentTurn == i ? "> " : "  ");
			board.append(String.format("%-"+nameLength+"s",players.get(i).name));
			//Now figure out if we need a negative sign, a space, or neither
			int playerMoney = (players.get(i).money - players.get(i).oldMoney);
			//What sign to print?
			board.append(playerMoney<0 ? "-" : "+");
			//Then print the money itself
			board.append(String.format("$%,"+moneyLength+"d",Math.abs(playerMoney)));
			//Now the booster display
			switch(players.get(i).status)
			{
			case ALIVE:
			case DONE:
				board.append(String.format(" [%3d%%",players.get(i).booster));
				if(players.get(i).status == PlayerStatus.DONE || (gameStatus == GameStatus.END_GAME && currentTurn == i))
					board.append(String.format("x%1$d.%2$d",players.get(i).winstreak/10,players.get(i).winstreak%10));
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
				for(int j=0; j<(nameLength-4); j++) board.append(" ");
				board.append("Total:");
				//Print sign
				board.append(players.get(i).money<0 ? "-" : " ");
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
			//Go through each player in the game to update their stats
			for(int i=0; i<playersJoined; i++)
			{
				/*
				 * Special case - if you lose the round with $1B you get bumped to $999,999,999
				 * so that an elimination without penalty (eg bribe) doesn't get you declared champion
				 * This is since you haven't won yet, after all (and it's *extremely* rare to win a round without turning a profit)
				 * Note that in the instance of a final showdown, both players are temporarily labelled champion
				 * But after the tie is resolved, one will be bumped back to $900M
				 */
				if(players.get(i).money == 1000000000 && players.get(i).status != PlayerStatus.DONE)
					players.get(i).money --;
				//Replace the records of the players if they're there, otherwise add them
				if(players.get(i).newbieProtection == 1)
					channel.sendMessage(players.get(i).getSafeMention() + ", your newbie protection has expired. "
							+ "From now on, your base bomb penalty will be $250,000.").queue();
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
				//Update a player's role if it's the role channel, they're human, and have earned a new one
				if(players.get(i).money/100000000 != players.get(i).oldMoney/100000000 && !players.get(i).isBot && rankChannel)
				{
					//Get the mod controls
					GuildController guild = channel.getGuild().getController();
					List<Role> rolesToAdd = new LinkedList<>();
					List<Role> rolesToRemove = new LinkedList<>();
					//Remove their old score role if they had one
					if(players.get(i).oldMoney/100000000 > 0 && players.get(i).oldMoney/100000000 < 10)
						rolesToRemove.addAll(guild.getGuild().getRolesByName(
										String.format("$%d00M",players.get(i).oldMoney/100000000),false));
					//Special case for removing Champion role in case of final showdown
					else if(players.get(i).oldMoney/100000000 == 10)
						rolesToRemove.addAll(guild.getGuild().getRolesByName("Champion",false));
					//Add their new score role if they deserve one
					if(players.get(i).money/100000000 > 0 && players.get(i).money/100000000 < 10)
						rolesToAdd.addAll(guild.getGuild().getRolesByName(
										String.format("$%d00M",players.get(i).money/100000000),false));
					//Or do fancy stuff for the Champion
					else if(players.get(i).money/100000000 == 10)
						rolesToAdd.addAll(guild.getGuild().getRolesByName("Champion",false));
					//Then add/remove appropriately
					guild.modifyMemberRoles(guild.getGuild().getMemberById(players.get(i).uID),
							rolesToAdd,rolesToRemove).queue();
				}
			}
			//Then sort and rewrite it
			DescendingScoreSorter sorter = new DescendingScoreSorter();
			list.sort(sorter);
			Path file = Paths.get("scores"+channel.getId()+".csv");
			Path oldFile = Files.move(file, file.resolveSibling("scores"+channel.getId()+"old.csv"));
			Files.write(file, list);
			Files.delete(oldFile);
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
	
	public void splitMoney(int totalToShare, MoneyMultipliersToUse multipliers, boolean divide)
	{
		if(divide)
			totalToShare /= playersAlive;
		for(int i=0; i<playersJoined; i++)
			//Don't pass money back to the player that hit it, and don't pass to dead players
			if(i != currentTurn && players.get(i).status == PlayerStatus.ALIVE)
			{
				players.get(i).addMoney(totalToShare,multipliers);
			}
	}
	public String checkLives(int index) {
		StringBuilder output = new StringBuilder();
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores"+channel.getId()+".csv"));
			String[] record = list.get(index).split("#");
			output.append(record[1] + ": ");
			if(Integer.parseInt(record[5]) > 0)
			{
				output.append(record[5]);
				output.append(" game");
				if(Integer.parseInt(record[5]) != 1)
					output.append("s");
				output.append(" of newbie protection left.");
			}
			else
			{
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
	void detonateBombs()
	{
		for(int i=0; i<boardSize; i++)
			if(bombs[i] && !pickedSpaces[i])
			{
				channel.sendMessage("Bomb in space " + (i+1) + " destroyed.")
					.queueAfter(2,TimeUnit.SECONDS);
				pickedSpaces[i] = true;
				spacesLeft --;
			}
	}
	public void addToPingList(User playerToAdd)
	{
		pingList.add(playerToAdd.getAsMention());
	}
	void runPingList()
	{
		//Don't do this if no one's actually there to ping
		if(pingList.size() == 0)
			return;
		StringBuilder output = new StringBuilder();
		if(playersCanJoin)
			output.append("The game is finished");
		else
			output.append("Betting is now open");
		for(String nextName : pingList)
		{
			output.append(" - ");
			output.append(nextName);
		}
		pingList.clear();
		channel.sendMessage(output.toString()).queue();
	}
}