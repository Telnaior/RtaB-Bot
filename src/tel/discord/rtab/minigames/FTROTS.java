package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FTROTS implements MiniGame {
	static final String NAME = "For the Rest of the Season";
	static final boolean BONUS = false;
	static final int[] TIME_LADDER = {0, 1, 2, 5, 10, 20, 30, 50, 75, 100, -1};
	List<Integer> money = new ArrayList<>();
	List<Integer> multis = Arrays.asList(1,1,1,1,1,2,2,2,2,3,3,3,4,4,5,6,8,10);
	List<Boolean> lights = Arrays.asList(true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false);
	List<Character> ops = Arrays.asList('x','x','x','x','x','x','x','x','x','x','x','x','/','/','/','/','/','/');
	boolean[] pickedSpaces = new boolean[18];
	int stage = 0;
	int lastPick;
	boolean multiply;
	int total = 0;
	int whiteLightsLeft = 13;
	int redLightsLeft = 5;
	int timeLadderPosition = 0;
	boolean canStop = false;
	
	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
	{
		LinkedList<String> output = new LinkedList<>();
		//Generate money values: $1000-$1499, $1500-$1999, etc, up to $9500-$9999
		for(int i=0; i<18; i++)
			money.add(((int)(Math.random()*500) + 500*(i+2)) * baseMultiplier);
		//Shuffle everythihg
		Collections.shuffle(money);
		Collections.shuffle(ops);
		Collections.shuffle(multis);
		Collections.shuffle(lights);
		//Give instructions
		output.add("For the Rest of the Season is a two-part game that is unlike any other.");
		output.add("In part one, we give you a sum of money via a shortened versoin of Math Time.");
		output.add("However, while this amount is affected by your booster, you won't be awarded that sum immediately. ");
		output.add("Instead, we'll give it to you as an annuity to be paid out the next time you play Race to a Billion.");
		output.add("You'll be receiving that amount every time you pick a space for a set number of installments. "
				+ "In part two of this minigame, you'll be deciding how many installments of the annuity you'll receive.");
		output.add("Start by selecting one space from this board. Behind each space is a four-figure sum of money.");
		output.add(generateBoard(false));
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick) {
		LinkedList<String> output = new LinkedList<>();
		if(canStop && pick.equalsIgnoreCase("STOP"))
		{
			output.add("Congratulations, you took the money!");
			output.add(generateBoard(true));
			stage++;
			return output;
		}
		if(!isNumber(pick))
		{
			//Ignore non-number picks entirely
			return output;
		}
		if(!checkValidNumber(pick))
		{
			output.add("Invalid pick.");
			return output;
		}
		else
		{
			lastPick = Integer.parseInt(pick)-1;
			pickedSpaces[lastPick] = true;
			//Print stuff
			output.add(String.format("Space %d selected...",(lastPick+1)));
			switch(stage)
			{
			case 0:
				total = money.get(lastPick);
				output.add(String.format("**$%,d!**", total));
				reduceLightCount();
				output.add("The next space you pick will contain an operation.");
				output.add("You want to see an 'x' for multiplication. You do NOT want to see division here.");
				stage++;
				break;
			case 1:
				if(ops.get(lastPick).equals('x'))
				{
					multiply = true;
					output.add("It's an **x**!");
				}
				else
				{
					multiply = false;
					output.add("It's an **/**.");
				}
				reduceLightCount();
				output.add("The next space you pick will contain a multiplier ranging from 1 to 10.");
				output.add(multiply?"There's nothing to lose here, but if you hit a big number we could be on for a huge win."
						:"Obviously we want this to be as low as possible, so try to find a 1.");
				stage++;
				break;
			case 2:
				output.add("...");
				if(multiply)
				{
					total *= multis.get(lastPick);
					output.add("It's a **"+multis.get(lastPick)+"**"+(multis.get(lastPick)>1?"!":"."));
					output.add(multis.get(lastPick)>1?String.format("This brings your total up to $%,d!",total)
							:String.format("This leaves your total unchanged at $%,d.",total));
				}
				else
				{
					total /= multis.get(lastPick);
					output.add("It's a **"+multis.get(lastPick)+"**"+(multis.get(lastPick)>1?".":"!"));
					output.add(multis.get(lastPick)>1?String.format("This reduces your total to $%,d.",total)
							:String.format("This leaves your total unchanged at $%,d!",total));
				}
				reduceLightCount();
				output.add("We now take that total and move on to part two.");
				output.add("This is your time ladder, showing what you are playing for today:");
				output.add(generateTimeLadder());
				output.add(String.format("On the left, you see the number of times your $%,d will be awarded - ",total)
						+ "From once, to twice, to five times, to ten times, all the way up to one hundred times.");
				output.add("Beyond that, if you can make it to the very top of the time ladder,"
						+ String.format("you will receive $%,d every time you pick a space for the rest of the season.",total));
				output.add("Here's how you do it. The remaining 15 spaces on the board contain "
						+whiteLightsLeft+" white lights and "+redLightsLeft+" red lights.");
				output.add("Every time you find a white light, you move up one rung on your time ladder.");
				output.add("However, every time you find a red light, you move down one rung.");
				output.add("You can stop at any time IF the last light you found was white.");
				output.add("If you find a red light, you MUST play on until you find another white light.");
				output.add("Finally, if you find ALL of the red lights, you will leave with nothing.");
				output.add("Good luck, and choose your first light when you are ready.");
				stage++;
				break;
			case 3:
				if(redLightsLeft == 1 || timeLadderPosition >= 5)
					output.add("...");
				if(lights.get(lastPick))
				{
					timeLadderPosition ++;
					canStop = true;
					output.add("It's a **WHITE** light!");
					reduceLightCount();
					if(timeLadderPosition == 10 || whiteLightsLeft == 0)
					{
						stage++;
						output.add("Congratulations, that's as far as you can go in this game!");
					}
					else
					{
						int currentTime = TIME_LADDER[timeLadderPosition];
						output.add(String.format("This brings you up to %d space"+(timeLadderPosition!=1?"s":"")+
								", for a total of $%,d!", currentTime, total*currentTime));
						output.add("You can stop here, or play on to find another white light.");
						output.add(generateTimeLadder());
					}
				}
				else
				{
					if(timeLadderPosition != 0)
						timeLadderPosition --;
					canStop = false;
					output.add("It's a **RED** light.");
					reduceLightCount();
					if(redLightsLeft == 0)
					{
						total = 0;
						stage++;
						output.add("Unfortunately, as you have found every red light, you leave with nothing.");
					}
					else
					{
						output.add("That pushes you down one rung on your time ladder, and you MUST pick again.");
						output.add(generateTimeLadder());
					}
				}
			}
			output.add(generateBoard(false));
			return output;
		}
	}
	
	void reduceLightCount()
	{
		if(lights.get(lastPick))
			whiteLightsLeft --;
		else
			redLightsLeft --;
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
		return (location >= 0 && location < 18 && !pickedSpaces[location]);
	}

	String generateBoard(boolean reveal)
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append(" FOR THE REST OF \n   THE  SEASON   \n");
		for(int i=0; i<18; i++)
		{
			if(pickedSpaces[i])
				display.append("  ");
			else
			{
				if(reveal)
				{
					if(lights.get(i))
						display.append("Wh");
					else
						display.append("Rd");
				}
				else
					display.append(String.format("%02d",(i+1)));
			}
			if(i%6 != 5)
				display.append(" ");
			else
				display.append("\n");
		}
		display.append("```");
		return display.toString();
	}
	
	String generateTimeLadder()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("     YOUR TIME LADDER     \n");
		display.append(String.format("    %2d WHITE    %2d RED    \n",whiteLightsLeft,redLightsLeft));
		display.append("==========================\n");
		int maxRungReachable = Math.min(10, timeLadderPosition + whiteLightsLeft);
		int minRungReachable = Math.max(1, timeLadderPosition - (redLightsLeft-1));
		int longestMoneyLength = 2;
		for(int i=maxRungReachable; i>=minRungReachable; i--)
		{
			int currentTime = TIME_LADDER[i];
			if(currentTime == -1)
				display.append("FOR THE REST OF THE SEASON\n");
			else
			{
				if(timeLadderPosition == i)
					display.append("> ");
				else
					display.append("  ");
				display.append(String.format("%3d SPACE", currentTime));
				if(currentTime != 1)
					display.append("S");
				else
					display.append(" ");
				//Aligning the cash totals to the right is stupidly complex lmao
				String timeTotal = String.format("$%,"+(longestMoneyLength-1)+"d", total*currentTime);
				if(longestMoneyLength == 2)
					longestMoneyLength = timeTotal.length();
				for(int j=13; j<25-longestMoneyLength; j++)
					display.append(" ");
				display.append(timeTotal);
				if(timeLadderPosition == i)
					display.append(" <");
				else
					display.append("  ");
				display.append("\n");
			}
		}
		//Then do it one last time for "NOTHING"
		if(timeLadderPosition == 0)
			display.append("> ");
		else
			display.append("  ");
		display.append("  NOTHING");
		for(int i=9; i<21; i++)
		{
			if(i == 22-longestMoneyLength)
				display.append("$");
			else
				display.append(" ");
		}
		display.append("0");
		if(timeLadderPosition == 0)
			display.append(" <");
		else
			display.append("  ");
		display.append("\n```");
		return display.toString();
	}
	
	@Override
	public boolean isGameOver()
	{
		return (stage > 3);
	}

	@Override
	public int getMoneyWon()
	{
		switch(stage)
		{
		case 4:
			stage++;
			return total;
		case 5:
			return TIME_LADDER[timeLadderPosition];
		default:
			return 0;
		}
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
	
	@Override
	public String getBotPick()
	{
		//Take a trial run, and stop if we'd hit two reds in a row or otherwise hit the last red
		if(canStop && Math.random()*(whiteLightsLeft+redLightsLeft) > whiteLightsLeft)
		{
			boolean willStop = redLightsLeft==1;
			if(!willStop)
				willStop = Math.random()*(whiteLightsLeft+redLightsLeft-1) > whiteLightsLeft;
			if(willStop)
				return "STOP";
		}
		ArrayList<Integer> openSpaces = new ArrayList<>(18);
		for(int i=0; i<18; i++)
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
