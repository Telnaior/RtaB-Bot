package tel.discord.rtab.minigames;

import java.util.LinkedList;

public class TheOffer implements MiniGame {
	static final String NAME = "The Offer";
	static final boolean BONUS = false;
	double chanceToBomb; 
	int offer;
	int seconds;
	boolean alive; //Player still alive?
	boolean accept; //Accepting the Offer
	/**
	 * Initialises the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initialiseGame(){
		offer = 1000 * (int)(Math.random()*51+50); // First Offer starts between 50,000 and 100,000
		chanceToBomb = offer/10000;  // Start chance to Bomb 5-10% based on first offer
		seconds = 1;
		alive = true; 
		accept = false;

		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("In The Offer, you can enter a room with a live bomb.");
		output.add("Every Room you survive will earn you more money!");
		output.add("Each Room will at least double your actual earned money, " +
				"but the chance of the bomb exploding will also increase significantly.");
		output.add("Every Room will also increase the amount of chances to Explode!");
		output.add("If the bomb explodes, you lose everything."); //~Duh
		output.add(makeOffer(offer, seconds, chanceToBomb)); 
		return output;  
	}

	/**
	 * Takes the next player input and uses it to play the next "turn" - up until the next input is required.
	 * @param  The next input sent by the player.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> playNextTurn(String pick){
		LinkedList<String> output = new LinkedList<>();
		String choice = pick.toUpperCase();
		choice = choice.replaceAll("\\s","");
		if(choice.equals("REFUSE") || choice.equals("NODEAL") || choice.equals("DARE"))
		{
			output.add("The Bomb goes live");
			output.add("...");
			//Let's find out if we explode
			for(int i=0; i<seconds; i++)
			{
				if (chanceToBomb > Math.random()*100)
				{
					output.add("**BOOM**");
					alive = false;
					break;
				}
			}
			//If still alive, let's run it
			if(alive)
			{
				double increment = Math.random()*0.5;
				offer += (int)(offer * (1 + increment));
				offer -= offer%100;
				seconds++;
				chanceToBomb += 5 + (increment*10);
				output.add(makeOffer(offer, seconds, chanceToBomb));
			}
		}
		else if(choice.equals("ACCEPT") || choice.equals("DEAL") || choice.equals("TAKE"))
		{
			accept = true;
			output.add("You take your Money!");
		}
		//If it's neither of those it's just some random string we can safely ignore
		return output;
	}

	/**
	* @param offer The amount that gets offered to the Player
	* @param times The amount of times the Bomb will Tick
	* @param bomb The Chance of the Bomb going Boom per Tick
	* @return Will Return a nice looking output with all Infos
	**/
	private String makeOffer(int offer, int times, double bomb)
	{
		StringBuilder output = new LinkedList<>();
		output.append("```\n");
		output.append("  The Offer  \n\n;");
		output.append("Next Room:\n");
		output.append("Bomb: " + String.format ("%.2f%%\n", bomb));
		output.append("Ticks: " + String.format("$%,d Times\n\n", times));
		output.append("Current Money: " + String.format("$%,d\n\n", offer));
		output.append(" 'Take' the Money  or  'Dare' the Bomb \n");
		output.append("```");
		return output.toString();
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
		return (isGameOver() & alive) ? offer : 0;
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
		//Do a "trial run", quit if it fails
		for(int i=0; i<=seconds; i++)
		{
			if (chanceToBomb > Math.random()*100)
			{
				return "ACCEPT";
			}
		}
		//Trial run says we'll survive, so play on
		return "REFUSE";
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
