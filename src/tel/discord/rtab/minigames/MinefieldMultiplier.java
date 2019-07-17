package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MinefieldMultiplier implements MiniGame {
	static final String NAME = "Minefield Multiplier";
	static final boolean BONUS = false; 
	int total;
	int stageAmount;
	int stage;
	List<Integer> numbers = Arrays.asList(-1,1,1,1,1,1,2,2,2,2,2,3,3,3,3,4,4,4,5,5,10);
	List<Integer> bombs;
	int maxBombs;
	boolean alive; //Player still alive?
	boolean stop; //Running away with the Money
	boolean[] pickedSpaces;
	int lastSpace;
	int lastPick;
	
	
	/**
	 * Initialises the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initialiseGame(){
		bombs = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
		total = 0;
		stage = 1;
		maxBombs = 1;
		stageAmount = 10000;
		alive = true; 
		stop = false;

		pickedSpaces = new boolean[numbers.size()];
		Collections.shuffle(numbers);

		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("Welcome to Minefield Multiplier");
		output.add("You have a Board of 21 Spaces You have a Board of 21 Spaces with a Multiplier hiding in each space.");
		output.add("Every Turn the Amount of Money which we multiply will increase, BUT...");
		output.add("The number of Bombs on the Board will increase as well!");
		output.add("There will be 8 Rounds and there is one x10 Multiplier.");
		output.add("Bombs will be randomly placed anywhere on the Board, "+
			  "including on top of other bombs or already picked spaces.");
		output.add("You can **STOP** after each Round, with your current Bank or **PASS** to the next stage!");
		output.add("But you lose everything, if you hit a Bomb."); //~Duh

		output.add(generateBoard());
		return output;  
	}

	/**
	 * Takes the next player input and uses it to play the next "turn" - up until the next input is required.
	 * @param pick The next input sent by the player.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> playNextTurn(String pick){
		LinkedList<String> output = new LinkedList<>();
		

		String choice = pick.toUpperCase();
		choice = choice.replaceAll("\\s","");
		if(choice.equals("ACCEPT") || choice.equals("DEAL") || choice.equals("TAKE") || choice.equals("STOP"))
		{
			// Player stops 
			stop = true;
			output.add("Very well! You escaped with your bank of " + String.format("%,d",total));
			output.add("Here is the revealed Board!");
			output.add(generateRevealBoard());
			return output;
		}
		else if(choice.equals("PASS")){
			if (stage >= 8) {
				output.add("You can't pass the Last Stage. Either **STOP** or Pick your space!");
			}
			else {
				output.add("We going to pass this Stage, but we still add the Bombs!");
				if (bombTable(stage + 1) == 1) {
					output.add("**1** Bomb was placed!");
				} else {
					output.add(String.format("**%d** Bombs were placed!", bombTable(stage + 1)));
				}
				increaseStage();
				output.add(generateBoard());
			}
			return output;
		}
		else if(!isNumber(choice))
		{
			//Still don't say anything for random strings
			return output;
		}
		if(!checkValidNumber(choice))
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
			output.add("..."); //suspend dots
			if(bombs.get(lastSpace) == 1 || lastPick == -1) // If it's a Bomb
			{
				output.add("**BOOM**");
				output.add("Sorry, you lose!");
				output.add("Here is the revealed Board!");
				output.add(generateRevealBoard());
				alive=false;

			}
			else // If it's NOT a Bomb
			{
				int win = 0;
				win = lastPick * stageAmount;
				total = win + total; // Either way, put the total on the board.

				output.add("It's a " + String.format("**x%d** Multiplier!", lastPick));
				output.add(String.format("That makes **%,d** for a total of **%,d!**", win, total));
				if (bombTable(stage+1)==1){
					output.add("We are going to add **%1** Bomb!");
				}
				else{
					output.add(String.format("We are going to add **%d** Bombs!", bombTable(stage+1)));
				}
				increaseStage();
				output.add(generateBoard());
			}
		}

		return output;
	}

	private boolean isNumber(String message)
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
	
	private boolean checkValidNumber(String message)
	{
		int location = Integer.parseInt(message)-1;
		return (location >= 0 && location < numbers.size() && !pickedSpaces[location]);
	}

	private String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("MINEFIELD MULTIPLIER\n");
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
			if(i%7 == 6)
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display Bank, StageAmount and Number of Max Bombs 
		display.append(String.format("Bank: $%,d\n",total));
		display.append(String.format("Next pick: $%,d\n",stageAmount));
		display.append(String.format("Max Bombs: %d\n",maxBombs));
		display.append("```");
		return display.toString();
	}

	private String generateRevealBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("MINEFIELD MULTIPLIER\n");
		for(int i=0; i<numbers.size(); i++)
		{
			if(pickedSpaces[i])
			{
				display.append("  ");
			}
			else if(bombs.get(i) == 1 || numbers.get(i)== -1)
			{
				display.append("XX");
			}
			else
			{
				if (numbers.get(i) == 10){
					display.append(String.format("%d",numbers.get(i)));
				}
				else{
					display.append(String.format("x%d",numbers.get(i)));
				}
			}
			if(i%7 == 6)
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("```");
		return display.toString();
	}

	private int stageTable(int stage)
	{
		int value = 0;
		switch (stage)
		{
			case 1:
				value = 10000;
				break;
			case 2:
				value = 20000;
				break;
			case 3:
				value = 30000;
				break;
			case 4:
				value = 40000;
				break;
			case 5:
				value = 50000;
				break;
			case 6:
				value = 75000;
				break;
			case 7:
				value = 100000;
				break;
			case 8:
				value = 200000;
				break;
		}
		return(value);
	}

	private int bombTable(int stage)
	{
		int value = 0;
		switch (stage)
		{
			case 6:
				value = 2;
				break;
			case 7:
				value = 3;
				break;
			case 8:
				value = 5;
				break;
			default:
				value = 1;
				break;
		}
		return(value);
	}

	private void increaseStage(){
		stage++;
		stageAmount = stageTable(stage);
		maxBombs = maxBombs + bombTable(stage);
		for(int i=0; i<bombTable(stage); i++)
		{
			int rand = (int) (Math.random()*numbers.size()); //0-20 (21 Spaces in the Array, 0 is included*)
			bombs.set(rand, 1);
		}
	}
	/**
	 * Returns true if the minigame has ended
	 */
	@Override
	public boolean isGameOver(){
		return stop || !alive || stage >=9;
	}


	/**
	 * Returns an int containing the player's winnings, pre-booster.
	 * If game isn't over yet, should return lowest possible win (usually 0) because player timed out for inactivity.
	 */
	@Override
	public int getMoneyWon(){
		return (isGameOver() & alive) ? total : 0;
	}
	/**
	 * Returns true if the game is a bonus game (and therefore shouldn't have boosters or winstreak applied)
	 * Returns false if it isn't (and therefore should have boosters and winstreak applied)
	 */
	@Override
	public boolean isBonusGame(){
		return BONUS;
	}
	
	@Override
	public String getBotPick()
	{
		//If there are more than 9 Bombs and he won more than 100k
		//Let him flip a coin to decide if he wants to continue
		if(maxBombs > 5 || total > 100000)
		{
			if((int)(Math.random()*2)< 1)
				return "STOP";
		}
		else if (stageAmount<=40000){
			if((int)(Math.random()*2)<1)
				return "PASS";
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