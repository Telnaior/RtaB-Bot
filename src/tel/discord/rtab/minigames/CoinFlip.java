
package tel.discord.rtab.minigames;

import java.util.LinkedList;

public class CoinFlip implements MiniGame {
	static final String NAME = "CoinFlip";
	static final boolean BONUS = false; 
	int stage;
	int coins;
	boolean alive; //Player still alive?
	boolean accept; //Accepting the Offer
	/**
	 * Initialises the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initialiseGame(){
		stage = 0; // We always start on Stage 0
		coins = 100;
		
		alive = true; 
		accept = false;

		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("Welcome to CoinFlip!");
		output.add("You have 100 Coins and have to choose Heads or Tails, " +
				"as long as atleast 1 Coin shows your choice you win the Stage.");
		output.add("We have 13 Stages with inreasing values to Win!");
		output.add("You lose everything, if **NO** Coin lands on your chosen side."); //~Duh
		output.add(ShowPaytable(stage));
		output.add(makeOverview(coins, stage)); 
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

		boolean heads = false; //Default variable
		boolean tails = false; //Default variable

		String choice = pick.toUpperCase();
		choice = choice.replaceAll("\\s","");
		if(choice.equals("HEADS") || choice.equals("H"))
		{
			heads = true;
		}
		else if (choice.equals("TAILS") || choice.equals("T"))
		{
			tails = true;
		}
		else if(choice.equals("ACCEPT") || choice.equals("DEAL") || choice.equals("TAKE") || choice.equals("STOP") || choice.equals("S"))
		{
			accept = true;
			output.add("You took the money!");
		}
		else if(choice.equals("!PAYTABLE"))
		{
			output.add(ShowPaytable(stage));
		}
		//If it's neither of those it's just some random string we can safely ignore
		
		if(heads || tails)
		{	
			int newcoins = 0;
			// Under 50 Heads, 50 and above Tails
			for(int i=0; i < coins; i++)
			{
				if (50 < (Math.random()*100)){
					if (tails) newcoins++;
				}
				else{
					if (heads) newcoins++;
				}
			}
			if (heads) output.add("We flipped " + String.format("%d", coins) + " Coins and we got " + String.format("%d HEADS\n\n", newcoins));
			else if (tails) output.add("We flipped " + String.format("%d", coins) + " Coins and we got " + String.format("%d TAILS\n\n", newcoins));
			coins = newcoins;
			if (coins == 0) {
				alive = false;
			}
			else {
				stage++;
				output.add("You won " + String.format("%d and ", stage) + String.format("%,d! \n", payTable(stage)));
				if (stage == 13) accept = true;
				else output.add(makeOverview(coins, stage));
			}
		}
		
		return output;
	}

	/**
	* @param  stage Shows the selected Stage bold.
	* @return Will Return a nice looking Paytable with all Infos
	**/
	private String ShowPaytable(int stage)
	{
		StringBuilder output = new StringBuilder();
		output.append("```\n");
		output.append("      Win-Stages  \n\n");
		if(stage==1) output.append("**Stage  1:  $    1,000**\n");
		else output.append("Stage  1:  $    1,000\n");
		if(stage==2) output.append("**Stage  2:  $    5,000**\n");
		else output.append("Stage  2:  $    5,000\n");
		if(stage==3) output.append("**Stage  3:  $   10,000**\n");
		else output.append("Stage  3:  $   10,000\n");
		if(stage==4) output.append("**Stage  4:  $   25,000**\n");
		else output.append("Stage  4:  $   25,000\n");
		if(stage==5) output.append("**Stage  5:  $   50,000**\n");
		else output.append("Stage  5:  $   50,000\n");
		if(stage==6) output.append("**Stage  6:  $   75,000**\n");
		else output.append("Stage  6:  $   75,000\n");
		if(stage==7) output.append("**Stage  7:  $  100,000**\n");
		else output.append("Stage  7:  $  100,000\n");
		if(stage==8) output.append("**Stage  8:  $  200,000**\n");
		else output.append("Stage  8:  $  200,000\n");
		if(stage==9) output.append("**Stage  9:  $  300,000**\n");
		else output.append("Stage  9:  $  300,000\n");
		if(stage==10) output.append("**Stage 10:  $  400,000**\n");
		else output.append("Stage 10:  $  400,000\n");
		if(stage==11) output.append("**Stage 11:  $  500,000**\n");
		else output.append("Stage 11:  $  500,000\n");
		if(stage==12) output.append("**Stage 12:  $  750,000**\n");
		else output.append("Stage 12:  $  750,000\n");
		if(stage==13) output.append("**Stage 13:  $1,000,000**\n");
		else output.append("Stage 13:  $1,000,000\n");
		output.append("```");
		return output.toString();
	}

	/**
	* @param coins The amount of coins left
	* @param stage The current stage
	* @return Will Return a nice looking output with all Infos
	**/
	private String makeOverview(int coins, int stage)
	{
		StringBuilder output = new StringBuilder();
		output.append("```\n");
		output.append("  CoinFlip  \n\n");
		output.append("Amount of Coins: " + String.format("%d \n", coins));
		output.append("Current Stage: " + String.format("%d - ", stage) + String.format("$%,d\n\n", payTable(stage)));
		output.append("'Heads' or 'Tails'   (or 'Stop')? \n");
		
		output.append("```");
		return output.toString();
	}

	private int payTable(int stage)
	{
		int value = 0;
		switch (stage) 
		{
			case 1:  value = 1000;
				break;
			case 2:  value = 5000;
				break;
			case 3:  value = 10000;
				break;
			case 4:  value = 25000;
				break;
			case 5:  value = 50000;
				break;
			case 6:  value = 75000;
				break;
			case 7:  value = 100000;
				break;
			case 8:  value = 200000;
				break;
			case 9:  value = 300000;
				break;
			case 10: value = 400000;
				break;
			case 11: value = 500000;
				break;
			case 12: value = 750000;
				break;
			case 13: value = 1000000;
				break;
			default: value = 0;


		}
		return(value);
	}

	/**
	 * Returns true if the minigame has ended
	 */
	@Override
	public boolean isGameOver(){
		return accept || !alive;
	}


	/**
	 * Returns an int containing the player's winnings, pre-booster.
	 * If game isn't over yet, should return lowest possible win (usually 0) because player timed out for inactivity.
	 */
	@Override
	public int getMoneyWon(){
		return (isGameOver() & alive) ? payTable(stage) : 0;
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
		//As long as we have more coins than 0-30, GO ON
	
		if (coins > Math.random()*5)
		{
			// Throw a single coin and decide from there.
			if (50 < (Math.random()*100)){
					return "TAILS";
				}
				else{
					return "HEADS";
				}
		}
	
		//If it thinks not enough coins
		return "STOP";
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}