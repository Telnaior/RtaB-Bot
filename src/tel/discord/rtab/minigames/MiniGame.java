package tel.discord.rtab.minigames;

import java.util.LinkedList;

public interface MiniGame {
	
	/**
	 * Initialises the variables used in the minigame and prints the starting messages.
	 * @param The channel the minigame is being played in, and the multiplier to apply to the minigame's values.
	 * @return A list of messages to send to the player.
	 */
	LinkedList<String> initialiseGame(String channelID, int baseMultiplier);
	
	/**
	 * Takes the next player input and uses it to play the next "turn" - up until the next input is required.
	 * @param  The next input sent by the player.
	 * @return A list of messages to send to the player.
	 */
	LinkedList<String> playNextTurn(String pick);
	
	/**
	 * Returns true if the minigame has ended
	 */
	boolean isGameOver();
	
	/**
	 * Returns an int containing the player's winnings, pre-booster.
	 * If game isn't over yet, should return lowest possible win (usually 0) because player timed out for inactivity.
	 */
	int getMoneyWon();
	
	/**
	 * Returns true if the game is a bonus game (and therefore shouldn't have boosters or winstreak applied)
	 * Returns false if it isn't (and therefore should have boosters and winstreak applied)
	 */
	boolean isBonusGame();

	/**
	 * Calculates the next choice a bot should make in the minigame.
	 * @return The next input the bot should send to the minigame.
	 */
	String getBotPick();
}
