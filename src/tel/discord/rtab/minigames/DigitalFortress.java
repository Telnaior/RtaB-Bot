package tel.discord.rtab.minigames;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class DigitalFortress implements MiniGame {
	static final String NAME = "Digital Fortress";
	static final boolean BONUS = true;
	static final int PRIZE_PER_DIGIT = 2500000;
	static final int ATTEMPTS_ALLOWED = 4;
	List<Character> solution = Arrays.asList('0','1','2','3','4','5','6','7','8','9');
	String[] guesses = new String[ATTEMPTS_ALLOWED];
	boolean[] lockedIn;
	int digitsCorrect;
	int attemptsLeft = ATTEMPTS_ALLOWED;
	
	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise stuff
		Collections.shuffle(solution);
		lockedIn = new boolean[solution.size()];
		digitsCorrect = 0;
		attemptsLeft = ATTEMPTS_ALLOWED;
		//Provide help
		output.add("For reaching a streak bonus of x10, you have earned the right to play the second bonus game!");
		output.add("In Digital Fortress, you can win up to twenty-five million dollars!");
		output.add("The computer has created a secret ten-digit passcode, using each digit once and once only.");
		output.add("Your job is to guess this passcode.");
		output.add("You have four attempts to do so, "
				+ "and after each attempt you will be told which digits are in the right place.");
		output.add("Once you have solved the passcode (or been locked out after four attempts), "
				+ "you will earn $2,500,000 for each digit you had correct.");
		output.add("Submit your first guess at the passcode when you are ready, and good luck!");
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick) {
		LinkedList<String> output = new LinkedList<>();
		if(!isValidNumber(pick))
		{
			//Non-number or wrong size doesn't need feedback
			return output;
		}
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
		//Print output
		output.add("Submitting "+guesses[attemptsLeft]+"...");
		output.add("...");
		if(digitsCorrect == solution.size())
			output.add(digitsCorrect + " digits correct, congratulations!");
		else if(digitsCorrect == 1)
			output.add(digitsCorrect + " digit correct.");
		else
			output.add(digitsCorrect + " digits correct.");
		if(digitsCorrect < 10)
			output.add(generateBoard());
		return output;
	}

	private boolean isValidNumber(String message) {
		try
		{
			//If this doesn't throw an exception we're good
			char[] test = message.toCharArray();
			for(char check : test)
			{
				if(!Character.isDigit(check))
					return false;
			}
			//Needs to be exactly ten digits
			return (message.length() == 10);
		}
		catch(NumberFormatException e1)
		{
			return false;
		}
	}

	private String generateBoard() {
		StringBuilder board = new StringBuilder();
		board.append("```\n");
		board.append("DIGITAL FORTRESS\n");
		//Counting down in a loop, spooky
		for(int i=3; i>=attemptsLeft; i--)
		{
			board.append("   ");
			board.append(guesses[i]);
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
		board.append("\n```");
		return board.toString();
	}

	@Override
	public boolean isGameOver() {
		return (digitsCorrect == solution.size() || attemptsLeft <= 0);
	}

	@Override
	public int getMoneyWon() {
		if(isGameOver())
			return (PRIZE_PER_DIGIT * digitsCorrect);
		else
			return 0;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
	
	@Override
	public String getBotPick()
	{
		List<Character> digits = Arrays.asList('0','1','2','3','4','5','6','7','8','9');
		//Cycle the list once for every attempt used
		for(int i=ATTEMPTS_ALLOWED; i>attemptsLeft; i++)
		{
			digits.add(digits.get(0));
			digits.remove(0);
		}
		//Now remove anything we've already locked in
		for(int i=0; i<solution.size(); i++)
			if(lockedIn[i])
				digits.remove(solution.get(i));
		//Now start building up the result
		String result = "";
		ListIterator<Character> nextDigit = digits.listIterator();
		//If we have the digit right then grab it directly
		//Otherwise grab the next digit from our set
		for(int i=0; i<solution.size(); i++)
		{
			if(lockedIn[i])
				result += solution.get(i);
			else
				result += nextDigit.next();
		}
		return result;
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}