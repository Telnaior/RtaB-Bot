package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import tel.discord.rtab.enums.Games;
import tel.discord.rtab.enums.PlayerStatus;


class Player
{
	final int MAX_BOOSTER = 999;
	final int MIN_BOOSTER = 010;
	User user;
	String name;
	String uID;
	int money;
	int booster;
	int winstreak;
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
				 */
				record = list.get(i).split(":");
				if(record[0].equals(uID))
				{
					money = Integer.parseInt(record[2]);
					booster = Integer.parseInt(record[3]);
					winstreak = Integer.parseInt(record[4]);
					break;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	StringBuilder addMoney(int amount, boolean bonus)
	{
		//Start with the base amount
		int adjustedPrize = amount;
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
		//And if it's a "bonus" (win bonus, minigames, the like), multiply by winstreak ("bonus multiplier") too
		if(bonus)
			adjustedPrize *= winstreak;
		money += adjustedPrize;
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
}