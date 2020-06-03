package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import tel.discord.rtab.enums.Jackpots;

public class Supercash implements MiniGame {
	static final String NAME = "Supercash";
	static final boolean BONUS = true;
	static final int BOARD_SIZE = 24;
	int maxValue, baseMultiplier;
	int[] values = {0,500000,1000000,2000000,3000000,4000000,5000000,
			6000000,7000000,8000000,9000000,-1}; //Bad things happen if this isn't sorted
	int[] numberPicked = new int[values.length];
	int neededToWin = BOARD_SIZE/values.length;
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastSpace;
	int lastPicked;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	String channelID;
	
	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
	{
		this.channelID = channelID;
		this.baseMultiplier = baseMultiplier;
		maxValue = Jackpots.SUPERCASH.getJackpot(channelID);
		values[values.length-1] = maxValue;
		for(int i = 0; i<values.length; i++)
			values[i] *= baseMultiplier;
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		board.clear();
		for(int i=0; i<values.length; i++)
			for(int j=0; j<neededToWin; j++)
				board.add(values[i]);
		//Switch one of the lowest values for an extra copy of the highest value
		board.set(0,values[values.length-1]);
		Collections.shuffle(board);
		numberPicked = new int[values.length];
		pickedSpaces = new boolean[BOARD_SIZE];
		//Display instructions
		output.add("For reaching a streak bonus of x4, you have earned the right to play the first bonus game!");
		output.add("In Supercash, you can win a jackpot of up to "+String.format("$%,d!",values[values.length-1]));
		output.add("Hidden on the board are three jackpot spaces, simply pick them all to win.");
		output.add("There are also other, lesser values, make a pair of those to win that amount instead.");
		output.add("Oh, and there's also a single bomb hidden somewhere on the board. If you pick that, you win nothing.");
		output.add("Best of luck! Make your first pick when you are ready.");
		output.add(generateBoard());
		return output;
	}
	
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		if(!isNumber(pick))
		{
			//Random unrelated non-number doesn't need feedback
			return output;
		}
		else if(!checkValidNumber(pick))
		{
			output.add("Invalid pick.");
			return output;
		}
		else
		{
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			lastPicked = board.get(lastSpace);
			numberPicked[Arrays.binarySearch(values,lastPicked)] ++;
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(lastPicked == 0)
				output.add("**BOOM**");
			else
				output.add(String.format("$%,d!",lastPicked));
			output.add(generateBoard());
			return output;
		}
	}

	boolean isNumber(String message)
	{
		try
		{
			//If this doesn't throw an exception we're good
			Integer.parseInt(message);
			return true;
		}
		catch(NumberFormatException e1)
		{
			return false;
		}
	}
	boolean checkValidNumber(String message)
	{
		int location = Integer.parseInt(message)-1;
		return (location >= 0 && location < BOARD_SIZE && !pickedSpaces[location]);
	}

	String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("    SUPERCASH    \n");
		for(int i=0; i<BOARD_SIZE; i++)
		{
			if(pickedSpaces[i])
			{
				display.append("  ");
			}
			else
			{
				display.append(String.format("%02d",(i+1)));
			}
			if((i%(values.length/2)) == ((values.length/2)-1))
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display how many of each we have
		for(int i=1; i<values.length; i++)
		{
			if(numberPicked[i] > 0)
				display.append(String.format("%1$dx $%2$,d\n",numberPicked[i],values[i]));
		}
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		for(int i=0; i<values.length; i++)
		{
			//Lowest amount is easier to win
			if(i == 0)
			{
				if(numberPicked[i] >= (neededToWin-1))
					return true;
			}
			//Highest amount is harder to win
			else if(i == (values.length-1))
			{
				if(numberPicked[i] >= (neededToWin+1))
					return true;
			}
			//Other amounts are normal rarity
			else
			{
				if(numberPicked[i] >= neededToWin)
					return true;
			}
		}
		return false;
	}

	@Override
	public int getMoneyWon()
	{
		if(isGameOver())
		{
			//Return the last value selected - but before then, figure out whether we need to increment or reset the jackpot
			if(lastPicked == values[values.length-1])
				Jackpots.SUPERCASH.resetJackpot(channelID);
			else
				Jackpots.SUPERCASH.setJackpot(channelID, maxValue+100_000);
			return lastPicked;
		}
		else
			return 0;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
	
	@Override
	public String getBotPick()
	{
		ArrayList<Integer> openSpaces = new ArrayList<>(BOARD_SIZE);
		for(int i=0; i<BOARD_SIZE; i++)
			if(!pickedSpaces[i])
				openSpaces.add(i+1);
		return String.valueOf(openSpaces.get((int)(Math.random()*openSpaces.size())));
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
