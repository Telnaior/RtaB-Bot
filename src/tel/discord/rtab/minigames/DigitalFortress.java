package tel.discord.rtab.minigames;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DigitalFortress implements MiniGame {
	static final boolean BONUS = true;
	static final int ATTEMPTS_ALLOWED = 4;
	List<Character> solution = Arrays.asList('0','1','2','3','4','5','6','7','8','9');
	String[] guesses = new String[ATTEMPTS_ALLOWED];
	boolean[] lockedIn;
	int digitsCorrect;
	int attemptsLeft = ATTEMPTS_ALLOWED;
	boolean invalid = false;
	boolean firstPlay = true;
	
	@Override
	public void sendNextInput(String pick) {
		// TODO Auto-generated method stub
		if(!isValidNumber(pick))
		{
			invalid = true;
			return;
		}
		invalid = false;
		//Subtract an attempt and record the guess
		attemptsLeft --;
		guesses[attemptsLeft] = pick;
		//Check to see how many digits are right
		for(int i=0; i<solution.size(); i++)
		{
			if(solution.get(i).equals(guesses[attemptsLeft].charAt(i)) && !lockedIn[i])
			{
				lockedIn[i] = true;
				digitsCorrect++;
			}
		}
	}

	private boolean isValidNumber(String message) {
		try
		{
			//If this doesn't throw an exception we're good
			Integer.parseInt(message);
			//Needs to be exactly ten digits
			return (message.length() == 10);
		}
		catch(NumberFormatException e1)
		{
			return false;
		}
	}

	@Override
	public LinkedList<String> getNextOutput() {
		LinkedList<String> output = new LinkedList<>();
		if(firstPlay)
		{
			//Initialise stuff
			Collections.shuffle(solution);
			lockedIn = new boolean[solution.size()];
			digitsCorrect = 0;
			attemptsLeft = ATTEMPTS_ALLOWED;
			//Provide help
			output.add("For reaching a bonus multiplier of x10, you have earned the right to play the second bonus game!");
			output.add("In Digital Fortress, you can win up to twenty-five million dollars!");
			output.add("The computer has created a secret ten-digit passcode, using each digit once and once only.");
			output.add("Your job is to guess this passcode.");
			output.add("You have four attempts to do so, "
					+ "and after each attempt you will be told which digits are in the right place.");
			output.add("Once you have solved the passcode (or been locked out after four attempts), "
					+ "you will earn $2,500,000 for each digit you had correct.");
			output.add("Submit your first guess at the passcode when you are ready, and good luck!");
			firstPlay = false;
		}
		else if(invalid)
		{
			//Non-number or wrong size doesn't need feedback
			return output;
		}
		else
		{
			output.add("Submitting "+guesses[attemptsLeft]+"...");
			output.add("...");
			if(digitsCorrect == solution.size())
				output.add(digitsCorrect + " digits correct, congratulations!");
			else if(digitsCorrect == 1)
				output.add(digitsCorrect + " digit correct.");
			else
				output.add(digitsCorrect + " digits correct.");			
		}
		if(digitsCorrect < 10)
			output.add(generateBoard());
		return output;
	}

	private String generateBoard() {
		StringBuilder board = new StringBuilder();
		board.append("```\n");
		board.append("DIGITAL FORTRESS\n");
		//Counting down in a loop, spooky
		for(int i=3; i>=attemptsLeft; i--)
		{
			board.append("   ");
			board.append(guesses[attemptsLeft]);
			board.append("\n");
		}
		if(attemptsLeft == 0)
			board.append("  ------------\n");
		board.append("   ");
		for(int i=0; i<solution.size(); i++)
		{
			if(lockedIn[i] || attemptsLeft == 0)
				board.append(solution.get(i));
			else
				board.append("-");
		}
		return board.toString();
	}

	@Override
	public boolean isGameOver() {
		return (digitsCorrect == solution.size() || attemptsLeft <= 0);
	}

	@Override
	public int getMoneyWon() {
		return (2500000 * digitsCorrect);
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}

}
