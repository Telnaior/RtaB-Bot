package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Spectrum implements MiniGame {
	static final String NAME = "Spectrum";
	static final boolean BONUS = true;
	static final int BOARD_SIZE = 25;
	static final int[] VALUES = {0,1000000,2000000,3000000,4000000,5000000,6000000,
		8000000,11000000,15000000,20000000,25000000}; //Bad things happen if this isn't sorted
	static final int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length); //Integer division lol, 25/12 = 2
	int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int baseMultiplier;
	int lastSpace;
	int lastPicked;
	int total;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	
	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
	{
		this.baseMultiplier = baseMultiplier;
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		board.clear();
		for(int i=0; i<VALUES.length; i++)
			for(int j=0; j<NEEDED_TO_WIN; j++)
				board.add(VALUES[i]);
		//Add an extra bomb
		board.add(0);
		Collections.shuffle(board);
		numberPicked = new int[VALUES.length];
		pickedSpaces = new boolean[BOARD_SIZE];
		total = 0;
		//Display instructions
		output.add("For reaching a streak bonus of x12, you have earned the right to play the third bonus game!");
		output.add(String.format("In Spectrum, you can win up to **$%,d**!",100_000_000*baseMultiplier));
		output.add("Pairs of money are hidden on the board, along with three bombs.");
		output.add("If you make a pair, you win that amount and get to keep picking!");
		output.add("The game only ends when you make a pair of bombs.");
		output.add("Good luck, do your best to clean up the board!");
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		if(!isNumber(pick))
		{
			//Random unrelated non-number doesn't need feedback
			return output;
		}
		if(!checkValidNumber(pick))
		{
			output.add("Invalid pick.");
			return output;
		}
		else
		{
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			lastPicked = board.get(lastSpace);
			numberPicked[Arrays.binarySearch(VALUES,lastPicked)] ++;
			if(numberPicked[Arrays.binarySearch(VALUES,lastPicked)] >= NEEDED_TO_WIN)
				total += lastPicked * baseMultiplier;
			//Print output
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(numberPicked[0] >= (NEEDED_TO_WIN-1))
				output.add("...");
			if(lastPicked == 0)
				output.add("**BOMB**");
			else
				output.add(String.format("$%,d!",lastPicked*baseMultiplier));
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
		display.append("   SPECTRUM   \n");
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
			if((i%5) == 4)
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display how many of each we have, and our total
		display.append("Total So Far: \n");
		display.append(String.format("$%,11d\n",total));
		display.append("\n");
		display.append(String.format("%dx BOMB\n",numberPicked[0]));
		for(int i=1; i<VALUES.length; i++)
		{
			if(numberPicked[i] > 0 && numberPicked[i] < NEEDED_TO_WIN)
				display.append(String.format("%1$dx $%2$,d\n",numberPicked[i],VALUES[i]*baseMultiplier));
		}
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		return numberPicked[0] >= NEEDED_TO_WIN;
	}

	@Override
	public int getMoneyWon()
	{
		if(isGameOver())
			return total;
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