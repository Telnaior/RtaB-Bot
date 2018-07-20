package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class StrikeItRich implements MiniGame {
	final static int BOARD_SIZE = 15;
	final static int[] VALUES = {0,1000,10000,100000,1000000}; //Bad things happen if this isn't sorted
	final static int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length);
	static int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastPicked;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	
	public StrikeItRich()
	{
		for(int i=0; i<VALUES.length; i++)
			for(int j=0; j<NEEDED_TO_WIN; j++)
				board.add(VALUES[i]);
		Collections.shuffle(board);
	}
	@Override
	public void sendNextInput(int pick)
	{
		if(pick > pickedSpaces.length || pickedSpaces[pick])
		{
			lastPicked = -1;
			return;
		}
		else
		{
			pickedSpaces[pick] = true;
			lastPicked = board.get(pick);
			numberPicked[Arrays.binarySearch(VALUES,lastPicked)] ++;
		}
	}

	@Override
	public LinkedList<String> getNextOutput()
	{
		LinkedList<String> output = new LinkedList<>();
		if(lastPicked < 0)
		{
			output.add("Invalid pick.");
		}
		else
		{
			output.add("...");
			output.add(String.format("$%,d!",lastPicked));
		}
		output.add(generateBoard());
		return output;
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
			if(i%VALUES.length==(VALUES.length-1))
				display.append("\n");
			else
				display.append(" ");
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
}
