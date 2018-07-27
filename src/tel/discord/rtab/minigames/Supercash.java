package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Supercash implements MiniGame {
	static final boolean BONUS = true;
	final static int BOARD_SIZE = 24;
	final static int MAX_VALUE = 10000000;
	final static int[] VALUES = {0,500000,1000000,2000000,3000000,4000000,5000000,
		6000000,7000000,8000000,9000000,MAX_VALUE}; //Bad things happen if this isn't sorted
	final static int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length);
	static int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastSpace;
	int lastPicked;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	
	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		board.clear();
		for(int i=0; i<VALUES.length; i++)
			for(int j=0; j<NEEDED_TO_WIN; j++)
				board.add(VALUES[i]);
		//Switch one of the lowest values for an extra copy of the highest value
		board.set(0,MAX_VALUE);
		Collections.shuffle(board);
		numberPicked = new int[VALUES.length];
		pickedSpaces = new boolean[BOARD_SIZE];
		//Display instructions
		output.add("For reaching a bonus multiplier of x5, you have earned the right to play the first bonus game!");
		output.add("In Supercash, you can win up to ten million dollars!");
		output.add("Hidden on the board are three \"$10,000,000\" spaces, simply pick them all to win.");
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
			numberPicked[Arrays.binarySearch(VALUES,lastPicked)] ++;
			output.add(String.format("Space %d selected...",lastSpace+1));
			output.add("...");
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
			if((i%(VALUES.length/2)) == ((VALUES.length/2)-1))
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display how many of each we have
		for(int i=1; i<VALUES.length; i++)
		{
			if(numberPicked[i] > 0)
				display.append(String.format("%1$dx $%2$,d\n",numberPicked[i],VALUES[i]));
		}
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		for(int i=0; i<VALUES.length; i++)
		{
			//Lowest amount is easier to win
			if(i == 0)
			{
				if(numberPicked[i] >= (NEEDED_TO_WIN-1))
					return true;
			}
			//Highest amount is harder to win
			else if(i == (VALUES.length-1))
			{
				if(numberPicked[i] >= (NEEDED_TO_WIN+1))
					return true;
			}
			//Other amounts are normal rarity
			else
			{
				if(numberPicked[i] >= NEEDED_TO_WIN)
					return true;
			}
		}
		return false;
	}

	@Override
	public int getMoneyWon()
	{
		if(isGameOver())
			return lastPicked;
		else
			return 0;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
}
