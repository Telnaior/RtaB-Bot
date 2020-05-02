package tel.discord.rtab.enums;

import tel.discord.rtab.minigames.*;

public enum Games implements WeightedSpace {
	//Minigame Pool
	//Booster Smash at the top intentionally
	BOOSTER_SMASH		(2,"Booster Smash","Smash") { public MiniGame getGame() { return new BoosterSmash(); } },					//Author: Atia
	MINEFIELD_MULTIPLIER(2,"Minefield Multiplier","Multiplier") {public MiniGame getGame() { return new MinefieldMultiplier(); } }, //Author: Amy
	COIN_FLIP			(2,"CoinFlip","Flip") { public MiniGame getGame() { return new CoinFlip(); } },								//Author: Amy
	MATH_TIME			(2,"Math Time","Math") { public MiniGame getGame() { return new MathTime(); } },							//Author: Atia
	STRIKE_IT_RICH		(2,"Strike it Rich","Strike") { public MiniGame getGame() { return new StrikeItRich(); } },					//Author: Atia
	DEAL_OR_NO_DEAL		(2,"Deal or No Deal","DoND") { public MiniGame getGame() { return new DealOrNoDeal(); } },					//Author: Atia
	DOUBLE_TROUBLE		(2,"Double Trouble","Double") { public MiniGame getGame() { return new DoubleTrouble(); } },				//Author: JerryEris
	DOUBLE_ZERO     	(2,"Double Zero","00") { public MiniGame getGame() { return new DoubleZeroes(); } },						//Author: JerryEris
	CALL_YOUR_SHOT		(2,"Call Your Shot","Call") { public MiniGame getGame() { return new CallYourShot(); } },					//Author: JerryEris
	OPENPASS		(2,"Open, Pass","Open") { public MiniGame getGame() { return new OpenPass(); } },					//Author: JerryEris
	SHUT_THE_BOX		(2,"Shut the Box","Shut") { public MiniGame getGame() { return new ShutTheBox(); } },						//Author: StrangerCoug
	BOMB_ROULETTE		(2,"Bomb Roulette","Roulette") { public MiniGame getGame() { return new BombRoulette(); } },				//Author: StrangerCoug
	HI_LO_DICE			(2,"Hi/Lo Dice","Hi/Lo") { public MiniGame getGame() { return new HiLoDice(); } },							//Author: StrangerCoug
	//Games rotated out
	THE_OFFER			(0,"The Offer","Offer") { public MiniGame getGame() { return new TheOffer(); }	},							//Author: Amy
	TRIPLE_PLAY			(0,"Triple Play","Triple") { public MiniGame getGame() { return new TriplePlay(); } },						//Author: Atia
	GAMBLE				(0,"The Gamble","Gamble") { public MiniGame getGame() { return new Gamble(); } },							//Author: Atia
	DEUCES_WILD			(0,"Deuces Wild","Deuces") { public MiniGame getGame() { return new DeucesWild(); } },						//Author: StrangerCoug
	//BUMPER_CASH   	(2,"Bumper Grab","Bumper") { public MiniGame getGame() { return new BumperGrab(); } },						//Author: Tara
	//Bonus games never appear in the pool
	SUPERCASH			(0,"SUPERCASH","Super") { public MiniGame getGame() { return new Supercash(); } },
	DIGITAL_FORTRESS	(0,"DIGITAL FORTRESS","Fortress") { public MiniGame getGame() { return new DigitalFortress(); } },
	SPECTRUM			(0,"SPECTRUM","Spectrum") { public MiniGame getGame() { return new Spectrum(); } },
	HYPERCUBE			(0,"HYPERCUBE","Hyper^3") { public MiniGame getGame() { return new Hypercube(); } },
	//This game also never appears in the pool, for obvious reasons
	SUPER_BONUS_ROUND	(0,null,"sbr") {public MiniGame getGame() { return new SuperBonusRound(); } };
	
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
