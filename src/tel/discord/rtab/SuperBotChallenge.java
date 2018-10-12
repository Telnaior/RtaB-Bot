package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.TextChannel;

public class SuperBotChallenge
{
	GameController gameHandler;
	public TextChannel channel;
	public ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);
	
	public GameController initialise(TextChannel channelID, int multiplier, boolean autoSchedule)
	{
		channel = channelID;
		gameHandler = new GameController(channel,false,false,false,true,multiplier);
		if(autoSchedule)
			loadGames();
		return gameHandler;
	}
	public void loadGames()
	{
		timer.purge();
		List<String> list = null;
		try
		{
			list = Files.readAllLines(Paths.get("schedule"+channel.getId()+".csv"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		ListIterator<String> schedule = list.listIterator();
		int totalDelay = 0;
		int totalGames = 0;
		while(schedule.hasNext())
		{
			String[] record = schedule.next().split(":");
			String[] players = record[0].split("	");
			totalDelay += Integer.parseInt(record[1]);
			timer.schedule(() -> 
			{
				for(String next : players)
					gameHandler.addBot(Integer.parseInt(next));
				channel.sendMessage("Next game starting in five minutes:").queue();
				if(record.length > 2)
					channel.sendMessage(record[2]).queue();
				channel.sendMessage(gameHandler.listPlayers(false)).queue();
			},totalDelay-5,TimeUnit.MINUTES);
			timer.schedule(() -> gameHandler.startTheGameAlready(),totalDelay,TimeUnit.MINUTES);
			totalGames ++;
		}
		channel.sendMessage(totalGames + " games loaded.").queue();
	}
}