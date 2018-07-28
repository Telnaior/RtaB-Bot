package tel.discord.rtab;

import tel.discord.rtab.enums.BombType;
import tel.discord.rtab.enums.BoostType;
import tel.discord.rtab.enums.CashType;
import tel.discord.rtab.enums.Events;
import tel.discord.rtab.enums.Games;
import tel.discord.rtab.enums.SpaceType;
import tel.discord.rtab.enums.WeightedSpace;

class Board
{
	SpaceType[] typeBoard;
	CashType[] cashBoard;
	BoostType[] boostBoard;
	BombType[] bombBoard;
	Games[] gameBoard;
	Events[] eventBoard;
	
	Board(int size)
	{
		//Initialise types
		typeBoard = initBoard(new SpaceType[size],SpaceType.values());
		cashBoard = initBoard(new CashType[size],CashType.values());
		boostBoard = initBoard(new BoostType[size],BoostType.values());
		bombBoard = initBoard(new BombType[size],BombType.values());
		gameBoard = initBoard(new Games[size],Games.values());
		eventBoard = initBoard(new Events[size],Events.values());
	}
	private <T extends WeightedSpace> T[] initBoard(T[] board, T[] values)
	{
		//Declare possible values and weights
		//Autogenerate cumulative weights
		int[] cumulativeWeights = new int[values.length];
		int totalWeight = 0;
		for(int i=0; i<values.length; i++)
		{
			totalWeight += values[i].getWeight();
			cumulativeWeights[i] = totalWeight;
		}
		double random;
		for(int i=0; i<board.length; i++)
		{
			//Get random spot in weight table
			random = Math.random() * totalWeight;
			//Find where we actually landed
			int search=0;
			while(cumulativeWeights[search] < random)
				search++;
			//And set the value to that
			board[i] = values[search];
		}
		return board;
	}
}
