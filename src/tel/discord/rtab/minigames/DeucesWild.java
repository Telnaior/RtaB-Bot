package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import tel.discord.rtab.enums.CardRank;
import tel.discord.rtab.enums.CardSuit;
import tel.discord.rtab.enums.PokerHand;
import tel.discord.rtab.objs.Card;

public class DeucesWild implements MiniGame {
	static final boolean BONUS = false;
	static final int BOARD_SIZE = 52;
	// TODO: The next two make no sense right now and need to be adjusted to handle cards. I haven't done the card object yet, though. --Coug
	static final int[] VALUES = {0,1000,10000,100000,1000000}; //Bad things happen if this isn't sorted
	static final int NEEDED_TO_WIN = (BOARD_SIZE/VALUES.length);
	static int[] numberPicked = new int[VALUES.length];
	ArrayList<Integer> board = new ArrayList<Integer>(BOARD_SIZE);
	int lastSpace;
	int lastPicked;
	PokerHand hand = PokerHand.NOTHING;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	boolean firstPlay = true;
	boolean pinchMode = false;
	
	@Override
	public void sendNextInput(String pick)
	{
		if(!isNumber(pick))
		{
			lastPicked = -2;
			return;
		}
		if(!checkValidNumber(pick))
		{
			lastPicked = -1;
			return;
		}
		else
		{
			lastSpace = Integer.parseInt(pick)-1;
			pickedSpaces[lastSpace] = true;
			lastPicked = board.get(lastSpace);
			numberPicked[Arrays.binarySearch(VALUES,lastPicked)] ++;
			if(numberPicked[Arrays.binarySearch(VALUES,lastPicked)] >= (NEEDED_TO_WIN-1))
				pinchMode = true;
		}
	}

	boolean isNumber(String message)
	{
		try
		{
			//If this doesn't throw an exception we're good
			Integer.parseInt(message);
			return true;
		}
		catch(NumberFormatException e1)
		{
			return false;
		}
	}
	boolean checkValidNumber(String message)
	{
		int location = Integer.parseInt(message)-1;
		return (location >= 0 && location < BOARD_SIZE && !pickedSpaces[location]);
	}

	@Override
	public LinkedList<String> getNextOutput()
	{
		LinkedList<String> output = new LinkedList<>();
		if(firstPlay)
		{
			//Initialise board
			board.clear();
			for(int i=0; i<VALUES.length; i++)
				for(int j=0; j<NEEDED_TO_WIN; j++)
					board.add(VALUES[i]);
			Collections.shuffle(board);
			numberPicked = new int[VALUES.length];
			pickedSpaces = new boolean[BOARD_SIZE];
			pinchMode = false;
			//Display instructions
			output.add("In Deuces Wild, your objective is to obtain the best poker hand possible.");
			output.add("We have shuffled a standard deck of 52 playing cards, from which you will pick five cards.");
			output.add("As the name of the game suggests, deuces are wild. Those are always treated as the best card possible");
			output.add("After you draw your five cards, you may redraw as many of them as you wish, but only once.");
			output.add("You must get at least a three of a kind to win any money. That pays $50,000.");
            output.add("Straights and flushes each pay $100,000. A full house pays $150,000, a four of a kind pays $250,000, "
                    + "a straight flush pays $450,000, a five of a kind pays $750,000, a wild royal flush pays $1,250,000, "
                    + "and four deuces pay $10,000,000.");
            output.add("If you are lucky enough to get a natural royal flush, you will win $40,000,000!");
			output.add("Best of luck! Pick your first space when you're ready.");
			firstPlay = false;
		}
		else if(lastPicked == -2)
		{
			//Random unrelated non-number doesn't need feedback
			return output;
		}
		else if(lastPicked == -1)
		{
			output.add("Invalid pick.");
		}
		else
		{
			output.add(String.format("Space %d selected...",lastSpace+1));
			if(pinchMode)
				output.add("...");
			output.add(String.format("$%,d!",lastPicked));
		}
		output.add(generateBoard());
		return output;
	}
	
	String generateBoard()
	{
		StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("      DEUCES WILD\n   ");
		for(int i=0; i<BOARD_SIZE; i++)
		{
			if(pickedSpaces[i])
			{
				display.append("  ");
			}
			else
			{
				display.append(String.format("%02d",(i+1)));
			}
			if (i == 45)
				display.append("\n   ");
			else if(i == 5 || (i > 5 && i%8 == 5))
				display.append("\n");
			else
				display.append(" ");
		}
		display.append("\n");
		//Next display how many of each we have
		for(int i=0; i<VALUES.length; i++)
		{
			display.append(String.format("%1$dx $%2$,d\n",numberPicked[i],VALUES[i]));
		}
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		for(int search : numberPicked)
			if(search >= NEEDED_TO_WIN)
				return true;
		return false;
	}

	@Override
	public int getMoneyWon()
	{
		firstPlay = true;
		switch(hand) {
			case NOTHING: return 0;
			case THREE_OF_A_KIND: return 50000;
			case STRAIGHT: case FLUSH: return 100000;
			case FULL_HOUSE: return 150000;
			case FOUR_OF_A_KIND: return 250000;
			case STRAIGHT_FLUSH: return 450000;
			case FIVE_OF_A_KIND: return 750000;
			case WILD_ROYAL: return 1250000;
			case FOUR_DEUCES: return 10000000;
			case NATURAL_ROYAL: return 40000000;
			default: throw new IllegalArgumentException(); // since the above is supposed to already handle everything
		}
	}

	@Override
	public boolean isBonusGame() {
		return BONUS;
	}

	public Card[] createDeck() {
		CardRank[] ranks = {CardRank.ACE, CardRank.DEUCE, CardRank.THREE, CardRank.FOUR, CardRank.FIVE, CardRank.SIX, CardRank.SEVEN,
				CardRank.EIGHT, CardRank.NINE, CardRank.TEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING};
		CardSuit[] suits = {CardSuit.CLUBS, CardSuit.DIAMONDS, CardSuit.HEARTS, CardSuit.SPADES};
		Card[] cards = new Card[ranks.length * suits.length];

		for (int i = 0; i < cards.length; i++) {
			cards[i] = new Card(ranks[i/suits.length],suits[i%suits.length]);
		}

		return cards;
	}
}