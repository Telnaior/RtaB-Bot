package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DoubleZeroes implements MiniGame {
	static final String NAME = "Double Zeroes";
	static final boolean BONUS = false;
	int total;
	int zeroesLeft;
	List<Integer> numbers = Arrays.asList(0,0,0,0,0,0,0,0,0,0,20,30,40,50,55,60,70,80,90,99);
	// 0 = Double Zero
	boolean alive;
	boolean[] pickedSpaces;
	int lastSpace;
	int lastPick;
	
	/**
	 * Initializes the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initialiseGame()
	{
		alive = true;
		total = 0;
		zeroesLeft = 10;
		pickedSpaces = new boolean[numbers.size()];
		Collections.shuffle(numbers);
		// Give 'em the run down
		LinkedList<String> output = new LinkedList<>();
		output.add("In Double Zeroes, you will see twenty spaces.");
		output.add("Ten of these are Double Zeroes, and ten are numbers from 20-99.");
		output.add("You'll pick spaces, one at a time, until you uncover two 20-99 spaces.");
		output.add("These numbers will be put on the board as your bank.");
		output.add("At this point, everything but the Double Zeroes turn into BOMBs!");
		output.add("You can then choose to 'STOP' and multiply your bank by the number of Double Zeroes remaining...");
		output.add("...or try to hit a Double Zero to stick that Double Zero at the end of your bank, multiplying it by 100! Good luck!");
		output.add(generateBoard());
		return output;
	}

	/**
	 * Takes the next player input and uses it to play the next "turn" - up until the next input is required.
	 * @param  The next input sent by the player.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		if(pick.toUpperCase().equals("STOP"))
		{
			if(total > 100) // 'total > 100' doesn't happen until there are two 20-99's picked
			{
				// Player stops at the decision point? Tell 'em what they've won and end the game!
			alive = false;
			total = total * zeroesLeft;
				output.add("Very well! Your bank is multiplied by " + String.format("$%,d!",zeroesLeft)
				+ ", which means it's...");
			return output;
			}
			else // Don't stop 'til you get enough, keep on!
			{
				output.add("Can't stop yet, you must pick two non-zero values first!");
				return output;
			}
		}
		else if(!isNumber(pick))
		{
			//Still don't say anything for random strings
			return output;
		}
		if(!checkValidNumber(pick))
		{
			// EASTER EGG! Take the RTaB Challenge!
			// Hit this message 29,998,559,671,349 times in a row
			output.add("Invalid pick.");
			return output;
			// and you win a free kick from the server
		}
		else
		{	
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			lastPick = numbers.get(lastSpace);
			//Start printing output
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(numbers.get(lastSpace) == 0) // If it's a Double Zero...
			{
				if(total > 100) // ...and you decided to go on, you win!
				{
					alive = false;
					total = total*100;
					output.add("It's a **Double Zero**!");
					output.add("Congratulations, you've won the game!");
					// No need to subtract a zero because the game's over
					// And no need to show the total because that happens at the Game Over message outside of this file
				}
				else // ...and it's still in the first phase, keep going and remember that there's one less Double Zero.
				{
					output.add("It's a **Double Zero**.");
					zeroesLeft--;
				}
			}
			else // If it's NOT a Double Zero...
			{
				if(total > 100) // ...and you decided to go...
				{
					alive = false; // BOMB, shoulda taken the bribe!
					total = 0;
					output.add("It's a **BOMB**.");
					output.add("Sorry, you lose.");
				}
				else if(total == 0) // ...and it's the first phase with no prior numbers...
				{
					if (numbers.get(lastSpace) == 80) // ... and it's an 80, use an 'an'
					{
					output.add("It's an " + String.format("**%,d!**",numbers.get(lastSpace)));
					}
					else // ... and it's not an 80, use an 'a'
					{
					output.add("It's a " + String.format("**%,d!**",numbers.get(lastSpace)));
					}
					total = numbers.get(lastSpace); // Either way, put the total on the board.
				}
				else // ...and it's the first phase with a prior number...
				{
					if (numbers.get(lastSpace) == 80) // ... and it's an 80, use an 'an'
					{
					output.add("It's an " + String.format("**%,d!**",numbers.get(lastSpace)));
					}
					else // ... and it's not an 80, use an 'a'
					{
					output.add("It's a " + String.format("**%,d!**",numbers.get(lastSpace)));
					}
					total = (total * 100) + numbers.get(lastSpace); // Either way, put the total on the board by multiplying the previous total by 100 and adding this number.
				}
			
			}
			if(alive)
			{

				if(total>100) // If we just hit the second number, tell 'em about the DECISION~!
				{
					output.add("You can now choose to continue by picking a number, "
							+ "or you can type STOP to stop with your bank of " + String.format("**$%,d**",total)
							+ ", times the number of Double Zeroes left, which would give you " + String.format("**$%,d!**",total*zeroesLeft));
					output.add(generateBoard());
				}
				else if(zeroesLeft>0) // Otherwise let 'em pick another space.
				{
					output.add("Please pick another space.");
					output.add(generateBoard());
				}
				else // Oops, All Digits! (tm) Better end it quick!
				{
					output.add("You picked all the Double Zeroes! Uh...");
					output.add("Let's just end the game here and tack a hundred grand on to your bank!");
					alive = false;
					total = total + 100000;
				}
			}
			return output;
		}
}

	boolean isNumber(String message)
	{
		try
		{
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
		return (location >= 0 && location < numbers.size() && !pickedSpaces[location]);
	}
	
	String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("DOUBLE ZEROES\n");
		for(int i=0; i<numbers.size(); i++)
		{
			if(pickedSpaces[i])
			{
				display.append("  ");
			}
			else
			{
				display.append(String.format("%02d",(i+1)));
			}
			if(i%5 == 4)
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display our bank and number of Double Zeroes left
		display.append(String.format("Bank: $%,d\n",total));
		display.append(String.format("%d Double Zeroes left\n",zeroesLeft));
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver() {
		return !alive;
	}

	@Override
	public int getMoneyWon() {
		if(isGameOver())
			return total;
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
			//If the game's at its decision point, make the decision
			//There should be (8 + zeroesLeft) spaces left here
			if(total > 100)
			{
				int goChance = 100 * (zeroesLeft / (8 + zeroesLeft));
				if(Math.random()*100>goChance)
					return "STOP";
			}
			//If we aren't going to stop, let's just pick our next space

			ArrayList<Integer> openSpaces = new ArrayList<>(numbers.size());
			for(int i=0; i<numbers.size(); i++)
				if(!pickedSpaces[i])
					openSpaces.add(i+1);
			return String.valueOf(openSpaces.get((int)(Math.random()*openSpaces.size())));
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
