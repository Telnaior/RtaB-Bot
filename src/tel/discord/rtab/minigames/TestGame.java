package tel.discord.rtab.minigames;

import java.util.LinkedList;

public class TestGame implements MiniGame 
{
	int gameState = 0;
	int numberSent;
	@Override
	public void sendNextInput(int pick) {
		numberSent = pick+1;
	}

	@Override
	public LinkedList<String> getNextOutput() {
		LinkedList<String> output = new LinkedList<>();
		switch(gameState)
		{
		case 0:
			gameState++;
			output.add("This Test Game is just a placeholder, so pick any number to win $100,000!");
			return output;
		case 1:
			gameState++;
			output.add("You picked " + numberSent);
			return output;
		default:
			output.add("If you see this message, something went wrong.");
			gameState = 2;
			return output;
		}
	}
	
	@Override
	public boolean isGameOver() {
		return (gameState >= 2);
	}
	
	@Override
	public int getMoneyWon(){
		return 100000;
	}

}
