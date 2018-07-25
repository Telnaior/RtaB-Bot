package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	REPEAT			(6),
	BONUSP1			(6),
	GAME_LOCK		(5),
	BOOST_DRAIN		(5),
	MINEFIELD		(4),
	BONUSP2			(4),
	JOKER			(3),
	LOCKDOWN		(3),
	STARMAN			(2),
	BONUSP3			(2),
	BLAMMO_FRENZY	(2),
	SPLIT_SHARE		(1),
	JACKPOT			(1);

	int weight;
	Events(int valueWeight)
	{
		weight = valueWeight;
	}
	@Override
	public int getWeight()
	{
		return weight;
	}
}
