package tel.discord.rtab.enums;

import tel.discord.rtab.minigames.*;

public enum Games {
	//TEST_GAME("Test Game","Test",new TestGame()),
	STRIKE_IT_RICH("Strike it Rich","Strike",new StrikeItRich());
	
	String fullName;
	String shortName;
	MiniGame game;
	Games(String gameName, String miniName, MiniGame gameID)
	{
		fullName = gameName;
		shortName = miniName;
		game = gameID;
	}
	@Override
	public String toString()
	{
		return this.fullName;
	}
	public MiniGame getGame()
	{
		return this.game;
	}
}
