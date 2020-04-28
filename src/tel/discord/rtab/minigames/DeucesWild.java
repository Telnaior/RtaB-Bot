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
	Deck deck;
	Card[] cardsPicked = new Card[5];
	boolean[] cardsHeld = new boolean[5];
	ArrayList<Card> board = new ArrayList<Card>(BOARD_SIZE);
	int lastSpace;
	Card lastPicked;
	PokerHand hand = PokerHand.NOTHING;
	boolean[] pickedSpaces = new boolean[BOARD_SIZE];
	boolean redrawUsed;
	byte gameStage;
	int baseMultiplier;

	@Override
	public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
	{
		//TODO make base multiplier matter
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
		gameStage = 0;
		this.baseMultiplier = baseMultiplier;
		//Display instructions
		output.add("In Deuces Wild, your objective is to obtain the best poker hand possible.");
		output.add("We have shuffled a standard deck of 52 playing cards, from which you will pick five cards.");
		output.add("As the name of the game suggests, deuces (twos) are wild. "
				+ "Those are always treated as the best card possible.");
		output.add("After you draw your five cards, you may redraw as many of them as you wish, but only once.");
		output.add(String.format("You must get at least a pair to win any money. That pays $%,d.",getMoneyWon(PokerHand.ONE_PAIR)));
		output.add(String.format("Two pairs pay $%,d, a three of a kind pays $%,d, a straight pays $%,d, ",
				getMoneyWon(PokerHand.TWO_PAIR), getMoneyWon(PokerHand.THREE_OF_A_KIND), getMoneyWon(PokerHand.STRAIGHT))
				+ String.format("a flush pays $%,d, a full house pays $%,d, a four of a kind pays $%,d, ",
				getMoneyWon(PokerHand.FLUSH), getMoneyWon(PokerHand.FULL_HOUSE), getMoneyWon(PokerHand.FOUR_OF_A_KIND))
				+ String.format("a straight flush pays $%,d, a five of a kind pays $%,d, a wild royal flush pays $%,d, ",
				getMoneyWon(PokerHand.STRAIGHT_FLUSH), getMoneyWon(PokerHand.FIVE_OF_A_KIND), getMoneyWon(PokerHand.WILD_ROYAL))
				+ String.format("and four deuces pay $%,d.", getMoneyWon(PokerHand.FOUR_DEUCES)));
		output.add(String.format("If you are lucky enough to get a natural royal flush, you will win $%,d!", getMoneyWon(PokerHand.NATURAL_ROYAL)));
		output.add("Best of luck! Pick your first card when you're ready.");
		output.add(generateBoard());
		return output;
	}
	
	@Override
	public LinkedList<String> playNextTurn(String pick)
	{
		LinkedList<String> output = new LinkedList<>();
		
		if (gameStage == 5)
		{
			if (pick.toUpperCase().equals("STOP") && hand != PokerHand.NOTHING) {
				redrawUsed = true;
			}
			else if (pick.toUpperCase().equals("DEAL")) {
				redrawUsed = true;
				gameStage = 0;

				String cardsHeldAsString = "Cards held: ";
				String cardsRedrawingAsString = "Cards being redrawn: ";

				for (int i = 0; i < cardsHeld.length; i++) {
					if (cardsHeld[i])
						cardsHeldAsString += cardsPicked[i].toStringShort() + " ";
					else cardsRedrawingAsString += cardsPicked[i].toStringShort() + " ";
				}
				
				if (cardsHeldAsString.equals("Cards held: ")) { // i.e. we're redrawing everything
						output.add("Redrawing all five cards.");
				}
				else if (cardsRedrawingAsString.equals("Cards being redrawn: ")) { // i.e. there aren't any
					gameStage = 5;
					if (hand == PokerHand.NOTHING) {
						output.add("Holding all five cards would prevent you from winning anything. Release at least one card first.");
						redrawUsed = false;
					}	
					else {
						output.add("All five cards held; ending game.");
					}
					return output;
				}
				else {
					output.add(cardsHeldAsString);
					output.add(cardsRedrawingAsString);
				}
				
				// Find out what stage we should be on now
				for (int i = 0; i < cardsHeld.length; i++) {
					if (!cardsHeld[i])
						break;
					else gameStage++;
				}
				output.add(generateBoard());
				output.add("Select your first card of the redraw when you are ready.");
			}

			// The next two if-else blocks could *probably* be merged together since they do the same thing with two
			// exceptions, but I'm lazy :P --Coug
			else if (pick.toUpperCase().startsWith("HOLD ")) {
				String[] tokens = pick.split("\\s");
				
				// If there are any non-numeric tokens after "HOLD", assume it's just the player talking
				for (int i = 1; i < tokens.length; i++) {
					if (!isNumber(tokens[i]))
						return output;
				}
				
				// Make sure the player's choices correspond to actual cards
				try {
					// The manual deep copy is intentional for safety. If we go into the catch block, we lose this array.
					boolean[] testCardsHeld = deepCopy(cardsHeld);

					for (int i = 1; i < tokens.length; i++)
					{
						testCardsHeld[Integer.parseInt(tokens[i])-1] = true;
					}
					cardsHeld = testCardsHeld;
					output.add(generateHand());
					output.add("You may 'HOLD' other cards, 'RELEASE' cards you no longer wish to hold, or type 'DEAL' to start the redraw.");
				}
				catch (IndexOutOfBoundsException e) {
					output.add("Invalid card(s).");
					return output;
				}
			}
			else if (pick.toUpperCase().startsWith("RELEASE ")) {
				String[] tokens = pick.split("\\s");
				
				// If there are any non-numeric tokens after "RELEASE", assume it's just the player talking
				for (int i = 1; i < tokens.length; i++) {
					if (!isNumber(tokens[i]))
						return output;
				}
				
				// Make sure the player's choices correspond to actual cards
				try {
					// The manual deep copy is intentional for safety. If we go into the catch block, we lose this array.
					boolean[] testCardsHeld = deepCopy(cardsHeld);

					for (int i = 1; i < tokens.length; i++)
					{
						testCardsHeld[Integer.parseInt(tokens[i])-1] = false;
					}
					cardsHeld = testCardsHeld;
					output.add(generateHand());
					output.add("You may 'HOLD' other cards, 'RELEASE' cards you no longer wish to hold, or type 'DEAL' to start the redraw.");
				}
				catch (IndexOutOfBoundsException e) {
					output.add("Invalid card(s).");
					return output;
				}
			}
			return output;
		}
		else //This code only triggers if we still need to draw cards, of course
		{
			String[] tokens = pick.split("\\s");
			for (int i = 0; i < tokens.length; i++)
			{
				if (!isNumber(tokens[i]))
					return output;
				if(!checkValidNumber(tokens[i]))
				{
					output.add("Invalid pick.");
					return output;
				}
			}
			for(String nextPick : tokens)
			{
				//Stop picking cards if we've already got five
				if(gameStage == 5)
					break;
				lastSpace = Integer.parseInt(nextPick)-1;
				pickedSpaces[lastSpace] = true;
				lastPicked = board.get(lastSpace);
				cardsPicked[gameStage] = lastPicked;
				//Autohold deuces, or any card once we've already redrawn
				if(redrawUsed || lastPicked.getRank() == CardRank.DEUCE)
					cardsHeld[gameStage] = true;
				do {
					gameStage++;
				} while (gameStage < 5 && cardsHeld[gameStage]);
				if (gameStage == 5)
					hand = evaluateHand(cardsPicked);
				output.add(String.format("Space %d selected...",lastSpace+1));
				output.add("**" + lastPicked.toString() + "**");
			}
			output.add(generateBoard());
			if (gameStage == 5) {
				if (hand != PokerHand.NATURAL_ROYAL && !redrawUsed) {
					output.add("You may now hold any or all of your five cards by typing HOLD followed by the numeric positions "
							+ "of each card.");
					output.add("For example, type 'HOLD 1' to hold the " + cardsPicked[0] + ".");
					output.add("If you change your mind or make a mistake, type RELEASE followed by the position number of the card " +
							"you would rather redraw, e.g. 'RELEASE 2' to remove any hold on the " + cardsPicked[1] + ".");
					output.add("You may also hold or release more than one card at a time; for example, you may type 'HOLD 3 4 5' to " +
							"hold the " + cardsPicked[2] + ", the " + cardsPicked[3]  + ", and the " + cardsPicked[4] + ".");
					output.add("The cards you do not hold will all be redrawn in the hopes of a better hand.");
					if(hand != PokerHand.NOTHING)
						output.add(String.format("If you like your hand, you may also type 'STOP' to end the game and claim your "+
							"prize of $%,d.", getMoneyWon()));
					output.add("When you are ready, type 'DEAL' to redraw the unheld cards.");
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
		if (gameStage == 5) {
			display.append("(" + hand.toString() + ")");
			if (!redrawUsed && hand != PokerHand.NATURAL_ROYAL)
			{
				display.append("\n              ");
				for (int i = 0; i < cardsPicked.length; i++)
				{
					display.append(i+1);
					display.append(cardsHeld[i] ? "*" : " ");
					display.append(" ");
				}
			}
		}
		display.append("\n```");
		return display.toString();
	}

	String generateHand() {
		StringBuilder display = new StringBuilder();

		display.append("```\n" + "Current hand: ");
		for (int i = 0; i < cardsPicked.length; i++)
		{
			display.append(cardsPicked[i].toStringShort() + " ");
		}
		display.append("\n" + "              ");
		for (int i = 0; i < cardsPicked.length; i++)
		{
			display.append(i+1);

			if (cardsHeld[i])
				display.append ("*");
			else display.append(" ");

			display.append(" ");
		}
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
		return getMoneyWon(hand);
	}

	public int getMoneyWon(PokerHand pokerHand) {
		switch(pokerHand) {
			case NOTHING: return 0;
			case ONE_PAIR: return 20000 * baseMultiplier;
			case TWO_PAIR: return 50000 * baseMultiplier;
			case THREE_OF_A_KIND: return 100000 * baseMultiplier;
			case STRAIGHT: return 150000 * baseMultiplier;
			case FLUSH: return 200000 * baseMultiplier;
			case FULL_HOUSE: return 250000 * baseMultiplier;
			case FOUR_OF_A_KIND: return 500000 * baseMultiplier;
			case STRAIGHT_FLUSH: return 750000 * baseMultiplier;
			case FIVE_OF_A_KIND: return 1000000 * baseMultiplier;
			case WILD_ROYAL: return 2000000 * baseMultiplier;
			case FOUR_DEUCES: return 5000000 * baseMultiplier;
			case NATURAL_ROYAL: return 10000000 * baseMultiplier;
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
		
		byte modeOfRanks = modeOfRanks(rankCount); // That is, how many are there of the most common rank?
		
		switch (modeOfRanks) {
			case 5: return PokerHand.FIVE_OF_A_KIND;
			case 4: return PokerHand.FOUR_OF_A_KIND;
			case 3: if (hasExtraPair(rankCount)) return PokerHand.FULL_HOUSE; // we need to check for a straight before we pay for
			default: break;                                                   // a three of a kind
		}
		
		if (highCardOfStraight != null) return PokerHand.STRAIGHT;
		
		switch (modeOfRanks) {
			case 3: return PokerHand.THREE_OF_A_KIND;
			case 2: 
				if (hasExtraPair(rankCount)) return PokerHand.TWO_PAIR;
				else return PokerHand.ONE_PAIR;
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
		 * Further, a two-pair will always be a natural hand; otherwise it'd be at least a three of a kind.
 		 */
 		byte[] sortedRankCount = rankCount;
 		Arrays.sort(sortedRankCount);
 		return sortedRankCount[rankCount.length - 2] == 2;
	}

	private boolean[] deepCopy (boolean[] arr) { // Here for DRY purposes
		boolean copiedArr[] = new boolean[arr.length];

		for (int i = 0; i < arr.length; i++)
			copiedArr[i] = arr[i];

		return copiedArr;
	}

	@Override
	public String getBotPick() {
		//Bot will redraw automatically in order to get as many deuces as possible (which hold automatically)
		if(gameStage == 5)
			return "DEAL";
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
