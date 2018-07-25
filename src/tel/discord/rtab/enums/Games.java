package tel.discord.rtab.enums;

import tel.discord.rtab.minigames.*;

public enum Games implements WeightedSpace {
	//TEST_GAME("Test Game","Test",new TestGame()),
	STRIKE_IT_RICH	(2,"Strike it Rich","Strike",new StrikeItRich()),
	MATH_TIME		(2,"Math Time","Math",new MathTime()),
	GAMBLE			(2,"The Gamble","Gamble",new Gamble()),
	DEUCES_WILD		(2,"Deuces Wild","Deuces",new DeucesWild()),
	//Bonus games never appear in the pool
	SUPERCASH		(0,"SUPERCASH","Super",new Supercash()),
	DIGITAL_FORTRESS(0,"DIGITAL FORTRESS","Fortress",new DigitalFortress()),
	SPECTRUM		(0,"SPECTRUM","Spectrum",new Spectrum()),
	HYPERCUBE		(0,"HYPERCUBE","Hyper^3",new Hypercube());
	
	String fullName;
	String shortName;
	MiniGame game;
	int weight;
	Games(int valueWeight, String gameName, String miniName, MiniGame gameID)
	{
		fullName = gameName;
		shortName = miniName;
		game = gameID;
		weight = valueWeight; 
	}
	@Override
	public String toString()
	{
		return fullName;
	}
	public MiniGame getGame()
	{
		return game;
	}
	@Override
	public int getWeight() {
		return weight;
	}
}
