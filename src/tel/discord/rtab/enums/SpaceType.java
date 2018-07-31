package tel.discord.rtab.enums;

public enum SpaceType implements WeightedSpace
{
	CASH	(55),
	BOOSTER	(12),
	GAME	(12),
	EVENT	(20),
	BLAMMO  ( 1);
	
	int weight;
	SpaceType(int spaceWeight)
	{
		weight = spaceWeight;
	}
	@Override
	public int getWeight()
	{
		return weight;
	}
}
