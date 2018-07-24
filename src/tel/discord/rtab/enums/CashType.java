package tel.discord.rtab.enums;

public enum CashType implements WeightedSpace
{
	//Negative
	N25K	(-  25000,3),
	N20K	(-  20000,3),
	N15K	(-  15000,3),
	N10K	(-  10000,3),
	N05K	(-   5000,3),
	//Small
	P10K	(   10000,5),
	P20K	(   20000,5),
	P30K	(   30000,5),
	P40K	(   40000,5),
	P50K	(   50000,5),
	P60K	(   60000,5),
	P70K	(   70000,5),
	P80K	(   80000,5),
	P90K	(   90000,5),
	P100K	(  100000,5),
	//Big
	P125K	(  125000,2),
	P150K	(  150000,2),
	P175K	(  175000,2),
	P200K	(  200000,2),
	P250K	(  250000,2),
	P300K	(  300000,2),
	P400K	(  400000,2),
	P500K	(  500000,2),
	P750K	(  750000,2),
	P1000K	( 1000000,2);
	
	int value;
	int weight;
	CashType(int cashValue, int valueWeight)
	{
		value = cashValue;
		weight = valueWeight;
	}
	@Override
	public int getWeight()
	{
		return weight;
	}
}
