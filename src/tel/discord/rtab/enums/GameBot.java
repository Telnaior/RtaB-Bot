package tel.discord.rtab.enums;

public enum GameBot
{
	BOT00("-01","Akhiles"),
	BOT01("-02","BigBOTDawg"),
	BOT02("-03","Bonehead"),
	BOT03("-04","BOT Milk?"),
	BOT04("-05","BOTIN8R"),
	BOT05("-06","Cathode Kiss") { public String getHuman() { return "146519216801972225"; } },
	BOT06("-07","Deathwind"),
	BOT07("-08","Demonshriek"),
	BOT08("-09","Devourer"),
	BOT09("-10","Dharmic Sword"),
	BOT10("-11","DiamondBOT"),
	BOT11("-12","Dogstar"),
	BOT12("-13","Doormat"),
	BOT13("-14","Dr. BOTward"),
	BOT14("-15","DustWitch"),
	BOT15("-16","East BOT"),
	BOT16("-17","ElectroJag"),
	BOT17("-18","Evenkill"),
	BOT18("-19","Fated to Glory"),
	BOT19("-20","Fishbait"),
	BOT20("-21","Frank BOTzo"),
	BOT21("-22","Glitter") { public String getHuman() { return "104985399826370560"; } },
	BOT22("-23","Gray Sabot"),
	BOT23("-24","Hagstomper"),
	BOT24("-25","Hellefleur"),
	BOT25("-26","Heretik"),
	BOT26("-27","HexaBOTic"),
	BOT27("-28","Hotfoot"),
	BOT28("-29","Hotspur"),
	BOT29("-30","Ironbreath"),
	BOT30("-31","Irrelevant Smoke"),
	BOT31("-32","Jetsam"),
	BOT32("-33","Jett BOT"),
	BOT33("-34","Jimmy BOT"),
	BOT34("-35","Kidney BOT"),
	BOT35("-36","King Snake"),
	BOT36("-37","Mameluke"),
	BOT37("-38","Missing BOT"),
	BOT38("-39","Mohican"),
	BOT39("-40","Mojo Savage"),
	BOT40("-41","Mongo BOT"),
	BOT41("-42","Neon Blossom"),
	BOT42("-43","Newguns"),
	BOT43("-44","No-Dachi"),
	BOT44("-45","Nova-9"),
	BOT45("-46","Orphan KazBOT"),
	BOT46("-47","Perilous") { public String getHuman() { return "346189542002393089"; } },
	BOT47("-48","Punch"),
	BOT48("-49","QIX BOT"),
	BOT49("-50","Ragbinder"),
	BOT50("-51","Rampant") { public String getHuman() { return "317079755814076418"; } },
	BOT51("-52","Rated BOT"),
	BOT52("-53","Red Ghost"),
	BOT53("-54","Retch"),
	BOT54("-55","ShazBOT"),
	BOT55("-56","Shiver"),
	BOT56("-57","Simrionic"),
	BOT57("-58","Skeet BOT"),
	BOT58("-59","Skulker"),
	BOT59("-60","Slicer"),
	BOT60("-61","Sne/\\kBOT"),
	BOT61("-62","Snow LeoBOT"),
	BOT62("-63","So Dark"),
	BOT63("-64","Sorrow"),
	BOT64("-65","SymBOT"),
	BOT65("-66","Terrapin"),
	BOT66("-67","Terrible BOT"),
	BOT67("-68","The Golden"),
	BOT68("-69","TickTock"),
	BOT69("-70","Torus"),
	BOT70("-71","Trail of Rust"),
	BOT71("-72","Troglodyte"),
	BOT72("-73","Twitch BOT"),
	BOT73("-74","UberBOT"),
	BOT74("-75","Vanguard") { public String getHuman() { return "276196470317907970"; } },
	BOT75("-76","Velomancer"),
	BOT76("-77","Widowmaker"),
	BOT77("-78","Wirehead"),
	BOT78("-79","WrongWay"),
	BOT79("-80","Zigzag");
	
	public String botID;
	public String name; 
	GameBot(String idNo, String botName)
	{
		botID = idNo;
		name = botName;
	}
	public GameBot next()
	{
        return values()[(ordinal() + 1) % values().length];
    }
	public String getHuman()
	{
		return null;
	}
}
