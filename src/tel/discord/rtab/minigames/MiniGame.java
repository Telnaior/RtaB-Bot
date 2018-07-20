package tel.discord.rtab.minigames;

import java.util.LinkedList;

public interface MiniGame {
	/*
	 * SendNextInput
	 * Receive the next pick from the player
	 */
	void sendNextInput(String pick);
	/*
	 * GetNextOutput
	 * Returns a String that should be next sent to the player
	 * Method throws exception if game is over
	 */
	LinkedList<String> getNextOutput();
	/*
	 * isGameOver
	 * Returns true if the minigame has ended
	 */
	boolean isGameOver();
	/*
	 * GetMoneyWon
	 * Returns an int containing the player's winnings, pre-booster
	 * Methods throws exception if game isn't over
	 */
	int getMoneyWon();
}
