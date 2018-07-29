package tel.discord.rtab.minigames;

import java.util.LinkedList;

public class TestGame implements MiniGame 
{
	static final String NAME = "Test Game";
	static final boolean BONUS = false;
	boolean gameOver = false;
	
	@Override
	public LinkedList<String> initialiseGame()
	{
		//Reset variables
		gameOver = false;
		//Send help
		LinkedList<String> output = new LinkedList<>();
		output.add("This Test Game is just a placeholder, so send any message to win $100,000!");
		return output;
	}
	@Override
	public LinkedList<String> playNextTurn(String pick) {
		LinkedList<String> output = new LinkedList<>();
		output.add("You picked " + pick);
		return output;
	}
	
	@Override
	public boolean isGameOver() {
		return gameOver;
	}
	
	@Override
	public int getMoneyWon(){
		if(isGameOver())
			return 100000;
		else
			return 0;
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}
	
	@Override
	public String getBotPick()
	{
		return "I am a bot.";
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
