package tel.discord.rtab.minigames;

import java.util.LinkedList;
import tel.discord.rtab.objs.Dice;

public class ShutTheBox implements MiniGame {
	static final String NAME = "Shut the Box";
	static final boolean BONUS = false;
	static final int BOARD_SIZE = 9;
	static final int MAX_SCORE = BOARD_SIZE * (BOARD_SIZE+1) / 2;
	boolean[] closedSpaces = new boolean[BOARD_SIZE];
	Dice dice; 
	boolean isAlive;  
	boolean isClosing;
	boolean upperThirdClosed;
	byte totalShut;

	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		closedSpaces = new boolean[BOARD_SIZE];
		dice = new Dice();
		isAlive = true;
		isClosing = true;
		upperThirdClosed = false;
		totalShut = 0;
		
		//Display instructions
		output.add("In Shut the Box, you will be given a pair of six-sided dice"
				+ " and a box with the numbers 1 through 9 on it.");
		output.add("Your objective is to close all nine numbers.");
		output.add("Each time you roll the dice, you may close one or more " +
				"numbers that total *exactly* the amount thrown.");
		output.add("For each number you successfully close, you will earn " +
				"$25,000 times that number. The top prize is $1,950,000.");
		output.add("You are free to stop after any roll, but if you can't " +
				"exactly close the number thrown, you lose everything.");
		output.add("However, if you succeed in closing all numbers from 7 to 9"
				+ " inclusive, you have to roll only one die for the rest of " +
				"the game.");
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
				dice.rollDice();
				output.add("You rolled: " + dice.toString());
				if (isGood(dice.getDiceTotal())) {
					if (totalShut + dice.getDiceTotal() == MAX_SCORE) {
						output.add("Congratulations, you shut the box!");
						totalShut = MAX_SCORE; // essentially closes the remaining numbers automatically
					}
					else {
						isClosing = true;
						if (dice.getDiceTotal() <= 2)
						output.add("The only number you can mathematically "
								+ "close is " + dice.getDiceTotal() + 
								" itself, so just type 'CLOSE " +
								dice.getDiceTotal() + "' to continue.");
						else {
							String help = "You may close " + dice.getDiceTotal()
									+ "itself if it is open by typing 'CLOSE " +
									dice.getDiceTotal() + "', or you may close "
									+ "any combination of numbers that total " +
									dice.getDiceTotal() + " (for example, " +
									"'CLOSE ";
								if(!upperThirdClosed) {
									int[] faces = dice.getDice();
									help += faces[0] + " " + faces[1];
								}
								else {
									help += "1 " + (dice.getDiceTotal()-1);
								}
								help += "' if both of those numbers are open).";
								output.add(help);
						}
					}
				}
				else {
					output.add("Unfortunately, you cannot close " +
							dice.getDiceTotal() + " exactly, so you have lost. "
							+ "Sorry.");
					totalShut = 0;
					isAlive = false;
				}
			}
			return output;
		}
		else {
			if (pick.toUpperCase().startsWith("CLOSE ") ||
					pick.toUpperCase().startsWith("SHUT ")) {
				String[] tokens = pick.split("\\s");
				
				// If there are any non-numeric tokens after "CLOSE" or "SHUT", assume it's just the player talking
				for (int i = 1; i < tokens.length; i++) {
					if (!isNumber(tokens[i]))
						return output;
				}
				
				// Make sure the numbers are actually in range and open
				for (int i = 1; i < tokens.length; i++) {
					if (Integer.parseInt(tokens[i]) < 1 ||
							Integer.parseInt(tokens[i]) > 9 ||
							closedSpaces[Integer.parseInt(tokens[i])-1]) {
						output.add("Invalid number(s).");
						return output;
					}
				}
				
				// Duplicates are not allowed, so check for those
				for (int i = 1; i < tokens.length - 1; i++)
					for (int j = i + 1; i < tokens.length - 1; i++)
						if (tokens[i].equals(tokens[j])) {
							output.add("You can't duplicate a number.");
							return output;
						}
				
				// Now we can sum everything and make sure it actually matches the roll
				int totalTryingToClose = 0;
				for (int i = 1; i < tokens.length - 1; i++)
					totalTryingToClose += Integer.parseInt(tokens[i]);
				
				if (totalTryingToClose == dice.getDiceTotal()) {
					for (int i = 1; i < tokens.length - 1; i++)
						closedSpaces[Integer.parseInt(tokens[i])] = true;
					isClosing = false;
					output.add("Numbers closed.");
					output.add(generateBoard());
					output.add("ROLL again if you dare, or type STOP to claim" +
							" your prize of " + String.format("$%,d.",
							getMoneyWon()));
					return output;
				}
				else {
					output.add("That does not total the amount thrown.");
					return output;
				}
			}
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
		return (location >= 0 && location < BOARD_SIZE && !closedSpaces
				[location]);
	}
	
	String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("  SHUT THE BOX");
		for(int i=0; i<BOARD_SIZE; i++)
		{
			if(closedSpaces[i])
			{
				display.append("  ");
			}
			else
			{
				display.append(i+1).append(" ");
			}
		}
		display.append("```");
		return display.toString();
	}

	/**
	 * 
	 * @param roll the number to be checked
	 * @return true if there are distinct open numbers that can be closed with
	 *		 the roll, false otherwise
	 */
	boolean isGood(int roll) {
		if (!closedSpaces[roll-1])
			return true;
		if (roll >= 3) {
			for (int i = 1; i < roll/2; i++) {
				if (closedSpaces[i-1])
					continue;
				if (!closedSpaces [roll-i-1])
					return true;
				if (roll >= 6) {
					for (int j = i + 1; j < (roll-i) / 2 ; j++) {
						if (closedSpaces[j-1])
							continue;
						if (!closedSpaces [roll-i-j-1])
							return true;
						if (roll >= 10) {
							for (int k = j + 1; j < (roll-i-j) / 2 ; j++) {
								if (closedSpaces[j-1])
									continue;
								if (!closedSpaces [roll-i-j-k-1])
									return true;
							} // end for (int k = j + 1; j < (roll-i-j) / 2 ; j++)
						} // end if (roll >= 10)
					} // end for (int j = i + 1; j < (roll-i) / 2 ; j++)
				} // end if (roll >= 6)
			} // end for (int i = 1; i < roll/2; i++)
		} // end if (roll >= 3)
		return false;
	}
		
	public int rollValue(int roll) {
		if (!isGood(roll))
			return getMoneyWon() * -1;
		if (totalShut + roll == MAX_SCORE)
			return 2000000 - getMoneyWon();
		return (totalShut + roll) * 1000;
	}
	
	@Override
	public boolean isGameOver()
	{
		return (!isAlive || totalShut == MAX_SCORE);
	}

	@Override
	public int getMoneyWon()
	{
		if (totalShut == MAX_SCORE)
			return 2000000;
		else return (totalShut*(totalShut+1)/2) * 1000;
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
