package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class StrikeItRich implements MiniGame {
	static final boolean BONUS = false;
	final static int BOARD_SIZE = 15;
	final static int[] VALUES = {0,1000,10000,100000,1000000}; //Bad things happen if this isn't sorted
	final static int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length);
	static int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastSpace;
	int lastPicked;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	boolean pinchMode = false;
	
	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		board.clear();
		for(int i=0; i<VALUES.length; i++)
			for(int j=0; j<NEEDED_TO_WIN; j++)
				board.add(VALUES[i]);
		Collections.shuffle(board);
		numberPicked = new int[VALUES.length];
		pickedSpaces = new boolean[BOARD_SIZE];
		pinchMode = false;
		//Display instructions
		output.add("In Strike it Rich, your objective is to match three of a kind.");
		output.add("Simply keep choosing numbers until you have three the same, and that is what you will win.");
		output.add("The top prize is $1,000,000!");
		output.add("Make your first pick when you are ready.");
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
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(pinchMode)
				output.add("...");
			output.add(String.format("$%,d!",lastPicked));
			numberPicked[Arrays.binarySearch(VALUES,lastPicked)] ++;
			if(numberPicked[Arrays.binarySearch(VALUES,lastPicked)] >= (NEEDED_TO_WIN-1))
				pinchMode = true;
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
		display.append("STRIKE IT RICH\n");
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
			if((i%VALUES.length) == (VALUES.length-1))
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display how many of each we have
		for(int i=0; i<VALUES.length; i++)
		{
			display.append(String.format("%1$dx $%2$,d\n",numberPicked[i],VALUES[i]));
		}
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		for(int search : numberPicked)
			if(search >= NEEDED_TO_WIN)
				return true;
		return false;
	}

	@Override
	public int getMoneyWon()
	{
		return lastPicked;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
}
