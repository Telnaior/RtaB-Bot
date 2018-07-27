package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import tel.discord.rtab.enums.Games;
import tel.discord.rtab.enums.MoneyMultipliersToUse;
import tel.discord.rtab.enums.PlayerStatus;


class Player implements Comparable<Player>
{
	static final int BOMB_PENALTY = -250000;
	static final int NEWBIE_BOMB_PENALTY = -100000;
	static final int MAX_BOOSTER = 999;
	static final int MIN_BOOSTER = 010;
	User user;
	String name;
	String uID;
	int money;
	int oldMoney;
	int booster;
	int winstreak;
	int oldWinstreak;
	int newbieProtection;
	//Event fields
	int jokers;
	boolean splitAndShare;
	boolean minigameLock;
	boolean jackpot;
	boolean threshold;
	PlayerStatus status;
	LinkedList<Games> games; 
	Player(Member playerName)
	{
		user = playerName.getUser();
		name = playerName.getEffectiveName();
		uID = user.getId();
		money = 0;
		booster = 100;
		winstreak = 0;
		newbieProtection = 10;
		jokers = 0;
		splitAndShare = false;
		minigameLock = false;
		threshold = false;
		status = PlayerStatus.OUT;
		games = new LinkedList<>();
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
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
				 */
				record = list.get(i).split(":");
				if(record[0].equals(uID))
				{
					money = Integer.parseInt(record[2]);
					booster = Integer.parseInt(record[3]);
					winstreak = Integer.parseInt(record[4]);
					newbieProtection = Integer.parseInt(record[5]);
					break;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		oldMoney = money;
		oldWinstreak = winstreak;
	}
	StringBuilder addMoney(int amount, MoneyMultipliersToUse multipliers)
	{
		//Start with the base amount
		int adjustedPrize = amount;
		if(multipliers.useBoost)
		{
			//Multiply by the booster (then divide by 100 since it's a percentage)
			if(amount%100 != 0)
			{
				adjustedPrize *= booster;
				adjustedPrize /= 100;
			}
			else
			{
				adjustedPrize /= 100;
				adjustedPrize *= booster;
			}
		}
		//And if it's a "bonus" (win bonus, minigames, the like), multiply by winstreak ("bonus multiplier") too
		//But make sure they still get something even if they're on x0
		if(multipliers.useBonus)
			adjustedPrize *= Math.max(1,winstreak);
		money += adjustedPrize;
		//Cap at +-$1,000,000,000
		if(money > 1000000000)
			money = 1000000000;
		if(money <= -1000000000)
		{
			money = -1000000000;
		}
		//Build the string if we need it
		if(adjustedPrize != amount)
		{
			StringBuilder resultString = new StringBuilder();
			resultString.append("...which gets ");
			if(Math.abs(adjustedPrize) < Math.abs(amount))
				resultString.append("drained");
			else
				resultString.append("boosted");
			resultString.append(" to **");
			if(adjustedPrize<0)
				resultString.append("-");
			resultString.append("$");
			resultString.append(String.format("%,d**",Math.abs(adjustedPrize)));
			if(adjustedPrize<amount)
				resultString.append(".");
			else
				resultString.append("!");
			return resultString;
		}
		return null;
	}
	void addBooster(int amount)
	{
		booster += amount;
		if(booster > MAX_BOOSTER)
			booster = MAX_BOOSTER;
		if(booster < MIN_BOOSTER)
			booster = MIN_BOOSTER;
	}
	int bankrupt()
	{
		int oldMoney = money;
		money = 0;
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
			String[] record;
			for(int i=0; i<list.size(); i++)
			{
				record = list.get(i).split(":");
				if(record[0].equals(uID))
				{
					money = Integer.parseInt(record[2]);
					break;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return oldMoney - money;
	}
	public StringBuilder blowUp(int multiplier, boolean holdBoost)
	{
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
		if(threshold)
			multiplier *= 4;
		int penalty;
		if(newbieProtection > 0)
			penalty = NEWBIE_BOMB_PENALTY;
		else
			penalty = BOMB_PENALTY;
		StringBuilder output = addMoney(penalty*multiplier,MoneyMultipliersToUse.BOOSTER_ONLY);
		//If they've got a split and share, they're in for a bad time
		if(splitAndShare)
		{
			int moneyLost = money/10;
			addMoney(-1*moneyLost,MoneyMultipliersToUse.NOTHING);
			GameController.splitAndShare(moneyLost);
		}
		//Wipe their booster if they didn't hit a boost holder
		if(!holdBoost)
			booster = 100;
		//Wipe everything else too
		winstreak = 0;
		GameController.repeatTurn = 0;
		GameController.playersAlive --;
		//Dumb easter egg
		if(money <= -1000000000)
		{
			GameController.channel.sendMessage("I'm impressed, "
					+ "but no you don't get anything special for getting your score this low.").queue();
			GameController.channel.sendMessage("See you next season!").queueAfter(1,TimeUnit.SECONDS);
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
	void resetPlayer()
	{
		oldMoney = money;
		oldWinstreak = winstreak;
		games.clear();
		status = PlayerStatus.OUT;
	}
}