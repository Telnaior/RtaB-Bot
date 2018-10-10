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
//	String[] botStrategy; // temporary comment-out; the function that generates this is not working
	boolean isAlive;  
	boolean isClosing;
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
//		botStrategy = new String[isGood.length]; // temporary comment-out; the function that generates this is not working
		for (int i = 0; i < isGood.length; i++) {
			isGood[i] = true;
//			botStrategy[i] = getBotStrategy(i+2, false); // temporary comment-out; the generator function is not working
		}
		isAlive = true;
		isClosing = false;
		totalShut = 0;
		
		//Display instructions
		output.add("In Shut the Box, you will be given a pair of six-sided dice"
				+ " and a box with the numbers 1 through 9 on it.");
		output.add("Your objective is to close all nine numbers.");
		output.add("Each time you roll the dice, you may close one or more " +
				"numbers that total *exactly* the amount thrown.");
		output.add("For each number you successfully close, you will earn " +
				"as many points as the amount thrown. The first point is " +
				"worth $1,000, with each additional point worth $1,000 more " +
				"than the previous.");
		output.add("If you shut the box completely, we'll augment your " +
				   "winnings to $2,000,000!");
		output.add("You are free to stop after any roll, but if you can't " +
				"exactly close the number thrown, you lose everything.");
		output.add("Good luck! Type ROLL when you're ready.");
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		
		if (!isClosing) {
			if (pick.toUpperCase().equals("STOP")) {
				// Prevent accidentally stopping with nothing if the player hasn't rolled yet
				if (totalShut == 0)
					return output;
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
						output.add("What number(s) totaling " + dice.getDiceTotal()
								+ " would you like to close?");
					}
				}
				else {
					output.add("That is unfortunately a bad roll. Sorry.");
					totalShut = 0;
					isAlive = false;
				}
			}
		}
		else {
			String[] tokens = pick.split("\\s");
			for (int i = 0; i < tokens.length; i++) {
				if (!isNumber(tokens[i]))
					return output;
			}
				
			// Make sure the numbers are actually in range and open
			for (int i = 0; i < tokens.length; i++) {
				if (Integer.parseInt(tokens[i]) < 1 ||
						Integer.parseInt(tokens[i]) > 9 ||
						closedSpaces[Integer.parseInt(tokens[i])-1]) {
					output.add("Invalid number(s).");
					return output;
				}
			}
				
			// Duplicates are not allowed, so check for those
			for (int i = 0; i < tokens.length; i++)
				for (int j = i + 1; j < tokens.length; j++)
					if (tokens[i].equals(tokens[j])) {
						output.add("You can't duplicate a number.");
						return output;
					}
				
			// Now we can sum everything and make sure it actually matches the roll
			int totalTryingToClose = 0;
			for (int i = 0; i < tokens.length; i++)
				totalTryingToClose += Integer.parseInt(tokens[i]);
			
			if (totalTryingToClose == dice.getDiceTotal()) {
				for (int i = 0; i < tokens.length; i++)
					closedSpaces[Integer.parseInt(tokens[i])-1] = true;
				totalShut += dice.getDiceTotal();
				isGood = refreshGood();
//				for (int i = 0; i < botStrategy.length; i++)     // temporary comment-out; the generator
//					botStrategy[i] = getBotStrategy(i+2, false); // function is not working
				isClosing = false;
				output.add("Numbers closed.");
				output.add(generateBoard());
				output.add("ROLL again if you dare, or type STOP to stop " +
						"with your total.");
			}
			else {
				output.add("That does not total the amount thrown.");
			}
		}
		return output;
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
		display.append("  SHUT THE BOX\n");
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
		display.append("\n Points:      " + String.format("%2d", totalShut));
		display.append("\n Total: $" + String.format("%,7d", getMoneyWon()));
		display.append("\n\n Possible Rolls:");
		for (int i = 0; i < isGood.length; i++) {
			if (isGood[i])
				display.append("\n " + String.format("%2d", i+2) + ": +$" +
						String.format("%,9d", rollValue(i+2)));
			else
				display.append("\n " + String.format("%2d", i+2) + ":   BAD ROLL");
		}
		display.append("\n```");
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
			for (int j = i+1; i+j < isStillGood.length + 2 && j <= closedSpaces.length; j++) {
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
		if (!isGood[roll-2])
			return getMoneyWon() * -1;
		if (totalShut + roll == MAX_SCORE)
			return 2000000 - getMoneyWon();
		return ((totalShut+roll)*((totalShut+roll)+1)/2) * 1000 - getMoneyWon();
	}
	
	/* FIXME: Function is not working. It is known to break with 2, one of {4 6}, and 9 shut.
	public String getBotStrategy(int roll, boolean dbl) {
		if (roll == 1) {
			if (!closedSpaces[0])
				return " 1";
			else return null;
		}
		if (!isGood[roll-2])
			return null;
		
		/* The reason for the dbl parameter is so the bot doesn't get something
		 * like, say, 5 5 for a strategy for 10; the first condition prevents
		 * that.
		 */
