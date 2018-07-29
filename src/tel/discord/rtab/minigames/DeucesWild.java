package tel.discord.rtab.minigames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import tel.discord.rtab.enums.CardRank;
import tel.discord.rtab.enums.CardSuit;
import tel.discord.rtab.enums.PokerHand;
import tel.discord.rtab.objs.Card;
import tel.discord.rtab.objs.Deck;

public class DeucesWild implements MiniGame {
	static final String NAME = "Deuces Wild";
	static final boolean BONUS = false;
	static final int BOARD_SIZE = 52;
	static Deck deck;
	static Card[] cardsPicked = new Card[5];
	static boolean[] cardsHeld = new boolean[5];
	ArrayList<Card> board = new ArrayList<Card>(BOARD_SIZE);
	int lastSpace;
	Card lastPicked;
	PokerHand hand = PokerHand.NOTHING;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	boolean firstPlay = true;
	boolean redrawUsed;
	byte gameStage;

	@Override
	public LinkedList<String> initialiseGame()
	{
		LinkedList<String> output = new LinkedList<>();
		//Initialise board
		board.clear();
		deck = new Deck();
		deck.shuffle(); // since the Deck object has its own shuffle method that can be called
		for(int i=0; i<BOARD_SIZE; i++)
				board.add(deck.dealCard());
		cardsPicked = new Card[5];
		cardsHeld = new boolean[5];
		pickedSpaces = new boolean[BOARD_SIZE];
		redrawUsed = false;
		firstPlay = false;
		gameStage = 0;
		//Display instructions
		output.add("In Deuces Wild, your objective is to obtain the best poker hand possible.");
		output.add("We have shuffled a standard deck of 52 playing cards, from which you will pick five cards.");
		output.add("As the name of the game suggests, deuces (twos) are wild. "
				+ "Those are always treated as the best card possible.");
		output.add("After you draw your five cards, you may redraw as many of them as you wish, but only once.");
		output.add("You must get at least a three of a kind to win any money. That pays $50,000.");
		output.add("Straights and flushes each pay $100,000. A full house pays $150,000, a four of a kind pays $250,000, "
				+ "a straight flush pays $450,000, a five of a kind pays $750,000, a wild royal flush pays $1,250,000, "
				+ "and four deuces pay $10,000,000.");
		output.add("If you are lucky enough to get a natural royal flush, you will win $40,000,000!");
		output.add("Best of luck! Pick your first card when you're ready.");
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		
		if (gameStage == 5) {
			if (pick.toUpperCase().equals("STOP")) {
				redrawUsed = true;
			}
			else if (pick.toUpperCase().equals("REDRAW")) {
				output.add("Redrawing all five cards.");
				redrawUsed = true;
				gameStage = 0;
				output.add(generateBoard());
				output.add("Select your first card of the redraw when you are ready.");
			}
			else if (pick.toUpperCase().startsWith("HOLD ")) {
				String[] tokens = pick.split("\\s");
				
				// If there are any non-numeric tokens after "HOLD", assume it's just the player talking
				for (int i = 1; i < tokens.length; i++) {
					if (!isNumber(tokens[i]))
						return output;
				}
				
				// Make sure the player's choices correspond to actual cards
				try {
					for (int i = 1; i < tokens.length; i++)
					{
						cardsHeld[Integer.parseInt(tokens[i])-1] = true;
					}
					
					// If we get here, we're good :)
					redrawUsed = true;
					gameStage = 0;
					
					String cardsHeldAsString = "Cards held: ";
					String cardsRedrawingAsString = "Cards being redrawn: ";
					

					for (int i = 0; i < cardsHeld.length; i++) {
						if (cardsHeld[i])
							cardsHeldAsString += cardsPicked[i].toStringShort() + " ";
						else cardsRedrawingAsString += cardsPicked[i].toStringShort() + " ";
					}
					
					if (cardsRedrawingAsString.equals("Cards being redrawn: ")) { // i.e. there aren't any
						output.add("All five cards held; ending game.");
						gameStage = 5;
						return output;
					}
					
					output.add(cardsHeldAsString);
					output.add(cardsRedrawingAsString);
					
					// Find out what stage we should be on now
					for (int i = 0; i < cardsHeld.length; i++) {
						if (!cardsHeld[i])
							break;
						else gameStage++;
					}
					output.add(generateBoard());
					output.add("Select your first card of the redraw when you are ready.");
				}
				catch (IndexOutOfBoundsException e) {
					// Clear out the hold flags to be safe
					for (int i = 0; i < cardsHeld.length; i++) {
						cardsHeld[i] = false;
					}
					output.add("Invalid card(s).");
					return output;
				}
			}
			return output;
		}
		else {
			if(!isNumber(pick))
			{
				//Random unrelated non-number doesn't need feedback
				return output;
			}
			if(!checkValidNumber(pick))
			{
				output.add("Invalid pick.");
				return output;
			}
			else
			{
				lastSpace = Integer.parseInt(pick)-1;
				pickedSpaces[lastSpace] = true;
				lastPicked = board.get(lastSpace);
				cardsPicked[gameStage] = lastPicked;
				if(redrawUsed)
					cardsHeld[gameStage] = true;
				do {
					gameStage++;
				} while (gameStage < 5 && cardsHeld[gameStage]);
				if (gameStage == 5)
					hand = evaluateHand(cardsPicked);
				output.add(String.format("Space %d selected...",lastSpace+1));
				output.add("**" + lastPicked.toString() + "**");
				output.add(generateBoard());
				if (gameStage == 5) {
					if (hand != PokerHand.NATURAL_ROYAL && !redrawUsed) {
						output.add("You may now hold any or all of your five cards by typing HOLD followed by the numeric positions "
								+ "of each card.");
						output.add("For example, type 'HOLD 1' to hold only the " + cardsPicked[0] +
								", or type 'HOLD 2 5' **in a single message** to hold the "
								+ cardsPicked[1] + " as well as the " + cardsPicked[4] + ".");
						output.add("The cards you do not hold will all be redrawn in the hopes of a better hand.");
						output.add(String.format("If you like your hand, you may also type 'STOP' to end the game and claim your "+
								"prize of $%,d.", getMoneyWon()));
						output.add("Alternatively, you can redraw every single card by typing 'REDRAW'.");
					}
				}
				else {
					output.add("Select another card.");
				}
			}
			return output;
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
		display.append("\n\n" + "Current hand: "); // The concatenation here is more for human legibility than anything
		for (int i = 0; i < cardsPicked.length; i++)
		{
			if (cardsPicked[i] == null)
				break;
			if (redrawUsed && !cardsHeld[i])
				display.append("   ");
			else display.append(cardsPicked[i].toStringShort() + " ");
		}
		if (gameStage == 5)
			display.append("(" + hand.toString() + ")");
		display.append("```");
		return display.toString();
	}

	@Override
	public boolean isGameOver()
	{
		if (gameStage == 5) {
			if (hand == PokerHand.NATURAL_ROYAL)
				return true;
			else return redrawUsed;
		}
		
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

	// This is probably not the most efficient way to write the hand evaluator--some things are checked more than once. 
	public PokerHand evaluateHand(Card[] cards) {
		if (cards.length != 5)
			throw new IllegalArgumentException("The hand evaluator function needs 5 cards to work; it was passed " + cards.length + ".");

		byte[] rankCount = new byte[CardRank.values().length];
		byte[] suitCount = new byte[CardSuit.values().length];

		for (int i = 0; i < cards.length; i++) {
			rankCount[cards[i].getRank().ordinal()]++;
			if (cards[i].getRank() != CardRank.DEUCE)      // for the purposes of this evaluator, deuces have no suit; that's the only 
				suitCount[cards[i].getSuit().ordinal()]++; // way I can think of to get it to work right when checking for a flush
		}

		// If we have four deuces, that precludes a natural royal flush and outpays a wild royal flush; so it's less work to check for that first
		if (rankCount[CardRank.DEUCE.ordinal()] == 4)
			return PokerHand.FOUR_DEUCES;

		CardRank highCardOfStraight = findStraightHighCard(rankCount); // If this is null, we do not have a straight
		boolean isFlush = isFlush(suitCount);

		// Put off the five of a kind check until these are all done--if we had one, we would have paid higher for four deuces already
		if (highCardOfStraight != null && isFlush) {
			if (highCardOfStraight == CardRank.ACE) {
				if (rankCount[CardRank.DEUCE.ordinal()] == 0)
					return PokerHand.NATURAL_ROYAL;
				else return PokerHand.WILD_ROYAL;
			}
			else return PokerHand.STRAIGHT_FLUSH;
		}
		if (isFlush) return PokerHand.FLUSH;
		if (highCardOfStraight != null) return PokerHand.STRAIGHT;

		byte modeOfRanks = modeOfRanks(rankCount); // That is, how many are there of the most common rank?

		switch (modeOfRanks) {
			case 3: 
				if (hasExtraPair(rankCount)) return PokerHand.FULL_HOUSE;
				else return PokerHand.THREE_OF_A_KIND;
			case 4: return PokerHand.FOUR_OF_A_KIND;
			case 5: return PokerHand.FIVE_OF_A_KIND;
			default: return PokerHand.NOTHING;
		}
	}

	private CardRank findStraightHighCard(byte[] rankCount) {
		// If we have any paired cards other than deuces, it can't be a straight, so check for that first
		for (int i = 0; i < rankCount.length; i++) {
			if (i == CardRank.DEUCE.ordinal())
				continue;
			if (rankCount[i] > 1)
				return null;
		}

		for (int i = 0; i < rankCount.length - 4; i++) {
			if (rankCount[i] + rankCount[i+1] + rankCount[i+2] + rankCount[i+3] + rankCount[i+4] == 5)
				return CardRank.values()[i+4];
			if (i > CardRank.DEUCE.ordinal() && rankCount[i] + rankCount[i+1] + rankCount[i+2] + 
					rankCount[i+3] + rankCount[i+4] + rankCount[CardRank.DEUCE.ordinal()] == 5)
				return CardRank.values()[i+4];
		}

		// The above scan doesn't catch an ace-high straight; so that is our final check:
		if (rankCount[CardRank.ACE.ordinal()] + rankCount[CardRank.DEUCE.ordinal()] + rankCount[CardRank.TEN.ordinal()]
				+ rankCount[CardRank.JACK.ordinal()] + rankCount[CardRank.QUEEN.ordinal()] + rankCount[CardRank.KING.ordinal()] == 5)
			return CardRank.ACE;
		
		return null;
	}

	private boolean isFlush(byte[] suitCount) {
		boolean suitFound = suitCount[0] != 0;

		for (int i = 1; i < suitCount.length; i++) {
			if (suitCount[i] != 0) {
				if (suitFound) return false;
				else suitFound = true;
			}
		}

		return true;
	}

	private byte modeOfRanks(byte[] rankCount) {
		byte deuces = rankCount[CardRank.DEUCE.ordinal()];
		byte max = (byte)(rankCount[CardRank.ACE.ordinal()] + deuces);

		for (int i = CardRank.THREE.ordinal(); i < rankCount.length; i++) {
			byte sum = (byte)(rankCount[i] + deuces);
			if (sum > max)
				max = sum;
		}

		return max;	
	}

	private boolean hasExtraPair(byte[] rankCount) {
		/* 
 		 * This works, but isn't entirely clear why:
 		 * There can only be a maximum of one deuce in a full house
 		 * And if there is one, it's pair + pair + deuce
 		 * Otherwise the result would be four or five of a kind and we wouldn't get to this point anyway
  		 * In any case, in a full house sorting the array comes out as either (0,2,3) or (1,2,2)
 		 * And the second-to-last value would always be 2.
 		 */
 		byte[] sortedRankCount = rankCount;
 		Arrays.sort(sortedRankCount);
 		return sortedRankCount[rankCount.length - 2] == 2;
	}

	@Override
	public String getBotPick() {
		//Bot will never redraw, unless someone wants to teach it poker strategy (lol)
		if(gameStage == 5)
			return "STOP";
		ArrayList<Integer> openSpaces = new ArrayList<>(BOARD_SIZE);
		for(int i=0; i<BOARD_SIZE; i++)
			if(!pickedSpaces[i])
				openSpaces.add(i+1);
		return String.valueOf(openSpaces.get((int)(Math.random()*openSpaces.size())));
	}
	
	@Override
	public String toString()
	{
		return NAME;
	}
}
