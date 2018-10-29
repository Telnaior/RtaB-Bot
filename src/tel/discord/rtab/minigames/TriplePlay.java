package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TriplePlay implements MiniGame {
	static final String NAME = "Triple Play";
	static final boolean BONUS = false;
	List<Integer> money = Arrays.asList(100,200,300,500,700,1000,2000,3000,5000,7000,
			10000,20000,30000,50000,70000,100000,200000,300000,500000,700000);
	boolean alive;
	boolean[] pickedSpaces;
	int lastPick;
	int lastSpace;
	int total;
	int target;
	int picksLeft;
	
	public TriplePlay()
	{
		alive = true;
		pickedSpaces = new boolean[money.size()];
		total = 0;
		target = 0;
		lastPick = 0;
		picksLeft = 3;
		Collections.shuffle(money);
	}
	
	@Override
	public LinkedList<String> initialiseGame()
	{
		//Dummy method so I can work on separating constructor work from instructions
		return giveInstructions();
	}
	
	public LinkedList<String> giveInstructions()
	{
		LinkedList<String> output = new LinkedList<>();
		output.add("Triple Play is a variant of The Gamble.");
		output.add("In this game, you pick three spaces and add their values together.");
		output.add("Then, you can either leave with that total or throw it away to pick three more spaces.");
		output.add("If you play on, your previous total becomes a target that you must beat, or you will leave with nothing.");
		output.add("The biggest possible win is $1,500,000.");
		output.add("Best of luck! Pick your first space when you're ready.");
		output.add(generateBoard());
		return output;
	}
	
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		if(pick.toUpperCase().equals("STOP") && picksLeft == 0)
		{
			total = target;
			alive = false;
		}
		else if(!isNumber(pick))
		{
			//Definitely don't say anything for random strings
		}
		else if(!checkValidNumber(pick))
		{
			output.add("Invalid pick.");
		}
		else
		{
			//If we're starting our second set, refresh the counter
			if(picksLeft == 0)
				picksLeft = 3;
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			total += money.get(lastSpace);
			lastPick = money.get(lastSpace);
			picksLeft --;
			//Start printing output
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(picksLeft == 0)
				output.add("...");
			output.add(String.format("$%,d!",lastPick));
			if(picksLeft > 0)
			{
				output.add(String.format("You have %d pick" + (picksLeft == 1 ? "" : "s") + " left.", picksLeft));
				output.add(generateBoard());
			}
			else
			{
				if(target == 0)
				{
					target = total;
					total = 0;
					output.add(String.format("You can now choose to leave with your total of $%,d, "
							+ "or pick three more spaces and try to get a higher total.",target));
					output.add("Type STOP to quit, or pick your first space if you are playing on.");
				}
				else if(total > target)
				{
					output.add("Congratulations, you beat your target!");
					alive = false;
				}
				else
				{
					output.add("Sorry, you fell short of the target.");
					total = 0;
					alive = false;
				}
			}
		}
		return output;
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
		display.append(" TRIPLE  PLAY \n");
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
		if(picksLeft == 0)
		{
			display.append(String.format(" Total: $%,d\n",target));
		}
		else
		{
			display.append(String.format(" Total: $%,d\n",total));
			if(target > 0)
				display.append(String.format("Target: $%,d\n",target));
		}
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
		//We only need to check if we'll stop if 
		if(picksLeft == 0)
		{
			//Arbitrary stopping point lol
			boolean willStop = target > 200000 ? true : false;
			if(willStop)
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
