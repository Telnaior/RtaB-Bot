package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Spectrum implements MiniGame {
	static final boolean BONUS = true;
	final static int BOARD_SIZE = 25;
	final static int[] VALUES = {0,1000000,2000000,3000000,4000000,5000000,6000000,
		8000000,11000000,15000000,20000000,25000000}; //Bad things happen if this isn't sorted
	final static int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length); //Integer division lol, 25/12 = 2
	static int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastSpace;
	int lastPicked;
	int total;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	boolean firstPlay = true;
	
	@Override
	public void sendNextInput(String pick)
	{
		if(!isNumber(pick))
		{
			lastPicked = -2;
			return;
		}
		if(!checkValidNumber(pick))
		{
			lastPicked = -1;
			return;
		}
		else
		{
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			lastPicked = board.get(lastSpace);
			numberPicked[Arrays.binarySearch(VALUES,lastPicked)] ++;
			if(numberPicked[Arrays.binarySearch(VALUES,lastPicked)] >= NEEDED_TO_WIN)
				total += lastPicked;
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

	@Override
	public LinkedList<String> getNextOutput()
	{
		LinkedList<String> output = new LinkedList<>();
		if(firstPlay)
		{
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
			output.add("For reaching a bonus multiplier of x15, you have earned the right to play the third bonus game!");
			output.add("In Spectrum, you can win up to one hundred million dollars!");
			output.add("Pairs of money are hidden on the board, along with three bombs.");
			output.add("If you make a pair, you win that amount and get to keep picking!");
			output.add("The game only ends when you make a pair of bombs.");
			output.add("Good luck, do your best to clean up the board!");
			firstPlay = false;
		}
		else if(lastPicked == -2)
		{
			//Random unrelated non-number doesn't need feedback
			return output;
		}
		else if(lastPicked == -1)
		{
			output.add("Invalid pick.");
		}
		else
		{
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(numberPicked[0] >= (NEEDED_TO_WIN-1))
				output.add("...");
			if(lastPicked == 0)
				output.add("**BOMB**");
			else
				output.add(String.format("$%,d!",lastPicked));
		}
		output.add(generateBoard());
		return output;
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
				display.append(String.format("%1$dx $%2$,d\n",numberPicked[i],VALUES[i]));
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
		firstPlay = true;
		return total;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
}