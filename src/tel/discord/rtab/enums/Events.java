package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	BOOST_DRAIN	(7),
	REPEAT		(7),
	GAME_LOCK	(6),
	MINEFIELD	(5),
	JOKER		(4),
	LOCKDOWN	(3),
	STARMAN		(2),
	SPLIT_SHARE	(1),
	JACKPOT		(1),
	BONUSP1		(6),
	BONUSP2		(4),
	BONUSP3		(2);

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
