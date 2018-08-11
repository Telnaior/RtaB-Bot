package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	REPEAT			( 6),
	STREAKP1		( 6),
	BOOST_CHARGER	( 6),
	GAME_LOCK		( 5),
	SHUFFLE_ORDER	( 5),
	STREAKP2		( 5),
	BOOST_DRAIN		( 4),
	MINEFIELD		( 4),
	DOUBLE_DEAL		( 4),
	JOKER			( 3),
	LOCKDOWN		( 3),
	STREAKP3		( 3),
	COMMUNISM		( 2),
	BLAMMO_FRENZY	( 2),
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
