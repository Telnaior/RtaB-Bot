package tel.discord.rtab.minigames;

import java.util.LinkedList;
import tel.discord.rtab.objs.Dice;

public class ShutTheBox implements MiniGame {
	static final String NAME = "Shut the Box";
	static final boolean BONUS = false;
	static final int BOARD_SIZE = 9;
	static final int MAX_SCORE = BOARD_SIZE * (BOARD_SIZE+1) / 2;
	boolean[] closedSpaces;
	Dice dice;
	boolean[] isGood;
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
		isGood = new boolean[dice.getDice().length * dice.getNumFaces() -
				(dice.getDice().length - 1)];
		for (int i = 0; i < isGood.length; i++)
			isGood[i] = true;
		isAlive = true;
		isClosing = true;
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
				if (isGood[dice.getDiceTotal() - 2]) {
					if (totalShut + dice.getDiceTotal() == MAX_SCORE) {
						output.add("Congratulations, you shut the box!");
						totalShut = MAX_SCORE; // essentially closes the remaining numbers automatically
					}
					if (totalShut + dice.getDiceTotal() == MAX_SCORE - 1) { // ARGH!!!
						output.add("Oh, so close, yet you couldn't shut the " + 
								"1 :frowning2: We'll close that for you, then "
								+ "we'll give you your consolation prize.");
						totalShut = MAX_SCORE - 1; // essentially closes the remaining numbers except 1 automatically
						isAlive = false;
					}
					else {
						isClosing = true;
						output.add("You may now close one or more numbers that "
								+ "total " + dice.getDiceTotal() + "by typing "
								+ "'SHUT' followed by all numbers you would " +
								"like to close.");
					}
				}
				else {
					output.add("That is unfortunately a bad roll. Sorry.");
					totalShut = 0;
					isAlive = false;
				}
			}
			return output;
		}
		else {
			if (pick.toUpperCase().startsWith("SHUT ") ||
					pick.toUpperCase().startsWith("CLOSE ")) {
				
				// Prevent accidentally stopping with nothing if the player hasn't rolled yet
				if (totalShut == 0)
					return output;
				
				String[] tokens = pick.split("\\s");
				
				// If there are any non-numeric tokens after "SHUT" or "CLOSE", assume it's just the player talking
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
					totalShut += dice.getDiceTotal();
					isGood = refreshGood();
					isClosing = false;
					output.add("Numbers closed.");
					output.add(generateBoard());
					output.add("ROLL again if you dare, or type STOP to stop " +
							"with your total.");
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
				display.append((i+1) + " ");
			}
		}
		display.append("\n Points:      " + String.format("% 2d", totalShut));
		display.append("\n Total: $" + String.format("% 6,d", getMoneyWon()));
		display.append("\n\n   Good rolls:");
		for (int i = 0; i < isGood.length; i++) {
			if (isGood[i])
				display.append("\n " + String.format("% 2d", i+2) + ": +$" +
						String.format("% 7,d", rollValue(i+2)));
		}
		display.append("```");
		return display.toString();
	}

	boolean[] refreshGood() {
		boolean[] isStillGood = new boolean[isGood.length];
		
		// The numbers that are still open besides 1 are obviously still good, so start there.
		for (int i = 1; i < closedSpaces.length; i++)
			isStillGood[i-1] = !closedSpaces[i];
		
		for (int i = 1; i <= closedSpaces.length; i++) {
			// If the corresponding space is closed, don't bother with it
			if (closedSpaces[i-1])
				continue;
			for (int j = i+1; i+j < isStillGood.length + 2; j++) {
				if (closedSpaces[j-1])
					continue;
				// i-1 and j-1 must both be open; otherwise we wouldn't reach this point in the code
				isStillGood[i+j-2] = true;
				for (int k = j+1; i+j+k < isStillGood.length + 2; k++) {
					if (closedSpaces[k-1])
						continue;
					isStillGood[i+j+k-2] = true;
					for (int l = k+1; i+j+k+l < isStillGood.length + 2; l++) {
						if (closedSpaces[l-1])
							continue;
						isStillGood[i+j+k+l-2] = true;
					}
				}
			}
		}
		
		return isStillGood;
	}
		
	public int rollValue(int roll) {
		if (isGood[roll-2])
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
