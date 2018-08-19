package tel.discord.rtab.enums;

public enum Events implements WeightedSpace
{
	REPEAT			( 6),
	STREAKP1		( 6),
	BOOST_CHARGER	( 6),
	DOUBLE_DEAL		( 5),
	SHUFFLE_ORDER	( 5),
	STREAKP2		( 5),
	GAME_LOCK		( 4)
	{
		@Override
		public int getWeight(int playerCount)
		{
			//Minigames are less common in large games, so the minigame lock doesn't really belong there either
			return (playerCount > 4) ? Math.max(0,weight-(playerCount-4)) : weight;
		}
	},
	BOOST_DRAIN		( 4),
	MINEFIELD		( 4),
	JOKER			( 3)
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
	LOCKDOWN		( 3),
	STREAKP3		( 3),
	COMMUNISM		( 2),
	BLAMMO_FRENZY	( 2),
	BRIBE			( 2),
	SPLIT_SHARE		( 2),
	SUPER_JOKER		( 1)
	{
		@Override
		public int getWeight(int playerCount)
		{
			//Super Jokers don't belong in small games
			return (playerCount < 4) ? 0 : weight;
		}
	},
	END_ROUND		( 1),
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
