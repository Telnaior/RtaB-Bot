package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.TextChannel;

public class SuperBotChallenge
{
	GameController gameHandler;
	TextChannel channel;
	ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
	
	public GameController initialise(TextChannel channelID, int multiplier)
	{
		channel = channelID;
		gameHandler = new GameController(channel,false,false,true,multiplier);
		loadGames();
		return gameHandler;
	}
	public void loadGames()
	{
		List<String> list = null;
		try
		{
			list = Files.readAllLines(Paths.get("schedule"+channel+".csv"));
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
			String[] players = record[0].split("  ");
			totalDelay += Integer.parseInt(record[1]);
			timer.schedule(() -> 
			{
				for(String next : players)
					gameHandler.addBot(Integer.parseInt(next));
				channel.sendMessage("Next game starting in five minutes.").queue();
				channel.sendMessage(gameHandler.listPlayers(false)).queue();
			},totalDelay-5,TimeUnit.MINUTES);
			timer.schedule(() -> gameHandler.startTheGameAlready(),totalDelay,TimeUnit.MINUTES);
			totalGames ++;
		}
		channel.sendMessage(totalGames + " games loaded.").queue();
	}
}