package tel.discord.rtab.enums;

public enum BoostType implements WeightedSpace
{
	//Negative
	N50	(- 50,3),
	N45	(- 45,3),
	N40	(- 40,3),
	N35	(- 35,3),
	N30	(- 30,3),
	N25	(- 25,3),
	N20	(- 20,3),
	N15	(- 15,3),
	N10	(- 10,3),
	N5	(-  5,3),
	//Small
	P5	(   5,7),
	P10	(  10,7),
	P15	(  15,7),
	P20	(  20,7),
	P25	(  25,7),
	P30	(  30,7),
	P35	(  35,7),
	P40	(  40,7),
	P45	(  45,7),
	P50	(  50,7),
	//Big
	P60	(  60,4),
	P70	(  70,4),
	P80	(  80,4),
	P90	(  90,4),
	P100( 100,4),
	P125( 125,2),
	P150( 150,2),
	P200( 200,2),
	P300( 300,1),
	P500( 500,1);
	
	int value;
	int weight;
	BoostType(int boostValue, int valueWeight)
	{
		value = boostValue;
		weight = valueWeight;
	}
	@Override
	public int getWeight()
	{
		return weight;
	}
	public int getValue()
	{
		return value;
	}
}
