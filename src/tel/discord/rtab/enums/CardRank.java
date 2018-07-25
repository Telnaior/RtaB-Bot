package tel.discord.rtab.enums;

// Swiped and slightly modified from another project of mine I did for university. The order of the ranks matter here. --Coug
public enum CardRank {
    DEUCE("Deuce","2"),
    THREE("Three","3"),
    FOUR("Four","4"),
    FIVE("Five","5"),
    SIX("Six","6"),
    SEVEN("Seven","7"),
    EIGHT("Eight","8"),
    NINE("Nine","9"),
    TEN("Ten","10"),
    JACK("Jack","J"),
    QUEEN("Queen","Q"),
    KING("King","K"),
    ACE("Ace","A");
    
    private final String name, symbol;

    CardRank(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }
}