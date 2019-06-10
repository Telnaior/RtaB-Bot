package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.ibm.icu.text.RuleBasedNumberFormat;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.tuple.ImmutablePair;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class BettingHandler {
	static final int MAX_OVERDRAFT = 1000;
	static final int HOUSE_BET = 100;
	TextChannel channel;
	List<Bettor> bettors;
	
	BettingHandler(TextChannel channelID)
	{
		channel = channelID;
		bettors = new ArrayList<>(20);
	}
	public void placeBet(Member bettor, int amount, String champion)
	{
		//All the validation of the bet itself should have been done already, so what we're doing here is more or less like joining a game
		Bettor newBettor = new Bettor(bettor, channel.getId());
		//Make sure they haven't bet already
		for(int i=0; i < bettors.size(); i++)
			if(bettors.get(i).uID.equals(newBettor.uID))
			{
				channel.sendMessage("Cannot place bet: You already have a bet.").queue();
				return;
			}
		//Check the name
		if(newBettor.name.contains(":") || newBettor.name.contains("#") || newBettor.name.startsWith("!"))
		{
			channel.sendMessage("Cannot place bet: Illegal name.").queue();
			return;
		}
		//Check they've got the money
		if(newBettor.funds < amount && amount > MAX_OVERDRAFT)
		{
			channel.sendMessage("Cannot place bet: You don't have enough funds.").queue();
			return;
		}
		//Cool, they're clear, pass it on
		newBettor.setBet(amount, champion);
		bettors.add(newBettor);
		channel.sendMessage("Bet placed.").queue();
	}
	public Pair<String,List<Integer>> listBets(List<String> playerNames)
	{
		//Get our list started
		List<StringBuilder> bets = new ArrayList<>(4);
		List<Integer> betTotals = new ArrayList<>(4);
		StringBuilder output = new StringBuilder();
		for(int i=0; i<playerNames.size(); i++)
		{
			bets.add(new StringBuilder().append(playerNames.get(i)).append("\n"));
			betTotals.add(0);
		}
		//Now add our bets to the right spots
		for(Bettor nextBet : bettors)
		{
			boolean badBet = true;
			//Look for the player that matches their bet
			for(int i=0; i<playerNames.size(); i++)
			{
				if(nextBet.champion.equals(playerNames.get(i).toUpperCase()))
				{
					badBet = false;
					//Add their string to the list, and their amount to the total for that player
					//Something about this code feels really rough but I can't think of a better way to do it right now
					bets.get(i).append(String.format("¤%1$,10d - %2$s\n",nextBet.betAmount,nextBet.name));
					betTotals.set(i,betTotals.get(i)+nextBet.betAmount);
					break;
				}
			}
			//If their bet doesn't match any of the players in the game, refund it
			if(badBet)
			{
				channel.sendMessage(nextBet.name+"'s bet on "+nextBet.champion+" refunded.").queue();
				bettors.remove(nextBet);
			}
		}
		//Add house bets if necessary
		for(int i=0; i<playerNames.size(); i++)
			if(betTotals.get(i) < HOUSE_BET)
			{
				bets.get(i).append(String.format("¤%,10d - RtaB\n", HOUSE_BET - betTotals.get(i)));
				betTotals.set(i,HOUSE_BET);
			}
		//Now stitch them up together in the proper format
		{
			output.append("```\n");
			for(StringBuilder nextList : bets)
			{
				output.append(nextList);
				output.append("\n");
			}
			output.append("```");
		}
		return ImmutablePair.of(output.toString(),betTotals);
	}
	void resolveBets(String winnerName, List<String> playerNames)
	{
		//List bets and get our totals
		Pair<String,List<Integer>> betsAndTotals = listBets(playerNames);
		if(bettors.size() > 0)
			channel.sendMessage(betsAndTotals.getLeft()).completeAfter(5,TimeUnit.SECONDS);
		List<Integer> totalBets = betsAndTotals.getRight();
		//Gather up all the lost bets
		int totalWrongBets = 0;
		for(int i=1; i<totalBets.size(); i++)
				totalWrongBets += totalBets.get(i);
		//Now loop through all the bettors and reward the winners
		boolean noWinners = true;
		for(Bettor nextBettor : bettors)
			if(nextBettor.champion.equals(winnerName.toUpperCase()))
			{
				//This guy won, reward them accordingly
				noWinners = false;
				int baseWin = nextBettor.betAmount * 4;
				double bonusPortion = nextBettor.betAmount;
				bonusPortion /= totalBets.get(0);
				int bonusWin = (int) (totalWrongBets * bonusPortion);
				channel.sendMessage(String.format("%1$s wins ¤%2$,d + ¤%3$,d!",nextBettor.name, baseWin, bonusWin))
					.completeAfter(2,TimeUnit.SECONDS);
				nextBettor.funds += baseWin + bonusWin;
				//Check to see if they've won
				if(nextBettor.funds > 1000000000)
				{
					//Reset them and add a prestige
					nextBettor.prestige ++;
					RuleBasedNumberFormat nf = new RuleBasedNumberFormat(Locale.UK, RuleBasedNumberFormat.SPELLOUT);
					channel.sendMessage(nextBettor.name + ", you have earned your " +
					nf.format(nextBettor.prestige, "%spellout-ordinal") +" billion!").queue();
					if(nextBettor.prestige == 1)
						channel.sendMessage("We'll just bank that so you can start over.").queueAfter(1,TimeUnit.SECONDS);
					else
						channel.sendMessage("You know what happens now, good luck with your next run.").queueAfter(1,TimeUnit.SECONDS);
					nextBettor.funds = 0;
				}
			}
		if(noWinners && bettors.size() > 0)
			channel.sendMessage("The house wins again! Mwahaha~").queueAfter(2,TimeUnit.SECONDS);
		saveData();
	}
	
	void saveData()
	{
		try
		{
			List<String> list = Files.readAllLines(Paths.get("bettors"+channel.getId()+".csv"));
			//Go through each player in the game to update their stats
			for(Bettor nextBettor : bettors)
			{
				int location = GameController.findUserInList(list,nextBettor.uID,false);
				StringBuilder toPrint = new StringBuilder();
				toPrint.append(nextBettor.uID+"#");
				toPrint.append(nextBettor.name+"#");
				toPrint.append(nextBettor.funds+"#");
				toPrint.append(nextBettor.prestige+"#");
				if(location == -1)
					list.add(toPrint.toString());
				else
					list.set(location,toPrint.toString());
			}
			//Then sort and rewrite it
			DescendingScoreSorter sorter = new DescendingScoreSorter();
			list.sort(sorter);
			Path file = Paths.get("bettors"+channel.getId()+".csv");
			Path oldFile = Files.move(file, file.resolveSibling("bettors"+channel.getId()+"old.csv"));
			Files.write(file, list);
			Files.delete(oldFile);
			bettors.clear();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public int getBetCount()
	{
		return bettors.size();
	}
}
