package tel.discord.rtab.minigames;

import java.util.LinkedList;

public interface MiniGame {
	/*
	 * SendNextInput
	 * Receive the next pick from the player
	 */
	void sendNextInput(int pick);
	/*
	 * GetNextOutput
	 * Returns a String that should be next sent to the player
	 * Method throws exception if game is over
	 */
	LinkedList<String> getNextOutput();
	/*
	 * GetMoneyWon
	 * Returns an int containing the player's winnings, pre-booster
	 * Methods throws exception if game isn't over
	 */
	boolean gameStillRunning();
	int getMoneyWon() throws GameNotOverException;
}
