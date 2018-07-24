package tel.discord.rtab.enums;

public enum PokerHand {
    NOTHING, // couldn't think of any other good name for a losing hand
    THREE_OF_A_KIND,
    STRAIGHT, // A-2-3-4-5 and 10-J-Q-K-A both count, even though aces are normally high
    FLUSH,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    STRAIGHT_FLUSH, // A-2-3-4-5 counts, but not 10-J-Q-K-A -- that's one of the royal hands
    FIVE_OF_A_KIND, // Only possible with deuces
    WILD_ROYAL,
    FOUR_DEUCES,
    NATURAL_ROYAL
}