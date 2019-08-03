package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tel.discord.rtab.enums.GameBot;
import tel.discord.rtab.enums.Games;
import tel.discord.rtab.enums.HiddenCommand;
import tel.discord.rtab.enums.MoneyMultipliersToUse;
import tel.discord.rtab.enums.PlayerStatus;


public class Player implements Comparable<Player>
{
	static final int BOMB_PENALTY = -250000;
	static final int NEWBIE_BOMB_PENALTY = -100000;
	static final int MAX_BOOSTER = 999;
	static final int MIN_BOOSTER =  10;
	static final int MAX_LIVES = 5;
	MessageChannel channel;
	User user;
	String name;
	String uID;
	boolean isBot;
	int lives;
	Instant lifeRefillTime;
	int money;
	int oldMoney;
	int currentCashClub;
	int booster;
	int winstreak;
	int oldWinstreak;
	int newbieProtection;
	HiddenCommand hiddenCommand;
	//Event fields
	int peek;
	int jokers;
	int boostCharge;
	boolean splitAndShare;
	boolean minigameLock;
	boolean jackpot;
	boolean threshold;
	boolean warned;
	PlayerStatus status;
	LinkedList<Games> games;
	LinkedList<Integer> knownBombs;
	LinkedList<Integer> safePeeks;
	//Constructor for humans
	Player(Member playerName, MessageChannel channelID, String botName)
	{
		user = playerName.getUser();
		uID = user.getId();
		//Bots don't get newbie protection, and neither do humans playing as bots
		if(botName == null)
		{
			name = playerName.getEffectiveName();
			newbieProtection = 10;
		}
		else
		{
			name = botName;
			newbieProtection = 0;
		}
		isBot = false;
		initPlayer(channelID);
	}
	//Constructor for bots
	Player(GameBot botName, MessageChannel channelID)
	{
		user = null;
		name = botName.name;
		uID = botName.botID;
		isBot = true;
		newbieProtection = 0;
		initPlayer(channelID);
	}
	
