package tel.discord.rtab.enums;

public enum SpaceType implements WeightedSpace
{
	CASH	(60),
	BOOSTER	(13),
	GAME	(13),
	EVENT	(13),
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
