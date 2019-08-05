package tel.discord.rtab.enums;

public enum PrizeType {
	//TROPHIES, woo winners
	S1TROPHY (   68000,"a replica of Vash's Season 1 trophy"),
	S2TROPHY (   60000,"a replica of Charles510's Season 2 trophy"),
	S3TROPHY (   54000,"a replica of Archstered's Season 3 trophy"),
	
	//Regular prizes
	DB1		    (   22805,"a DesertBuck"),
	DANGAN    (   11037,"an unfamiliar memory"),
	GOVERNOR  (   26000,"the Governor's favourite"),
	BIGJON    (    1906,"the BigJon special");
    
    private final int theValue;
    private final String theName;

    PrizeType(int theValue, String theName) {
        this.value = theValue;
        this.name = theName;
    }

    public String getPrizeName() {
        return name;
    }

    public int getPrizeValue() {
        return value;
    }
}
