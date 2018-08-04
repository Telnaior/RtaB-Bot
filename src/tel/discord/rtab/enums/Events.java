package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	MYSTERY_MONEY	(15),
	REPEAT			( 6),
	STREAKP1		( 6),
	GAME_LOCK		( 5),
	SHUFFLE_ORDER	( 5),
	BOOST_DRAIN		( 4),
	MINEFIELD		( 4),
	STREAKP2		( 4),
	JOKER			( 3),
	LOCKDOWN		( 3),
	COMMUNISM		( 3),
	BLAMMO_FRENZY	( 2),
	STREAKP3		( 2),
	SPLIT_SHARE		( 2),
	END_ROUND		( 1),
	STARMAN			( 1),
	JACKPOT			( 1);

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
