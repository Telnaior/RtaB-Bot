package tel.discord.rtab.enums;

public enum Events implements WeightedSpace {
	BOOST_DRAIN(2), MINEFIELD(3), LOCKDOWN(1), STARMAN(1), REPEAT(3);

	int weight;
	Events(int eventWeight)
	{
		this.weight = eventWeight;
	}
	public int getWeight()
	{
		return weight;
	}
}
