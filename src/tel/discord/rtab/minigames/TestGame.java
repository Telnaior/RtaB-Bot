package tel.discord.rtab.minigames;

public class TestGame implements MiniGame {
	boolean sentMessage = false;
	@Override
	public void sendNextInput(int pick) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getNextOutput() throws GameOverException {
		// TODO Auto-generated method stub
		if(sentMessage)
			throw new GameOverException();
		else
		{
			sentMessage = true;
			return "This Test Game is just a placeholder, so here's $100,000!";
		}
	}

	@Override
	public int getMoneyWon() throws GameNotOverException {
		// TODO Auto-generated method stub
		return 100000;
	}

}
