package tel.discord.rtab.enums;

import tel.discord.rtab.minigames.*;

public enum Games implements WeightedSpace {
	//Minigame Pool
	MATH_TIME		(2,"Math Time","Math",new MathTime()),
	STRIKE_IT_RICH	(2,"Strike it Rich","Strike",new StrikeItRich()),
	GAMBLE			(2,"The Gamble","Gamble",new Gamble()),
	THE_OFFER		(2,"The Offer","Offer",new TheOffer()),
	DEUCES_WILD		(2,"Deuces Wild","Deuces",new DeucesWild()),
	DOUBLE_TROUBLE	(2,"Double Trouble","Double",new DoubleTrouble()),
	DEAL_OR_NO_DEAL	(2,"Deal or No Deal","DoND", new DealOrNoDeal()),
	BUMPER_CASH     (2,"Bumper Grab","Bumper", new BumperGrab()),
	DOUBLE_ZERO     (2,"Double Zero","00", new DoubleZeroes()),
	SHUT_THE_BOX	(2,"Shut the Box","Shut",new ShutTheBox()),
	//Don't have enough games in the pool to have a rotation yet
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
	public String getShortName()
	{
		return shortName;
	}
	public MiniGame getGame()
	{
		return game;
	}
	@Override
	public int getWeight(int playerCount)
	{
		//Minigame types don't care about playercount
		return weight;
	}
}
