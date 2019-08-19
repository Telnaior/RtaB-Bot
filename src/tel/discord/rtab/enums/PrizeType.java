package tel.discord.rtab.enums;

//Created by JerryEris <3

public enum PrizeType {
	//TROPHIES, woo winners
	S1TROPHY (   68000,"a replica of Vash's Season 1 trophy"),
	S2TROPHY (   60000,"a replica of Charles510's Season 2 trophy"),
	S3TROPHY (   54000,"a replica of Archstered's Season 3 trophy"),
	
	//Regular prizes
	DB1		  (   22805,"a DesertBuck"),
	DANGAN    (   11037,"an unfamiliar memory"),
	GOVERNOR  (   26000,"the Governor's favourite"),
	BIGJON    (    1906,"the BigJon special");
    
    private final int prizeValue;
    private final String prizeName;

    PrizeType(int theValue, String theName) {
        this.prizeValue = theValue;
        this.prizeName = theName;
    }

    public String getPrizeName() {
        return prizeName;
    }

    public int getPrizeValue() {
        return prizeValue;
    }
}