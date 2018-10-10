package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.dv8tion.jda.core.entities.Member;

public class Bettor {
	static final int STARTING_MONEY = 10000;
	String uID;
	String name;
	int funds;
	int prestige;
	int betAmount;
	String champion;

	public Bettor(Member bettor, String channelID) {
		name = bettor.getEffectiveName();
		uID = bettor.getUser().getId();
		//Set default value
		funds = STARTING_MONEY;
		prestige = 0;
		//Now check if they're already in, and correct their funds if so
		try
		{
			List<String> list = Files.readAllLines(Paths.get("bettors"+channelID+".csv"));
			String[] record;
			for(int i=0; i<list.size(); i++)
			{
				/*
				 * record format:
				 * record[0] = uID
				 * record[1] = name
				 * record[2] = funds
				 * record[3] = prestige
				 */
				record = list.get(i).split("#");
				if(record[0].equals(uID))
				{
					funds = Integer.parseInt(record[2]);
					prestige = Integer.parseInt(record[3]);
					break;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setBet(int amount, String betTarget) {
		champion = betTarget;
		betAmount = amount;
		funds -= amount;
		//Add on overdraft penalty if they've done it
		if(funds < 0)
			funds -= Math.min(amount, Math.abs(funds));
		//SURELY this will never happen (but we're prepared if it does)
		if(funds < -1000000000)
			funds = -1000000000;
	}
	
}
