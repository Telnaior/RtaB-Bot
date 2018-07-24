package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class DeucesWild implements MiniGame {
	static final boolean BONUS = false;
	final static int BOARD_SIZE = 52;
	final static int[] VALUES = {0,1000,10000,100000,1000000}; //Bad things happen if this isn't sorted
	final static int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length);
	static int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastSpace;
	int lastPicked;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	boolean firstPlay = true;
	boolean pinchMode = false;
	
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
			if(numberPicked[Arrays.binarySearch(VALUES,lastPicked)] >= (NEEDED_TO_WIN-1))
				pinchMode = true;
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
			Collections.shuffle(board);
			numberPicked = new int[VALUES.length];
			pickedSpaces = new boolean[BOARD_SIZE];
			pinchMode = false;
			//Display instructions
			output.add("In Deuces Wild, your objective is to obtain the best poker hand possible.");
			output.add("We have shuffled a standard deck of 52 playing cards, from which you will pick five cards.");
			output.add("As the name of the game suggests, deuces are wild. Those are always treated as the best card possible");
			output.add("After you draw your five cards, you may redraw as many of them as you wish, but only once.");
			output.add("You must get at least a three of a kind to win any money. That pays $50,000.");
            output.add("Straights and flushes each pay $100,000. A full house pays $150,000, a four of a kind pays $250,000, "
                    + "a straight flush pays $450,000, a five of a kind pays $750,000, a wild royal flush pays $1,250,000, "
                    + "and four deuces pay $10,000,000.");
            output.add("If you are lucky enough to get a natural royal flush, you will win $40,000,000!");
			output.add("Best of luck! Pick your first space when you're ready.");
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
			if(pinchMode)
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
		display.append("DEUCES WILD\n");
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
		firstPlay = true;
		return lastPicked;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
}