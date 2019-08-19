package tel.discord.rtab.enums;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public enum Jackpots
{
	BOWSER		(0),
	SUPERCASH	(10_000_000),
	DIGITAL		(25_000_000);
	
	public int resetValue;
	Jackpots(int base)
	{
		resetValue = base;
	}
	
	public int getJackpot(String channelID)
	{
		try
		{
			List<String> list = Files.readAllLines(Paths.get("jackpots"+channelID+".csv"));
			int jackpot = Integer.parseInt(list.get(this.ordinal()));
			return Math.max(jackpot, resetValue);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return resetValue;
		}
	}
	
	public void setJackpot(String channelID, int value)
	{
		try
		{
			List<String> list = Files.readAllLines(Paths.get("jackpots"+channelID+".csv"));
			Path file = Paths.get("jackpots"+channelID+".csv");
			Path oldFile = Files.move(file, file.resolveSibling("jackpots"+channelID+"old.csv"));
			list.set(this.ordinal(),Integer.toString(value));
			Files.write(file, list);
			Files.delete(oldFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void resetJackpot(String channelID)
	{
		setJackpot(channelID, resetValue);
	}
}
