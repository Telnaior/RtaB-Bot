package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.TextChannel;
import tel.discord.rtab.enums.GameStatus;

public class SuperBotChallenge
{
	GameController gameHandler;
	public TextChannel channel;
	public ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(1);
	
	public GameController initialise(TextChannel channelID, boolean autoSchedule)
	{
		channel = channelID;
		gameHandler = new GameController(channel,false,false,0,true);
		if(autoSchedule)
			loadGames();
		return gameHandler;
	}
	public void loadGames()
	{
		//Format is "~ CHALLENGE CHANNEL ~ XX Players Remain"
		int playersLeft = Integer.parseInt(channel.getTopic().substring(22,24));
		int multiplier = getMultiplier(playersLeft);
		gameHandler.setMultiplier(multiplier);
		timer.shutdownNow();
		timer = new ScheduledThreadPoolExecutor(1);
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
				if(gameHandler.gameStatus != GameStatus.SEASON_OVER)
				{
					for(String next : players)
						gameHandler.addBot(Integer.parseInt(next),true);
					channel.sendMessage("Next game starting in five minutes:").queue();
					if(record.length > 2)
						channel.sendMessage(record[2]).queue();
					channel.sendMessage(gameHandler.listPlayers(false)).queue();
					gameHandler.runPingList();
				}
				else
				{
					timer.purge();
					timer.shutdownNow();
				}
			},totalDelay-5,TimeUnit.MINUTES);
			timer.schedule(() -> gameHandler.startTheGameAlready(),totalDelay,TimeUnit.MINUTES);
			totalGames ++;
		}
		if(totalGames > 0)
			channel.sendMessage(totalGames + " games loaded.").queue();
	}
	int getMultiplier(int playersLeft)
	{
		int multiplier;
		switch(playersLeft)
		{
		case 4:
			multiplier = 10;
			break;
		case 8:
			multiplier = 9;
			break;
		case 12:
			multiplier = 8;
			break;
		case 16:
		case 20:
			multiplier = 7;
			break;
		case 24:
		case 28:
			multiplier = 6;
			break;
		case 32:
		case 36:
			multiplier = 5;
			break;
		case 40:
		case 44:
			multiplier = 4;
			break;
		case 48:
		case 52:
		case 56:
			multiplier = 3;
			break;
		case 60:
		case 64:
		case 68:
			multiplier = 2;
			break;
		case 72:
		case 76:
		case 80:
			multiplier = 1;
			break;
		default:
			multiplier = 1;
			channel.sendMessage("Multiplier not initialised properly!").queue();
		}
		return multiplier;
	}
}