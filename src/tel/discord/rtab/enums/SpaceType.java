package tel.discord.rtab.enums;

public enum SpaceType implements WeightedSpace
{
	CASH	(10),
	BOOSTER	( 2),
	GAME	( 2),
	EVENT	( 2);
	
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
