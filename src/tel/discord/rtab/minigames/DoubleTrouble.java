package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TheOffer implements MiniGame {
	static final boolean BONUS = false;
	int rounds;
	int mystery;
	int total;
	List<Integer> money = Arrays.asList(0,1,25000,10000,10000,5000,5000,5000,5000,5000,2,2,2,2,2,2,2,2,2,2);
	// 0 = Bomb, 1 = Mystery, 2 = Double
	boolean alive;
	boolean[] pickedSpaces;
	
	/**
	 * Initializes the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initializeGame(){
		rounds = 0;
		total = 10000; // Player starts with $10,000
		mystery = (int)(1000 + (Math.random()*9501)); // Generates a random number from 1,000-10,500, otherwise only a 1/9001 chance of 10k and that's too low imo
		if(mystery>10000)
		{
			mystery = 10000; // Max mystery is 10,000.
		}	
		if(Math.random()<.8)
		{
			mystery = (int)(mystery / 1000); // 80% chance to have a clean thousand (if not 10k)
		}
		else if(Math.random()<.8)
		{
			mystery = (int)(mystery / 100); // 16% (.2*.8) chance to have a clean hundred
		}
	
		pickedSpaces = new boolean[money.size()];
		Collections.shuffle(money);
		
		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("In Double Trouble, you will see twenty spaces.");
		output.add("You'll start with $10,000 and will pick spaces one at a time.");
		output.add("Ten of them are Double spaces, and will double your winnings for the round.");
		output.add("Nine of them have dollar amounts, which could range from $1,000 to $25,000.");
		output.add("One is the bomb. If you hit the bomb, you lose everything.");
		output.add("You may STOP at any time after your first pick or pick a number to go on. Good luck!");
		output.add("----------------------------------------"); 
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
		if(pick.toUpperCase().equals("STOP") && total > 0)
		{
			if(rounds == 0)
				{
					output.add("You haven't picked yet!");
				}
			else
			{
			alive = false;
			return output;
			}
		}
		else if(!isNumber(pick))
		{
			//Definitely don't say anything for random strings
			return output;
		}
		if(!checkValidNumber(pick))
		{
			output.add("Invalid pick.");
			return output;
		}
		else
		{	
			rounds ++;
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			lastPick = money.get(lastSpace);
			//Start printing output
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(total != lastPick)
				output.add("...");
			if(money.get(lastSpace) = 0)
			{
				alive = false; // BOMB, tough cookies
				total = 0;
				output.add("It's a BOMB.");
				output.add("Sorry, you lose.");
			}
			else if(money.get(lastSpace) = 1)
			{
				// Mystery picked!
				output.add("It's the MYSTERY!");
				output.add(String.format("This time, it's worth $%,d!",mystery));
				total += mystery;
			}			
			else if(money.get(lastSpace) = 2)
			{
				// Double picked!
				output.add("It's a DOUBLE!");
				total += total;
			}
			else
			{
				// Money picked!
				output.add(String.format("It's $%,d!",lastPick));
				total += money.get(lastSpace);
			}
			if(alive)
			{
				if(rounds == 19)
				{
					output.add("You left the bomb for last! How daring you must be!");
					alive = false;
				}
				else
				{
					output.add("Choose another space if you dare, or type STOP to stop with your current total...");
					output.add(String.format("which is $%,d!",total));
					output.add(generateBoard());
				}
			}
			else
			{
				output.add("Unfortunately, you've won nothing.");
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
		return (location >= 0 && location < money.size() && !pickedSpaces[location]);
	}
	
	String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("DOUBLE TROUBLE\n");
		for(int i=0; i<money.size(); i++)
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
		//Next display our total and last space picked
		display.append(String.format("    Total: $%,d\n",total));
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
		//We don't need to check if we need to stop if we haven't even picked once yet
		//Code somewhat ~~stolen~~ borrowed from the Gamble game
		if(lastPick > 0)
		{
			boolean willStop = false;
			int stopChance = .05+(rounds*.05);
			if (stopChance>.9)
				stopChance=.9;
			int pickPosition = Arrays.binarySearch(money.toArray(),lastPick);
			int spacesPicked = 0;
			for(boolean next : pickedSpaces)
				if(next)
					spacesPicked ++;
			int badPicks = pickPosition + 1 - spacesPicked;
			//Basically take a "trial" pick and stop if it comes up bad
			willStop = (Math.random() * (money.size() - spacesPicked)) < badPicks;
			if(Math.random()<stopChance)
				return "STOP";
		}
		//If we aren't going to stop, let's just pick our next space
		ArrayList<Integer> openSpaces = new ArrayList<>(money.size());
		for(int i=0; i<money.size(); i++)
			if(!pickedSpaces[i])
				openSpaces.add(i+1);
		return String.valueOf(openSpaces.get((int)(Math.random()*openSpaces.size())));
}
}
