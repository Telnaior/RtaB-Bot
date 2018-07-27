package tel.discord.rtab.minigames;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
	LinkedList<String> initialiseGame()
  {
    seconds = 0;                      // Starting at 0 Seconds
    chanceToBomb = (Math.Random()*15) + 5;  // Start chance to Bomb 5-20%
    offer = 1000 * (int)(Math.Random()*100); // First Offer starts between 1,000 and 100,000
    alive = true; 
    accept = false; 
    refuse = false; 
    invalid = false; 
    
    LinkedList<String> output = new LinkedList<>();
    //Give instructions
		output.add("In The Offer, your will get placed in a room with a live Bomb");
    output.add("You will get offers while in the room to leave it.");
		output.add("Every Second that passes increases the money you gain as an offer by atleast 100%," +
     "But also the Chance to Bomb by atleast 5%");
    output.add("If you refuse the money you have to decide how many WHOLE seconds you want to wait.");
		
		output.add("If you bomb, you lose everything. ~Duh");
    output.add("Be aware the Bomb can explode every moment, so don't take too much time!");
    output.add("----------------------------------------");
    output.add("Your first Offer is: " + String.format("**$%,d**", offer));
    output.add("Do you "Accept" or "Refuse" ?")
    return output;  
  }
	
	/**
	 * Takes the next player input and uses it to play the next "turn" - up until the next input is required.
	 * @param  The next input sent by the player.
	 * @return A list of messages to send to the player.
	 */
   @Override
  LinkedList<String> playNextTurn(String pick){
 
  LinkedList<String> output = new LinkedList<>();
  string choice = pick.toUpperCase();
  choice = choice.replaceAll("\\s","");
  if (refuse)
  {
    if (!isNumber(choice))
    {
    //Definitely don't say anything for random strings
    return output;
    }
    
    int stopAt = seconds + Integer.parseInt(choice);
    if (stopAt <= seconds)
      {
        output.add("Please try to wait in the Now and not in the Past");
        output.add("Use a positive Number higher than 0");
        return output;
      }
    refuse = false;
    bool halfSecond = false;
    int boomValue;
    while(seconds < stopAt)
    {      
      boomValue = Math.Random()*100;
      if (chanceToBomb > boomValue){
        output.add("**BOOM**");
        alive = false;
      }
      if (!alive){
        break;
      }
      
      if (!halfSecond){
        halfSecond = true;
      }
      else {
        halfSecond = false;
        offer += (int)(offer * (1 + (Math.Random()*0.5)));
        offer -= offer%100;
        chanceToBomb += 5 + (Math.Random()*5)
        output.Add("...")
        seconds++;
      }
    }
    if (seconds == stopAt && alive)
    {
      output.add("Your new offer is: " + String.format("**$%,d**", offer));
    }
    else if(seconds > stopAt)
    {
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
    else if(!"choice".equals("REFUSE") || !"choice".equals("NODEAL"))
    {

      //Definitely don't say anything for random strings
      return output;
    }
    else
    {
      refuse = true;
      output.add("Offer Refused!");
      output.add("How many seconds do you want to wait?");
      return output;
    }
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
	  
  
	/**
	 * Returns true if the minigame has ended
	 */
   @Override
	boolean isGameOver(){
    if (alive && accept){ 
    return true;
    }
    return !alive;
  }
  
	
	/**
	 * Returns an int containing the player's winnings, pre-booster.
	 * If game isn't over yet, should return lowest possible win (usually 0) because player timed out for inactivity.
	 */
   @Override
	int getMoneyWon();
    if (isGameOver() && alive){
      return offer;
    }
    else {
      return 0;
    }
	
	/**
	 * Returns true if the game is a bonus game (and therefore shouldn't have boosters or winstreak applied)
	 * Returns false if it isn't (and therefore should have boosters and winstreak applied)
	 */
   @Override
	boolean isBonusGame(){
    return Bonus;
    }
}
