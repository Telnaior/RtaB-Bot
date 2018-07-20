package tel.discord.rtab.minigames;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MathTime implements MiniGame {
	List<Integer> money = Arrays.asList(0,5000,10000,20000,30000,40000,60000);
	List<String> ops1 = Arrays.asList("+","+","+","+","+","-","-");
	List<String> ops2 = Arrays.asList("x","x","x","x","/","/","/");
	List<Integer> multis = Arrays.asList(1,2,3,4,5,7,10);
	int stage = 0;
	int stage1Pick;
	String result2, result4;
	int lastPick;
	int total = 0;
	String equation = "";
	boolean invalid = false;
	
	@Override
	public void sendNextInput(int pick) {
		if(pick >= 7 || (stage == 3 && pick == stage1Pick))
			invalid = true;
		else
		{
			lastPick = pick;
			if(stage == 1)
				stage1Pick = pick;
		}
	}

	@Override
	public LinkedList<String> getNextOutput() {
		LinkedList<String> output = new LinkedList<>();
		if(invalid)
		{
			output.add("Invalid pick.");
			return output;
		}
		output.add(String.format("Space %d selected...",(lastPick+1)));
		output.add("...");
		switch(stage)
		{
		case 0:
			//Initialise stuff
			Collections.shuffle(money);
			Collections.shuffle(ops1);
			Collections.shuffle(ops2);
			//Give instructions
			output.add("In Math Time, you will pick five spaces that will, together, form an equation.");
			output.add("If you pick well, you could win up to $1,000,000!");
			output.add("But if things go poorly you could *lose* money in this minigame, so be careful.");
			output.add("On each stage you will have seven spaces to choose from - "
					+ "except that you pick from the money stage twice.");
			output.add("When you are ready, make your first pick from the money stage.");
			stage++;
			break;
		case 1:
		case 3:
			if(stage == 3 && result2 == "-")
				total -= money.get(lastPick);
			else
				total += money.get(lastPick);
			String result = String.format("$%,d!",money.get(lastPick));
			output.add(result);
			output.add("Next, pick an operation...");
			equation += result;
			stage++;
			break;
		case 2:
			result2 = ops1.get(lastPick);
			output.add("**"+result2+"**");
			output.add("Next, pick more cash...");
			equation += (" "+result2+" ");
			stage++;
			break;
		case 4:
			result4 = ops2.get(lastPick);
			output.add("**"+result4+"**");
			output.add("Next, pick a multiplier...");
			equation += (" "+result4+" ");
			stage++;
			break;
		case 5:
			if(result4 == "/")
				total /= multis.get(lastPick);
			else
				total *= multis.get(lastPick);
			String result5 = String.format("%d!",money.get(lastPick));
			output.add(result5);
			equation += result5 + " = ";
			equation += String.format("$,d",total);
			stage++;
			break;
		}
		output.add(generateBoard());
		return output;
	}

	String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		if(stage <= 5)
		{
			display.append("    MATH    TIME    \n");
			for(int i=0; i<7; i++)
			{
				if(stage == 3 && i == stage1Pick)
				{
					display.append("  ");
				}
				else
				{
					display.append(String.format("%02d",(i+1)));
				}
				display.append(" ");	
			}
			display.append("\n\n");
		}
		display.append(equation);
		display.append("\n```");
		return display.toString();
	}
	
	@Override
	public boolean isGameOver()
	{
		return stage >= 6;
	}

	@Override
	public int getMoneyWon()
	{
		stage = 0;
		return total;
	}

}
