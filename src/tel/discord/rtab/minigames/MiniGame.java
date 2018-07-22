package tel.discord.rtab.minigames;

import java.util.LinkedList;

public interface MiniGame {
	/*
	 * sendNextInput
	 * Receive the next pick from the player
	 */
	void sendNextInput(String pick);
	/*
	 * getNextOutput
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
	 * getMoneyWon
	 * Returns an int containing the player's winnings, pre-booster
	 * Methods throws exception if game isn't over
	 */
	int getMoneyWon();
	
	/* isBonusGame
	 * Returns true if the game is a bonus game (and therefore shouldn't have boosters or winstreak applied)
	 * Returns false if it isn't (and therefore should have boosters and winstreak applied)
	 */
	boolean isBonusGame();
}
