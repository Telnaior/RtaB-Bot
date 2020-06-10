package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DoubleTrouble implements MiniGame {
	static final String NAME = "Double Trouble";
	static final boolean BONUS = false;
	int rounds;
	int mystery;
	int total;
	int bombsLeft;
	int crashLeft;
	int cashLeft;
	int doublesLeft;
	List<Integer> money = Arrays.asList(-1,0,1,10000,5000,5000,2500,2500,2500,2500,2500,2,2,2,2,2,2,2,2,2);
	// -1 = Trouble, 0 = Bomb, 1 = Mystery, 2 = Double
	boolean alive;
	boolean[] pickedSpaces;
	int lastSpace;
	int lastPick;
	int baseMultiplier;
	
	/**
	 * Initializes the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
	{
		this.baseMultiplier = baseMultiplier;
		alive = true;
		rounds = 0;
		total = 5000 * baseMultiplier; // Player starts with $5,000
		mystery = 100*(int)((Math.random()*200+1)); // Generates a random number from 100 - 20,000 in $100 increments
		if(Math.random() < 0.25) //With 25% chance, give it another random number with the same distribution
			mystery += 100*(int)((Math.random()*100+1));
		mystery *= baseMultiplier;
		bombsLeft = 1;
		crashLeft = 1;
		cashLeft = 9;
		doublesLeft = 9;
		pickedSpaces = new boolean[money.size()];
		Collections.shuffle(money);
		
		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("In Double Trouble, you will see twenty spaces.");
		output.add(String.format("You'll start with $%,d and will pick spaces one at a time.",5_000*baseMultiplier));
		output.add("Nine of them are Double spaces, and will double your winnings for the round.");
		output.add("Nine of them have dollar amounts, "
				+ String.format("which could range from $%,d to $%,d.",100*baseMultiplier,20_000*baseMultiplier));
		output.add("One space is Trouble, which will reduce your winnings by 90% should you find it.");
		output.add("Finally, there is a bomb. If you hit it, the game is over and you lose everything.");
		output.add("You may STOP at any time or pick a number to go on. Good luck!");
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
			alive = false;
			return output;
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
			switch(lastPick)
			{
			case 0: //Bomb
				alive = false;
				total = 0;
				bombsLeft --;
				output.add("It's a **BOMB**.");
				output.add("Sorry, you lose.");
				break;
			case -1: //Crash
				total /= 10;
				crashLeft --;
				output.add("Uh oh, it's TROUBLE...");
				output.add("You lose 90% of your cash.");
				break;
			case 1: //Mystery
				output.add("It's the MYSTERY!");
				output.add(String.format("This time, it's worth $%,d!",mystery));
				total += mystery;
				cashLeft --;
				break;
			case 2: //Double
				output.add("It's a DOUBLE!");
				total *= 2;
				doublesLeft --;
				break;
			default: //Regular cash
				output.add(String.format("It's $%,d!",lastPick*baseMultiplier));
				total += lastPick*baseMultiplier;
				cashLeft --;
				break;
			}
			if(alive)
			{
				if(cashLeft == 0 && doublesLeft == 0)
				{
					output.add("You totally cleaned out the board! How daring you must be!");
					alive = false;
				}
				else
				{
					output.add("Choose another space if you dare, "
							+ "or type STOP to stop with your current total: " + String.format("**$%,d!**",total));
					output.add(generateBoard());
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
		//Next display our total and how many spaces are left in each category
		display.append(String.format("Total: $%,d\n",total));
		if(doublesLeft > 0)
			display.append(String.format("%dx DOUBLE\n",doublesLeft));
		if(cashLeft > 0)
			display.append(String.format("%dx CASH\n",cashLeft));
		if(crashLeft > 0)
			display.append(String.format("%dx TROUBLE\n",crashLeft));
		if(bombsLeft > 0)
			display.append(String.format("%dx BOMB\n",bombsLeft));
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
			if(rounds > 0)
			{
				int stopChance = 5+(rounds*5);
				if (stopChance>90)
					stopChance=90;
				if(Math.random()*100<stopChance)
					return "STOP";
			}
			//If we aren't going to stop, let's just pick our next space
			ArrayList<Integer> openSpaces = new ArrayList<>(money.size());
			for(int i=0; i<money.size(); i++)
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
