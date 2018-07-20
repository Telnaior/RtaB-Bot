package tel.discord.rtab.minigames;

public class TestGame implements MiniGame {

	@Override
	public void sendNextInput(int pick) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getNextOutput() throws GameOverException {
		// TODO Auto-generated method stub
		throw new GameOverException();
	}

	@Override
	public int getMoneyWon() throws GameNotOverException {
		// TODO Auto-generated method stub
		return 100000;
	}

}
