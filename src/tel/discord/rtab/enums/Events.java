package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	STREAKPSMALL	( 7),
	BOOST_CHARGER	( 7),
	DOUBLE_DEAL		( 7),
	//BOOST_DRAIN		( 6), Not in Season 3
	DRAW_TWO		( 6),
	SKIP_TURN		( 6),
	PEEK_REPLENISH	( 6),
	REVERSE_ORDER	( 5),
	STREAKPLARGE	( 5),
	BOWSER			( 5),
	BOOST_MAGNET	( 4),
	MINEFIELD		( 4),
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
	DRAW_FOUR		( 3)
	{
		@Override
		public int getWeight(int playerCount)
		{
			//This space would be a little too painful in a small game.
			switch(playerCount)
			{
			case 2:
				return 1;
			case 3:
				return 2;
			default:
				return weight;
			}
		}
	},
	LOCKDOWN		( 3),
	BLAMMO_FRENZY	( 3),
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
	JACKPOT			( 1),
	//Bowser Events
	COINS_FOR_BOWSER( 0) { public String getName() { return "Cash for Bowser"; } },
	BOWSER_POTLUCK	( 0) { public String getName() { return "Bowser's Cash Potluck"; } },
	RUNAWAY			( 0) { public String getName() { return "Billion-Dollar Present"; } },
	BOWSER_JACKPOT	( 0) { public String getName() { return "Bowser's Jackpot"; } },
	COMMUNISM		( 0) { public String getName() { return "Bowser Revolution"; } };

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
	public String getName()
	{
		//This gets overridden by events that actually use it
		return "NONAME";
	}
}
