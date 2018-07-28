package tel.discord.rtab.minigames;

import java.util.LinkedList;

public class TheOffer implements MiniGame {
	static final boolean BONUS = false;
	double chanceToBomb; 
	int offer; 
	int seconds; // Time passed with the Bomb
	boolean alive; //Player still alive?
	boolean accept; //Accepting the Offer
	boolean refuse; //Refusing the Offer
	/**
	 * Initialises the variables used in the minigame and prints the starting messages.
	 * @return A list of messages to send to the player.
	 */
	@Override
	public LinkedList<String> initialiseGame(){
		seconds = 0;                      // Starting at 0 Seconds
		chanceToBomb = 5 + (Math.random()*16);  // Start chance to Bomb 5-20%
		offer = 1000 * (int)(Math.random()*100+1); // First Offer starts between 1,000 and 100,000
		alive = true; 
		accept = false; 
		refuse = false; 

		LinkedList<String> output = new LinkedList<>();
		//Give instructions
		output.add("In The Offer, you will be placed in a room with a live bomb.");
		output.add("You will get offers while in the room to leave it.");
		output.add("Every second that passes increases the money you gain as an offer by at least 100%, " +
				"but the chance of the bomb exploding will also increase by at least 5%.");
		output.add("If you refuse the money you have to decide how many WHOLE seconds you want to wait.");
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
		if (refuse){
			if (!isNumber(choice)){
				//Definitely don't say anything for random strings
				return output;
			}

			int stopAt = seconds + Integer.parseInt(choice);
			
			if (stopAt <= seconds){
				output.add("Please try to wait in the Now and not in the Past.");
				output.add("Use a positive number, higher than 0.");
				return output;
			}
			
			refuse = false;
			boolean halfSecond = false;
			int boomValue;
			
			while(seconds < stopAt){      
				
				boomValue = (int) (Math.random()*100);
				
				if (chanceToBomb > boomValue){
					output.add("**BOOM**");
					alive = false;
					break;
				}
				else
				{
					output.add("...");
				}

				if (!halfSecond){
					halfSecond = true;
				}
				else {
					halfSecond = false;
					offer += (int)(offer * (1 + (Math.random()*0.5)));
					offer -= offer%100;
					chanceToBomb += 5 + (Math.random()*5);
					seconds++;
				}
			}
			
			if (seconds == stopAt && alive){
				output.add("Your new offer is: " + String.format("**$%,d**", offer));
				output.add("Do you 'ACCEPT' or 'REFUSE'?");
			}
			else if(seconds > stopAt){
				output.add("You found a Bug! Tell a DEV! - Seconds > StopAT - Take this!");
				offer = 100000;
				alive = true;
				accept = true;
				return output;
			}
			
			return output;
		}
		else
		{
			if(choice.equals("ACCEPT") || choice.equals("DEAL"))
			{
				accept = true;
				output.add("Offer Accepted!");
				return output;
			}
			else if(choice.equals("REFUSE") || choice.equals("NODEAL"))
			{
				refuse = true;
				output.add("Offer Refused!");
				output.add("How many seconds do you want to wait?");
				return output;
			}
			else
			{
				//Definitely don't say anything for random strings
				return output;
			}
		}
	}

	boolean isNumber(String message)
	{
		try{
			Integer.parseInt(message);
			return true;
		}
		catch(NumberFormatException e1){
			return false;
		}
	}


	/**
	 * Returns true if the minigame has ended
	 */
	@Override
	public boolean isGameOver(){
		if (alive && accept) 
			return true;
		return !alive;
	}


	/**
	 * Returns an int containing the player's winnings, pre-booster.
	 * If game isn't over yet, should return lowest possible win (usually 0) because player timed out for inactivity.
	 */
	@Override
	public int getMoneyWon(){
		if (isGameOver() & alive)
			return offer;
		else
			return 0;
	}
	/**
	 * Returns true if the game is a bonus game (and therefore shouldn't have boosters or winstreak applied)
	 * Returns false if it isn't (and therefore should have boosters and winstreak applied)
	 */
	@Override
	public boolean isBonusGame(){
		return BONUS;
	}
}
