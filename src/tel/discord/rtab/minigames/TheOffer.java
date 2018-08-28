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
		seconds = 0;
		alive = true; 
		accept = false;

		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("In The Offer, you will be placed in a room with a live bomb.");
		output.add("You will get offers while in the room to leave it.");
		output.add("Every offer will be at least double the previous one, " +
				"but the chance of the bomb exploding will also increase significantly.");
		output.add("If the bomb explodes, you lose everything."); //~Duh
		output.add("Be aware the Bomb can explode at any moment, so don't take too long!");
		output.add("----------------------------------------"); 
		output.add("Your first Offer is: " + String.format("**$%,d**", offer));
		output.add("Do you 'ACCEPT' or 'REFUSE'?");
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
		if(choice.equals("REFUSE") || choice.equals("NODEAL"))
		{
			output.add("Offer Refused!");
			seconds++;
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
				chanceToBomb += 5 + (increment*10);
				output.add("Your new offer is: " + String.format("**$%,d**", offer));
				output.add("Do you 'ACCEPT' or 'REFUSE'?");
			}
		}
		else if(choice.equals("ACCEPT") || choice.equals("DEAL"))
		{
			accept = true;
			output.add("Offer Accepted!");
		}
		//If it's neither of those it's just some random string we can safely ignore
		return output;
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
