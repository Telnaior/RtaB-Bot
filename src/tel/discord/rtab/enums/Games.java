package tel.discord.rtab.enums;

import tel.discord.rtab.minigames.*;

public enum Games implements WeightedSpace {
	//Minigame Pool
	MATH_TIME			(2,"Math Time","Math") { public MiniGame getGame() { return new MathTime(); } },							//Author: Atia
	STRIKE_IT_RICH		(2,"Strike it Rich","Strike") { public MiniGame getGame() { return new StrikeItRich(); } },					//Author: Atia
	GAMBLE				(1,"The Gamble","Gamble") { public MiniGame getGame() { return new Gamble(); } },							//Author: Atia
	TRIPLE_PLAY			(1,"Triple Play","Triple") { public MiniGame getGame() { return new TriplePlay(); } },						//Author: Atia
	DEUCES_WILD			(2,"Deuces Wild","Deuces") { public MiniGame getGame() { return new DeucesWild(); } },						//Author: StrangerCoug
	DOUBLE_TROUBLE		(2,"Double Trouble","Double") { public MiniGame getGame() { return new DoubleTrouble(); } },				//Author: JerryEris
	DEAL_OR_NO_DEAL		(2,"Deal or No Deal","DoND") { public MiniGame getGame() { return new DealOrNoDeal(); } },					//Author: Atia
	MINEFIELD_MULTIPLIER(2,"Minefield Multiplier","Multiplier") {public MiniGame getGame() { return new MinefieldMultiplier(); } }, //Author: Amy
	DOUBLE_ZERO     	(2,"Double Zero","00") { public MiniGame getGame() { return new DoubleZeroes(); } },						//Author: JerryEris
	SHUT_THE_BOX		(2,"Shut the Box","Shut") { public MiniGame getGame() { return new ShutTheBox(); } },						//Author: StrangerCoug
	COIN_FLIP			(2,"CoinFlip","Flip") { public MiniGame getGame() { return new CoinFlip(); } },								//Author: Amy
	BOMB_ROULETTE		(1,"Bomb Roulette","Roulette") { public MiniGame getGame() { return new BombRoulette(); } },				//Author: StrangerCoug
	HI_LO_DICE			(2,"Hi/Lo Dice","Hi/Lo") { public MiniGame getGame() { return new HiLoDice(); } },							//Author: StrangerCoug
	//Games rotated out
	THE_OFFER			(0,"The Offer","Offer") { public MiniGame getGame() { return new TheOffer(); }	},							//Author: Amy
	//BUMPER_CASH   	(2,"Bumper Grab","Bumper") { public MiniGame getGame() { return new BumperGrab(); } },						//Author: Tara
	//Bonus games never appear in the pool
	SUPERCASH			(0,"SUPERCASH","Super") { public MiniGame getGame() { return new Supercash(); } },
	DIGITAL_FORTRESS	(0,"DIGITAL FORTRESS","Fortress") { public MiniGame getGame() { return new DigitalFortress(); } },
	SPECTRUM			(0,"SPECTRUM","Spectrum") { public MiniGame getGame() { return new Spectrum(); } },
	HYPERCUBE			(0,"HYPERCUBE","Hyper^3") { public MiniGame getGame() { return new Hypercube(); } };
	
	String fullName;
	String shortName;
	int weight;
	Games(int valueWeight, String gameName, String miniName)
	{
		fullName = gameName;
		shortName = miniName;
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
	
	//Returns a new instance of the requested minigame
	public abstract MiniGame getGame();
	
	@Override
	public int getWeight(int playerCount)
	{
		//Minigame types don't care about playercount
		return weight;
	}
}
