package tel.discord.rtab.enums;

public enum CashType implements WeightedSpace
{
	//Negative
	N25K	(-  25000,3,null),
	N20K	(-  20000,3,null),
	N15K	(-  15000,3,null),
	N10K	(-  10000,3,null),
	N05K	(-   5000,3,null),
	//Small
	P10K	(   10000,5,null),
	P20K	(   20000,5,null),
	P30K	(   30000,5,null),
	P40K	(   40000,5,null),
	P50K	(   50000,5,null),
	P60K	(   60000,5,null),
	P70K	(   70000,5,null),
	P80K	(   80000,5,null),
	P90K	(   90000,5,null),
	P100K	(  100000,5,null),
	//Big
	P111K	(  111111,2,null),
	P125K	(  125000,2,null),
	P150K	(  150000,2,null),
	P200K	(  200000,2,null),
	P250K	(  250000,2,null),
	P300K	(  300000,2,null),
	P400K	(  400000,2,null),
	P500K	(  500000,2,null),
	P750K	(  750000,2,null),
	P1000K	( 1000000,2,null),
	//Trophies
	S1TROPHY(   68000,1,"a replica of Vash's Season 1 trophy"),
	S2TROPHY(   60000,1,"a replica of Charles510's Season 2 trophy"),
	//Meme
	P10		(      10,1,null),
	MYSTERY (       0,3,null),
	DB1		(   22805,1,"a DesertBuck"),
	DANGAN  (   11037,1,"an unfamiliar memory"),
	GOVERNOR(   26000,1,"the Governor's favourite");
	
	int value;
	int weight;
	String prize;
	CashType(int cashValue, int valueWeight, String prizeText)
	{
		value = cashValue;
		weight = valueWeight;
		prize = prizeText;
	}
	@Override
	public int getWeight(int playerCount)
	{
		//Cash types don't care about playercount
		return weight;
	}
	public int getValue()
	{
		return value;
	}
	public String getPrize()
	{
		return prize;
	}
}
