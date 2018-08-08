package tel.discord.rtab.enums;

public enum BombType implements WeightedSpace
{
	NORMAL		(16),
	BANKRUPT	( 2),
	BOOSTHOLD	( 2),
	CHAIN		( 2),
	REVERSE		( 2),
	DUD			( 1);

	int weight;
	BombType(int valueWeight)
	{
		weight = valueWeight;
	}
	@Override
	public int getWeight()
	{
		return weight;
	}
}