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
		typeBoard = (SpaceType[]) initBoard(size,SpaceType.values());
		cashBoard = (CashType[]) initBoard(size,CashType.values());
		boostBoard = (BoostType[]) initBoard(size,BoostType.values());
		bombBoard = (BombType[]) initBoard(size,BombType.values());
		gameBoard = (Games[]) initBoard(size,Games.values());
		eventBoard = (Events[]) initBoard(size,Events.values());
	}
	private WeightedSpace[] initBoard(int size, WeightedSpace[] values)
	{
		WeightedSpace[] board = new WeightedSpace[size];
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
		for(int i=0; i<size; i++)
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
