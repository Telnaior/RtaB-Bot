package tel.discord.rtab;

import java.util.ArrayList;

import tel.discord.rtab.enums.BombType;
import tel.discord.rtab.enums.BoostType;
import tel.discord.rtab.enums.CashType;
import tel.discord.rtab.enums.Events;
import tel.discord.rtab.enums.Games;
import tel.discord.rtab.enums.SpaceType;
import tel.discord.rtab.enums.WeightedSpace;

class Board
{
	ArrayList<SpaceType> typeBoard;
	ArrayList<CashType> cashBoard;
	ArrayList<BoostType> boostBoard;
	ArrayList<BombType> bombBoard;
	ArrayList<Games> gameBoard;
	ArrayList<Events> eventBoard;
	
	Board(int size, int players)
	{
		typeBoard = new ArrayList<>(size+5);
		cashBoard = new ArrayList<>(size+5);
		boostBoard = new ArrayList<>(size+5);
		bombBoard = new ArrayList<>(size+5);
		gameBoard = new ArrayList<>(size+5);
		eventBoard = new ArrayList<>(size+5);
		addSpaces(size, players);
	}
	
	void addSpaces(int size, int players)
	{
		//Boost each board
		addToBoard(size, players, typeBoard,SpaceType.values());
		addToBoard(size, players, cashBoard,CashType.values());
		addToBoard(size, players, boostBoard,BoostType.values());
		addToBoard(size, players, bombBoard,BombType.values());
		addToBoard(size, players, gameBoard,Games.values());
		addToBoard(size, players, eventBoard,Events.values());
	}
	private <T extends WeightedSpace> void addToBoard(int spaces, int players, ArrayList<T> board, T[] values)
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
		for(int i=0; i<spaces; i++)
		{
			//Get random spot in weight table
			random = Math.random() * totalWeight;
			//Find where we actually landed
			int search=0;
			while(cumulativeWeights[search] < random)
				search++;
			//And set the value to that
			board.add(values[search]);
		}
		return;
	}
}
