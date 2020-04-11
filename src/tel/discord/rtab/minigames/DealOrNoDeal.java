package tel.discord.rtab.minigames;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DealOrNoDeal implements MiniGame {
	static final String NAME = "Deal or No Deal";
	static final boolean BONUS = false;
	List<Integer> VALUE_LIST = Arrays.asList(1,2,5,10,50,100,500,1000,2500,5000,7500, //Blues
			10000,30000,50000,100000,150000,200000,350000,500000,750000,1000000,2500000); //Reds
	LinkedList<Integer> values = new LinkedList<>();
	int offer;
	int casesLeft;
	boolean accept; //Accepting the Offer

	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier) {
		casesLeft = 22;
		offer = 0;
		accept = false;
		//Multiply each value, EXCEPT the $1, by the base multiplier
		for(int i = 1; i < VALUE_LIST.size(); i++)
		{
			VALUE_LIST.set(i, VALUE_LIST.get(i)*baseMultiplier);
		}
		//Load up the boxes and shuffle them
		values.clear();
		values.addAll(VALUE_LIST);
		Collections.shuffle(values);
		//Give instructions
		LinkedList<String> output = new LinkedList<>();
		output.add("In Deal or No Deal, there are 22 boxes, "
				+ String.format("each holding an amount of money from $1 to $%,d.",2500000*baseMultiplier));
		output.add("One of these boxes is 'yours', and if you refuse all the offers you win the contents of that box.");
		output.add("We open the other boxes one by one to find out which values *aren't* in your own box.");
		output.add("The first offer comes after five boxes are opened, after which offers are received every three boxes.");
		output.add("If you take an offer at any time, you win that amount instead of the contents of the final box.");
		output.add("Best of luck, let's start the game...");
		output.add(generateBoard());
		output.add("Opening five boxes...");
		for(int i=0; i<5; i++)
			output.add(openBox());
		output.add("...");
		output.add(generateOffer());
		output.add(generateBoard());
		output.add("Deal or No Deal?");
		return output;
	}

	private String openBox() {
		casesLeft --;
		return String.format("$%,d!",values.pollFirst());
	}

	@Override
	public LinkedList<String> playNextTurn(String pick) {
		LinkedList<String> output = new LinkedList<>();
		String choice = pick.toUpperCase();
		choice = choice.replaceAll("\\s","");
		if(choice.equals("REFUSE") || choice.equals("NODEAL") || choice.equals("ND"))
		{
			output.add("NO DEAL!");
			if(casesLeft == 2)
			{
				output.add("Your box contains...");
				//Update offer here so that it gets picked up as the amount won
				offer = values.pollLast();
				output.add(String.format("$%,d!",offer));
				accept = true;
			}
			else
			{
				output.add("Opening three boxes...");
				for(int i=0; i<3; i++)
					output.add(openBox());
				output.add("...");
				output.add(generateOffer());
				output.add(generateBoard());
				output.add("Deal or No Deal?");
			}
		}
		else if(choice.equals("ACCEPT") || choice.equals("DEAL") || choice.equals("D"))
		{
			accept = true;
			output.add("It's a DONE DEAL!");
			output.add("Your box contained...");
			output.add(String.format("$%,d!",values.pollLast()));
		}
		//If it's neither of those it's just some random string we can safely ignore
		return output;
	}

	private String generateOffer() {
		//Generate "fair deal"
		int totalSqrts = 0;
		for(int i : values)
		{
			totalSqrts += Math.sqrt(i);
		}
		totalSqrts /= casesLeft;
		offer = (int)Math.pow(totalSqrts,2);
		//Add random factor - 1.00-1.30
		int multiplier = (int)((Math.random()*31) + 100);
		offer *= multiplier;
		offer /= 100;
		//Round it off
		if(offer > 250000)
			offer -= (offer%10000);
		else if(offer > 25000)
			offer -= (offer%1000);
		else if(offer > 2500)
			offer -= (offer%100);
		else if(offer > 250)
			offer -= (offer%10);
		//And format the result they want to see
		return String.format("BANK OFFER: $%,d",offer);
	}

	private String generateBoard() {
		StringBuilder output = new StringBuilder();
		output.append("```\n");
		//Header
		output.append("    DEAL OR NO DEAL    \n");
		if(offer > 0)
			output.append(String.format("   OFFER: $%,9d   \n",offer));
		output.append("\n");
		//Main board
		int nextValue = 0;
		for(int i=0; i<VALUE_LIST.size(); i++)
		{
			if(values.contains(VALUE_LIST.get(nextValue)))
			{
				output.append(String.format("$%,9d",VALUE_LIST.get(nextValue)));
			}
			else
			{
				output.append("          ");
			}
			//Order is 0, 11, 1, 12, ..., 9, 20, 10, 21
			nextValue += 11;
			if(nextValue > 21)
				nextValue -= 21;
			//Space appropriately
			output.append(i%2==0 ? "   " : "\n");
		}
		output.append("```");
		return output.toString();
	}

	@Override
	public boolean isGameOver() {
		return (accept);
	}

	@Override
	public int getMoneyWon() {
		return isGameOver() ? offer : 0;
	}

	@Override
	public boolean isBonusGame()
	{
		return BONUS;
	}

	@Override
	public String getBotPick() {
		//Chance to deal is based on offer as percent of average
		int totalValue = 0;
		for(int i : values)
			totalValue += i;
		double average = totalValue / casesLeft;
		double dealChance = offer / average;
		return (Math.random() < dealChance) ? "DEAL" : "NO DEAL";
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
