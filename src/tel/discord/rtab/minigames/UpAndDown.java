package tel.discord.rtab.minigames;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UpAndDown implements MiniGame {
	static final String NAME = "Up And Down";
	static final boolean BONUS = false;
	static final int BOARD_SIZE = 5;
	int[] dollarValues = new int[BOARD_SIZE];
	int[] dollarChange = new int[BOARD_SIZE];
	int[] curMulti = {-100, 100, 100, 150, 200};
	int[] multiChange = {150, 300, 450, 550, 1750};
	String[] alphabet = {"A", "B", "C", "D", "E"};
	List<Integer> shuffleResult = Arrays.asList(0, 1, 2, 3, 4); //Reds

	int roundNum;
	int yourChoice;
	int baseMultiplier;
	int total;
	boolean alive;
	
	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
	{
		this.baseMultiplier = baseMultiplier;
		for (int i=0; i<BOARD_SIZE; i++)
		{
			dollarValues[i] = 0;
			dollarChange[i] = 0;
		}
		LinkedList<String> output = new LinkedList<>();
		alive = true;
		roundNum = 0;
		//This first one doesn't actually set up the values, just the initial multipliers
		updateValues();
		total = 1000 * baseMultiplier;
		//Display instructions
		output.add("In Up And Down, you can win **unlimited** money! But with that potential comes big risk, too.");
		output.add(String.format("You'll start with **$%,d**. We'll put five dollar amounts in envelopes, and shuffle them up.",total));
		output.add("After each pick, the lowest money amount will get lower, the highest money amount will get higher, and the others will change as well.");
		output.add("They will start by rising, but the further you get into the game, the lower they get, and soon all but the high value will become negative!");
		output.add("The game ends when you choose to stop, or when your bank becomes negative. In either case, you'll leave with your bank.");
		output.add("With that said, please pick envelope **A**, **B**, **C**, **D**, or **E** to begin, and **STOP** when you're satisfied!");
		roundNum++;
		updateValues();
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		if(pick.equalsIgnoreCase("A") || pick.equalsIgnoreCase("1"))
		{
			yourChoice = 0;
			output.add("Let's open envelope A!");
		}	
		else if(pick.equalsIgnoreCase("B") || pick.equalsIgnoreCase("2"))
		{
			yourChoice = 1;
			output.add("Let's open envelope B!");
		}	
		else if(pick.equalsIgnoreCase("C") || pick.equalsIgnoreCase("3"))
		{
			yourChoice = 2;
			output.add("Let's open envelope C!");
		}	
		else if(pick.equalsIgnoreCase("D") || pick.equalsIgnoreCase("4"))
		{
			yourChoice = 3;
			output.add("Let's open envelope D!");
		}	
		else if(pick.equalsIgnoreCase("E") || pick.equalsIgnoreCase("5"))
		{
			yourChoice = 4;
			output.add("Let's open envelope E!");
		}	
		else if(pick.equalsIgnoreCase("STOP"))
		{
			alive = false;
			return output;
		}		
		else
		{
			//Omega continue not saying anything for random strings
			return output;
		}
		for (int j=0; j<4; j++)
		{
			if (total + dollarValues[j] < 0)
			{
				output.add("...");
				break;
			}
		}
		total = total + dollarValues[shuffleResult.get(yourChoice)];
		if (dollarValues[shuffleResult.get(yourChoice)] > 0)
		{
			output.add(String.format("**$%,d!**",dollarValues[shuffleResult.get(yourChoice)]));
		}
		else
		{
			output.add(String.format("**$%,d.**",dollarValues[shuffleResult.get(yourChoice)]));
		}
		if (total < 0)
		{
			output.add("Too bad, that's the end for you.");
			alive = false;
		}
		else
		{
			output.add("Let's change the values and see if you want to play another round!");
			roundNum++;
			updateValues();
			output.add(generateBoard());
			output.add("Will you pick envelope **A**, **B**, **C**, **D**, or **E**, or will you **STOP** with your bank?");
		}
		return output;
	}
	
	void updateValues()
	{
		for (int j=0; j<5; j++)
		{
			dollarValues[j] += dollarChange[j] * baseMultiplier;
			dollarChange[j] += 5 * curMulti[j];
			for (int k=0; k<j; k++)
			{
				if (dollarChange[k] > dollarChange[j])
				{
					dollarChange[j] = (int)(dollarChange[k] * .95);
				}
			}
			curMulti[j] = curMulti[j] + multiChange[j];
			multiChange[j] = multiChange[j] - (int)((5.5 - j) * ((int)(Math.random()*126) + 125));
		}
		if (curMulti[4] < 400)
		{
			curMulti[4] = 400;
		}
	}
	
	String generateBoard()
	{
		Collections.shuffle(shuffleResult);
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append(" Up And Down \n\n");
		display.append("Round " + roundNum + "\n");
		display.append(String.format("TOTAL: $%,d\nVALUES: ",total));
		//Next display how many of each we have, and our total
		for(int i=0; i<4; i++)
		{
			display.append(String.format("$%,d, ",dollarValues[i]));
		}
		display.append(String.format("$%,d\n",dollarValues[4]));
		display.append("\n");
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver() 
	{
		return !alive;
	}

	@Override
	public int getMoneyWon()
	{
		if(isGameOver())
		{
			return total;
		}
		else
		{
			if (total < dollarValues[0])
			{
				return total - dollarValues[0];
			}
			else
			{
				return 0;
			}
		}
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
	
	@Override
	public String getBotPick()
	{
		boolean willStop = false;
		for (int j=0; j<4; j++)
		{
			if (total + dollarValues[j] < 0)
			{
				if (Math.random() < (.05 * roundNum))
				{
					willStop = true;
				}
			}
		}	
		if (Math.random()*1_000_000 < total)
		{
			willStop = true;
		}
		if (roundNum < 4)
		{
			willStop = false;
		}		
		if (willStop)
		{
			return "STOP";
		}
		else
		{
			return alphabet[(int)(Math.random()*5)];
		}
		
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
