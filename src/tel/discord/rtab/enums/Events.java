package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	STREAKPSMALL	( 7),
	BOOST_CHARGER	( 7),
	DOUBLE_DEAL		( 7),
	BOOST_DRAIN		( 6),
	DRAW_TWO		( 6),
	SKIP_TURN		( 6),
	MINEFIELD		( 5),
	BOOST_MAGNET	( 5),
	REVERSE_ORDER	( 5),
	JOKER			( 4)
	{
		@Override
		public int getWeight(int playerCount)
		{
			//Jokers don't belong in 2p, and have reduced frequency in 3p
			switch(playerCount)
			{
			case 2:
				return 0;
			case 3:
				return 1;
			default:
				return weight;
			}
		}
	},
	LOCKDOWN		( 4),
	STREAKPLARGE	( 4),
	COMMUNISM		( 3),
	BLAMMO_FRENZY	( 3),
	DRAW_FOUR		( 3),
	BRIBE			( 2),
	SPLIT_SHARE		( 2),
	END_ROUND		( 2),
	SUPER_JOKER		( 1)
	{
		@Override
		public int getWeight(int playerCount)
		{
			//Super Jokers don't belong in small games
			return (playerCount < 4) ? 0 : weight;
		}
	},
	STARMAN			( 1),
	JACKPOT			( 1);

	int weight;
	Events(int valueWeight)
	{
		weight = valueWeight;
	}
	@Override
	public int getWeight(int playerCount)
	{
		//This gets overriden by a few events that don't belong in small or large games
		return weight;
	}
}
