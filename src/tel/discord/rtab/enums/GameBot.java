package tel.discord.rtab.enums;

import java.util.HashMap;
import java.util.Map;

public enum GameBot
{
	BOT00("-01","Akhiles",null),
	BOT01("-02","BigBOTDawg",null),
	BOT02("-03","Bonehead",null),
	BOT03("-04","BOT Milk?",null),
	BOT04("-05","BOTIN8R",null),
	BOT05("-06","Cathode Kiss",null) { public String getHuman() { return "146519216801972225"; } },
	BOT06("-07","Deathwind",null),
	BOT07("-08","Demonshriek",null),
	BOT08("-09","Devourer",null),
	BOT09("-10","Dharmic Sword",null),
	BOT10("-11","DiamondBOT",null),
	BOT11("-12","Dogstar",null),
	BOT12("-13","Doormat",null),
	BOT13("-14","Dr. BOTward",null),
	BOT14("-15","DustWitch",null),
	BOT15("-16","East BOT",null),
	BOT16("-17","ElectroJag",null),
	BOT17("-18","Evenkill",null),
	BOT18("-19","Fated to Glory",null),
	BOT19("-20","Fishbait",null),
	BOT20("-21","Frank BOTzo",null),
	BOT21("-22","Glitter",null) { public String getHuman() { return "104985399826370560"; } },
	BOT22("-23","Gray Sabot",null),
	BOT23("-24","Hagstomper",null),
	BOT24("-25","Hellefleur",null),
	BOT25("-26","Heretik",null),
	BOT26("-27","HexaBOTic",null),
	BOT27("-28","Hotfoot",null),
	BOT28("-29","Hotspur",null),
	BOT29("-30","Ironbreath",null),
	BOT30("-31","Irrelevant Smoke",null),
	BOT31("-32","Jetsam",null),
	BOT32("-33","Jett BOT",null),
	BOT33("-34","Jimmy BOT",null),
	BOT34("-35","Kidney BOT",null),
	BOT35("-36","King Snake",null),
	BOT36("-37","Mameluke",null),
	BOT37("-38","Missing BOT",null),
	BOT38("-39","Mohican",null),
	BOT39("-40","Mojo Savage",null),
	BOT40("-41","Mongo BOT",null),
	BOT41("-42","Neon Blossom",null),
	BOT42("-43","Newguns",null),
	BOT43("-44","No-Dachi",null),
	BOT44("-45","Nova-9",null),
	BOT45("-46","Orphan KazBOT",null),
	BOT46("-47","Perilous",null) { public String getHuman() { return "346189542002393089"; } },
	BOT47("-48","Punch",null),
	BOT48("-49","QIX BOT",null),
	BOT49("-50","Ragbinder",null),
	BOT50("-51","Rampant",null) { public String getHuman() { return "317079755814076418"; } },
	BOT51("-52","Rated BOT",null),
	BOT52("-53","Red Ghost",null),
	BOT53("-54","Retch",null),
	BOT54("-55","ShazBOT",null),
	BOT55("-56","Shiver",null),
	BOT56("-57","Simrionic",null),
	BOT57("-58","Skeet BOT",null),
	BOT58("-59","Skulker",null),
	BOT59("-60","Slicer",null),
	BOT60("-61","Sne/\\kBOT",null),
	BOT61("-62","Snow LeoBOT",null),
	BOT62("-63","So Dark",null),
	BOT63("-64","Sorrow",null),
	BOT64("-65","SymBOT",null),
	BOT65("-66","Terrapin",null),
	BOT66("-67","Terrible BOT",null),
	BOT67("-68","The Golden",null),
	BOT68("-69","TickTock",null),
	BOT69("-70","Torus",null),
	BOT70("-71","Trail of Rust",null),
	BOT71("-72","Troglodyte",null),
	BOT72("-73","Twitch BOT",null),
	BOT73("-74","UberBOT",null),
	BOT74("-75","Vanguard",null) { public String getHuman() { return "276196470317907970"; } },
	BOT75("-76","Velomancer",null),
	BOT76("-77","Widowmaker",null),
	BOT77("-78","Wirehead",null),
	BOT78("-79","WrongWay",null),
	BOT79("-80","Zigzag",null);
	
	private static class Holder
	{
		static Map<String, GameBot> ID_MAP = new HashMap<>();
	}
	
	public String botID;
	public String name;
	String humanID;
	GameBot(String botID, String name, String humanID)
	{
		this.botID = botID;
		this.name = name;
		this.humanID = humanID;
		//If they're human, add them to the map to be found later
		if(humanID != null)
			Holder.ID_MAP.put(humanID, this);
	}
	public GameBot next()
	{
        return values()[(ordinal() + 1) % values().length];
    }
	public String getHuman()
	{
		return humanID;
	}
	public static int getBotFromHuman(String id)
	{
		//Ask the map if there's a bot with that id
		GameBot foundBot = Holder.ID_MAP.get(id);
		//If we found one, return its number, otherwise -1 failure
		if(foundBot != null)
			return foundBot.ordinal();
		else
			return -1;
	}
}
