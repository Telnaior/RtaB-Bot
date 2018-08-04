package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class ShutTheBox implements MiniGame {
    static final String NAME = "Shut the Box";
	static final boolean BONUS = false;
	static final int BOARD_SIZE = 12;

	boolean[] closedSpaces = new boolean[BOARD_SIZE];
	boolean isAlive;
    boolean isClosing;
    boolean bottomHalfClosed;
    byte totalShut;

	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
        closedSpaces = new boolean[BOARD_SIZE];
        isAlive = true;
        isClosing = true;
        bottomHalfClosed = false;
        totalShut = 0;
		//Display instructions
		output.add("In Shut the Box, you will be given a pair of six-sided dice and a box with the numbers 1 through 12 on it.");
		output.add("Your objective is to close all 12 numbers.");
        output.add("Each time you roll the dice, you may close one or more numbers that total *exactly* the amount thrown.");
        output.add("For each number you successfully close, you will earn $25,000 times that number. The top prize is $1,950,000.");
        output.add("You are free to stop after any roll, but if you can't exactly close the number thrown, you lose everything.");
        output.add("However, if you succeed in closing all numbers from 7 to 12 inclusive, you have to roll only one die for the rest of the game.");
        output.add("Good luck!");
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		
		if (!isClosing) {
			if (pick.toUpperCase().equals("STOP")) {
				isAlive = false;
			}
			else if (pick.toUpperCase().equals("ROLL")) {
				isClosing = false;
			}
			return output;
		}
		else {
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
		display.append("  SHUT THE BOX");
		for(int i=0; i<BOARD_SIZE; i++)
		{
            if (i % 6 == 0) {
                display.append("\n");
            }

			if(closedSpaces[i])
			{
				display.append("  ");
			}
			else
			{
				display.append(String.format("%02d",(i+1)));
			}
		}
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		return (!isAlive || totalShut == 78); // 78 is 1 + 2 + 3 + ... + 12
	}

	@Override
	public int getMoneyWon()
	{
		return totalShut * 25000;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}

	@Override
	public String getBotPick() {
        // TODO: Teach the bot how to play :P
        return "";
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