	void initPlayer(MessageChannel channelID)
	{
		channel = channelID;
		lives = MAX_LIVES;
		lifeRefillTime = Instant.now();
		money = 0;
		booster = 100;
		winstreak = 0;
		peek = 1;
		jokers = 0;
		boostCharge = 0;
		hiddenCommand = HiddenCommand.NONE;
		splitAndShare = false;
		minigameLock = false;
		threshold = false;
		warned = false;
		status = PlayerStatus.OUT;
		games = new LinkedList<>();
		knownBombs = new LinkedList<>();
		safePeeks = new LinkedList<>();
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores"+channel.getId()+".csv"));
			String[] record;
			for(int i=0; i<list.size(); i++)
			{
				/*
				 * record format:
				 * record[0] = uID
				 * record[1] = name
				 * record[2] = money
				 * record[3] = booster
				 * record[4] = winstreak
				 * record[5] = newbieProtection
				 * record[6] = lives
				 * record[7] = time at which lives refill
				 */
				record = list.get(i).split("#");
				if(record[0].equals(uID))
				{
					money = Integer.parseInt(record[2]);
					booster = Integer.parseInt(record[3]);
					winstreak = Integer.parseInt(record[4]);
					newbieProtection = Integer.parseInt(record[5]);
					lifeRefillTime = Instant.parse(record[7]);
					lives = Integer.parseInt(record[6]);
					//If we're short on lives and we've passed the refill time, restock them
					if(lifeRefillTime.isBefore(Instant.now()) && lives < MAX_LIVES && lives >= 0)
						lives = MAX_LIVES;
					break;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		oldMoney = money;
		currentCashClub = money/100_000_000;
		oldWinstreak = winstreak;
	}
	StringBuilder addMoney(int amount, MoneyMultipliersToUse multipliers)
	{
		//Start with the base amount
		long adjustedPrize = amount;
		if(multipliers.useBoost)
		{
			//Multiply by the booster (then divide by 100 since it's a percentage)
			adjustedPrize *= booster;
			adjustedPrize /= 100;
		}
		//And if it's a "bonus" (win bonus, minigames, the like), multiply by winstreak ("bonus multiplier") too
		//But make sure they still get something even if they're on x0
		if(multipliers.useBonus)
		{
			adjustedPrize *= Math.max(10,winstreak);
			adjustedPrize /= 10;
		}
		//Dodge overflow by capping at +-$1,000,000,000 while adding the money
		if(adjustedPrize + money > 1_000_000_000) money = 1_000_000_000;
		else if(adjustedPrize + money < -1_000_000_000) money = -1_000_000_000;
		else money += adjustedPrize;
		//Build the string if we need it
		if(adjustedPrize != amount)
		{
			StringBuilder resultString = new StringBuilder();
			resultString.append("...which gets ");
			resultString.append(Math.abs(adjustedPrize) < Math.abs(amount) ? "drained" : "boosted");
			resultString.append(" to **");
			if(adjustedPrize<0)
				resultString.append("-");
			resultString.append("$");
			resultString.append(String.format("%,d**",Math.abs(adjustedPrize)));
			resultString.append(adjustedPrize<amount ? "." : "!");
			return resultString;
		}
		return null;
	}
	void addBooster(int amount)
	{
		booster += amount;
		//Convert excess boost to cash
		if(booster > MAX_BOOSTER)
		{
			addMoney(10000*(booster - MAX_BOOSTER), MoneyMultipliersToUse.NOTHING);
			booster = MAX_BOOSTER;
		}
		if(booster < MIN_BOOSTER)
		{
			addMoney(10000*(booster - MIN_BOOSTER), MoneyMultipliersToUse.NOTHING);
			booster = MIN_BOOSTER;
		}
	}
	int bankrupt()
	{
		int lostMoney = money - oldMoney;
		money = oldMoney;
		return lostMoney;
	}
	public StringBuilder blowUp(int multiplier, boolean holdBoost, int othersOut)
	{
		//Start with modifiers the main controller needs
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == channel)
			{
				game.repeatTurn = 0;
				game.playersAlive --;
				//We found the right channel, so
				break;
			}
		}
		//Just fold if they've got a minigame lock so they still play their games
		if(minigameLock && games.size() > 0)
		{
			status = PlayerStatus.FOLDED;
		}
		else
		{
			games.clear();
			status = PlayerStatus.OUT;
		}
		//Bomb penalty needs to happen before resetting their booster
		if(threshold) multiplier *= 4;
		int penalty;
		penalty = newbieProtection > 0 ? NEWBIE_BOMB_PENALTY : BOMB_PENALTY;
		//Reduce penalty by 10% for each player already gone
		penalty /= 10;
		penalty *= (10 - Math.min(9,othersOut));
		//Set their refill time if this is their first life lost, then dock it if they aren't in newbie protection
		if(newbieProtection <= 0)
		{
			if(lives == MAX_LIVES) lifeRefillTime = Instant.now().plusSeconds(72000);
			if(lives > 0)
			{
				if(lives == 1)
				{
					channel.sendMessage(getSafeMention() + ", you are out of lives. "
							+ "Further games today will incur an entry fee.").queue();
				}
				lives --;
			}
		}
		StringBuilder output = addMoney(penalty*multiplier,MoneyMultipliersToUse.BOOSTER_ONLY);
		//If they've got a split and share, they're in for a bad time
		if(splitAndShare)
		{
			for(GameController game : RaceToABillionBot.game)
			{
				if(game.channel == channel)
				{
					channel.sendMessage("Because " + getSafeMention() + " had a split and share, "
							+ "2% of their total will be given to each living player.")
							.queueAfter(1,TimeUnit.SECONDS);
					int moneyLost = (int)(money/50);
					addMoney(-1*moneyLost*game.playersAlive,MoneyMultipliersToUse.NOTHING);
					game.splitMoney(moneyLost*game.playersAlive, MoneyMultipliersToUse.NOTHING, true);
					//We found the right channel, so
					break;
				}
			}
		}
		//Wipe their booster if they didn't hit a boost holder
		if(!holdBoost)
			booster = 100;
		//Wipe everything else too, and dock them a life
		winstreak = 0;
		hiddenCommand = HiddenCommand.NONE;
		//Dumb easter egg
		if(money <= -1000000000)
		{
			channel.sendMessage("I'm impressed, "
					+ "but no you don't get anything special for getting your score this low.").queue();
			channel.sendMessage("See you next season!").queueAfter(1,TimeUnit.SECONDS);
		}
		return output;
	}
	@Override
	public int compareTo(Player other)
	{
		//THIS ISN'T CONSISTENT WITH EQUALS
		//Sort by round delta, descending order
		return (other.money - other.oldMoney) - (money - oldMoney);
	}
	/*
	 * If the player is human, gets their name as a mention
	 * If they aren't, just gets their name because user = null and null pointers are bad news bears yo!
	 */
	public String getSafeMention()
	{
		return isBot ? name : user.getAsMention();
	}
	
	public String printBombs()
	{
		StringBuilder result = new StringBuilder();
		result.append(name);
		result.append(":");
		for(int bomb : knownBombs)
		{
			result.append(" ");
			result.append(String.format("%02d",bomb+1));
		}
		return result.toString();
	}
	
	public String getName()
	{
		return name;
	}
}