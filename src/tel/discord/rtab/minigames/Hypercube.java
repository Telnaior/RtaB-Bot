package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Hypercube implements MiniGame {
	static final boolean BONUS = true;
	static final int MAX_PICKS = 10;
	static final int MIN_NUM = 51;
	static final int MAX_NUM = 99;
	static final int BOMBS = 15;
	static final int BOARD_SIZE = (MAX_NUM-MIN_NUM+1)+BOMBS;
	int picksUsed;
	int total;
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	int lastSpace;
	int lastPicked;

	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		board.clear();
		//Add the numbers
		for(int i=MIN_NUM; i<=MAX_NUM; i++)
			board.add(i);
		//Add the zeroes and bombs too
		Integer[] bombBlock = new Integer[BOMBS];
		Arrays.fill(bombBlock,0);
		board.addAll(Arrays.asList(bombBlock));
		Collections.shuffle(board);
		pickedSpaces = new boolean[BOARD_SIZE];
		picksUsed = 0;
		total = 0;
		//Display instructions
		output.add("For reaching a streak bonus of x20, you have earned the right to play the final bonus game!");
		output.add("In Hypercube, you can win hundreds of millions of dollars!");
		output.add("You have ten picks to build up the largest total you can by selecting the largest numbers.");
		output.add("But if you find one of the fifteen bombs, your total will be reset to zero!");
		output.add("Once you've made all ten picks, your total is cubed and the result is the money you win.");
		output.add("Good luck, go for a top score!");
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
			if(lastPicked == 0)
				total = 0;
			else
			{
				total += lastPicked;
				picksUsed ++;
			}
			//Print output
			output.add(String.format("Space %d selected...",lastSpace+1));
			output.add("...");
			if(lastPicked == 0)
				output.add("**BOOM**");
			else
				output.add(String.format("**%2d**",lastPicked));
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
		display.append("   H Y P E R C U B E   \n");
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
			if((i%Math.sqrt(BOARD_SIZE)) == (Math.sqrt(BOARD_SIZE)-1))
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display our total and the cash it converts to
		display.append(String.format("   Total So Far: %03d   \n",total));
		display.append(String.format("     $ %,11d     \n",(int)Math.pow(total,3)));
		if(picksUsed == (MAX_PICKS-1))
			display.append(String.format("    %02d Pick Remains    \n",(MAX_PICKS-picksUsed)));
		else
			display.append(String.format("    %02d Picks Remain    \n",(MAX_PICKS-picksUsed)));
		display.append("```");
		return display.toString();
	}
	
	@Override
	public boolean isGameOver() {
		return (picksUsed >= MAX_PICKS);
	}

	@Override
	public int getMoneyWon() {
		if(isGameOver())
			return (int) Math.pow(total,3);
		else
			return 0;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}

}