/*		if (!dbl && roll < 10 && !closedSpaces[roll - 1])
			return " " + roll;
			
		for (int i = Math.min(roll-1, 9); i > 0; i--) {
			if (i < roll-i)
				break; // and throw our error
			if (!isGood[i-2])
				continue;
			if (i == roll-i)
				return " " + (roll - i) + getBotStrategy(i, true);
			if (isGood[roll - i - 2])
				return " " + i + getBotStrategy(roll-i, false);
		}
		throw new IllegalArgumentException("Uh-oh--something's wrong with the" +
				" Shut the Box combination generator! Tell StrangerCoug.");
	}
*/	
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
		if (isClosing) {
//			return botStrategy[dice.getDiceTotal() - 1]; // temporary comment out; generator function is not working
// Temporary switch statement to hardcode desired strategy; will be removed when the
// getBotStrategy() function is fixed.
			switch (dice.getDiceTotal()) {
				case 2: return "2";
				case 3: if (!closedSpaces[2]) return "3";
					else return "2 1";
				case 4: if (!closedSpaces[3]) return "4";
					else return "3 1";
				case 5: if (!closedSpaces[4]) return "5";
					else if (!closedSpaces[3] && !closedSpaces[0])
						return "4 1";
					else return "3 2";
				case 6: if (!closedSpaces[5]) return "6";
					else if (!closedSpaces[4] && !closedSpaces[0]) return "5 1";
					else if (!closedSpaces[3]) return "4 2";
					else return "3 2 1";
				case 7: if (!closedSpaces[6]) return "7";
					else if (!closedSpaces[5] && !closedSpaces[0]) return "6 1";
					else if (!closedSpaces[4] && !closedSpaces[1]) return "5 2";
					else {
						if (!closedSpaces[2]) return "4 3";
						else return "4 2 1";
					}
				case 8: if (!closedSpaces[7]) return "8";
					else if (!closedSpaces[6] && !closedSpaces[0]) return "7 1";
					else if (!closedSpaces[5] && !closedSpaces[1]) return "6 2";
					else {
						if (!closedSpaces[2]) return "5 3";
						else return "5 2 1";
					}
				case 9: if (!closedSpaces[8]) return "9";
					else if (!closedSpaces[7] && !closedSpaces[0]) return "8 1";
					else if (!closedSpaces[6] && !closedSpaces[1]) return "7 2";
					else if (!closedSpaces[5]) {
						if (!closedSpaces[2]) return "6 3";
						else if (!closedSpaces[1] && !closedSpaces[0]) return "6 2 1";
					}
					else {
						if (!closedSpaces[3]) return "5 4";
						else return "5 3 1";
					}
				case 10: if (!closedSpaces[8] && !closedSpaces[0]) return "9 1";
					else if (!closedSpaces[7] && !closedSpaces[1]) return "8 2";
					else if (!closedSpaces[6]) {
						if (!closedSpaces[2]) return "7 3";
						else if (!closedSpaces[1] && !closedSpaces[0])  return "7 2 1";
					}
					else if (!closedSpaces[5]) {
						if (!closedSpaces[3]) return "6 4";
						else if (!closedSpaces[2] && !closedSpaces[0]) return "6 3 1";
					}
					else if (!closedSpaces[4]) {
						if (!closedSpaces[3] && !closedSpaces[0]) return "5 4 1";
						else return "5 3 2";
					}
					else return "4 3 2 1";
				case 11: if (!closedSpaces[8] && !closedSpaces[1]) return "9 2";
					else if (!closedSpaces[7]) {
						if (!closedSpaces[2]) return "8 3";
						else if (!closedSpaces[1] && !closedSpaces[0]) return "8 2 1";
					}
					else if (!closedSpaces[6]) {
						if (!closedSpaces[3]) return "7 4";
						else if (!closedSpaces[2] && !closedSpaces[0]) return "7 3 1";
					}
					else if (!closedSpaces[5]) {
						if (!closedSpaces[4]) return "6 5";
						else if (!closedSpaces[3] && !closedSpaces[0]) return "6 4 1";
						else return "6 3 2";
					}
					else return "5 3 2 1";
				case 12: if (!closedSpaces[8]) {
						if (!closedSpaces[2]) return "9 3";
						else if (!closedSpaces[1] && !closedSpaces[0])  return "9 2 1";
					}
					else if (!closedSpaces[7]) {
						if (!closedSpaces[3]) return "8 4";
						else if (!closedSpaces[2] && !closedSpaces[0])  return "8 3 1";
					}
					else if (!closedSpaces[6]) {
						if (!closedSpaces[4]) return "7 5";
						else if (!closedSpaces[3] && !closedSpaces[0]) return "7 4 1";
						else return "7 3 2";
					}
					else if (!closedSpaces[5]) {
						if (!closedSpaces[3]) return "6 4 2";
						else return "6 3 2 1";
					}
					else return "5 4 2 1";
				default: throw new IndexOutOfBoundsException("Not even your " +
						"hardcoded tempfix works, StrangerCoug? You're fired!");
			}
// End temporary switch statement
		}
		else {
			Dice testDice = new Dice();
			testDice.rollDice();
			if (isGood[testDice.getDiceTotal()-1])
				return "ROLL";
			else return "STOP";
		}
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
