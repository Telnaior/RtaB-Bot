package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import tel.discord.rtab.commands.AddBotCommand;
import tel.discord.rtab.commands.BoardCommand;
import tel.discord.rtab.commands.DemoCommand;
import tel.discord.rtab.commands.HelpCommand;
import tel.discord.rtab.commands.JoinCommand;
import tel.discord.rtab.commands.LivesCommand;
import tel.discord.rtab.commands.LuckyNumberCommand;
import tel.discord.rtab.commands.MemeCommand;
import tel.discord.rtab.commands.PingBotCommand;
import tel.discord.rtab.commands.PlayersCommand;
import tel.discord.rtab.commands.QuitCommand;
import tel.discord.rtab.commands.RankCommand;
import tel.discord.rtab.commands.ResetCommand;
import tel.discord.rtab.commands.ShutdownBotCommand;
import tel.discord.rtab.commands.StartCommand;
import tel.discord.rtab.commands.StatsCommand;
import tel.discord.rtab.commands.TopCommand;
import tel.discord.rtab.commands.TotalsCommand;
import tel.discord.rtab.commands.ViewBombsCommand;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

public class RaceToABillionBot
{
	public static ArrayList<GameController> game = new ArrayList<>(3);
	public static ArrayList<SuperBotChallenge> challenge = new ArrayList<>(1);
	
	public static void main(String[] args) throws LoginException, InterruptedException, IOException
	{
		List<String> list = Files.readAllLines(Paths.get("config.txt"));
		String token = list.get(0);
		String owner = list.get(1);
		EventWaiter waiter = new EventWaiter();
		GameController.waiter = waiter;
		CommandClientBuilder utilities = new CommandClientBuilder();
		utilities.setOwnerId(owner);
		utilities.setPrefix("!");
		utilities.setHelpWord("commands");
		utilities.addCommand(new HelpCommand());
		utilities.addCommand(new JoinCommand());
		utilities.addCommand(new QuitCommand());
		utilities.addCommand(new PlayersCommand());
		utilities.addCommand(new BoardCommand());
		utilities.addCommand(new TotalsCommand());
		utilities.addCommand(new LivesCommand());
		utilities.addCommand(new RankCommand());
		utilities.addCommand(new TopCommand());
		utilities.addCommand(new StatsCommand());
		utilities.addCommand(new PingBotCommand());
		utilities.addCommand(new StartCommand());
		utilities.addCommand(new ResetCommand());
		utilities.addCommand(new ViewBombsCommand());
		utilities.addCommand(new ShutdownBotCommand());
		utilities.addCommand(new AddBotCommand());
		utilities.addCommand(new DemoCommand());
		utilities.addCommand(new MemeCommand());
		utilities.addCommand(new LuckyNumberCommand());
		JDABuilder prepareBot = new JDABuilder(AccountType.BOT);
		prepareBot.setToken(token);
		prepareBot.addEventListener(utilities.build());
		prepareBot.addEventListener(waiter);
		JDA yayBot = prepareBot.buildBlocking();
		//Get all the guilds we're in
		List<Guild> guildList = yayBot.getGuilds();
		//And for each guild, get the list of channels in it
		for(Guild guild : guildList)
		{
			List<TextChannel> channelList = guild.getTextChannels();
			//And for each channel...
			for(TextChannel channel : channelList)
			{
				//If it's a designated game channel, make a controller here!
				if(channel.getTopic().startsWith("~ MAIN CHANNEL ~"))
				{
					game.add(new GameController(channel,true,true,false,1));
					System.out.println("Main Channel: " + channel.getName() + " ("+ channel.getId() + ")");
				}
				else if(channel.getTopic().startsWith("~ GAME CHANNEL ~"))
				{
					game.add(new GameController(channel,false,true,false,1));
					System.out.println("Game Channel: " + channel.getName() + " ("+ channel.getId() + ")");
				}
				else if(channel.getTopic().startsWith("~ CHALLENGE CHANNEL ~"))
				{
					int playersLeft = Integer.parseInt(channel.getTopic().substring(22,24));
					int multiplier = 1 + (80 - playersLeft) / 8;
					SuperBotChallenge challengeHandler = new SuperBotChallenge();
					challenge.add(challengeHandler);
					game.add(challengeHandler.initialise(channel,multiplier));
				}
				else if(channel.getTopic().startsWith("~ RESULT CHANNEL ~"))
				{
					String linkChannel = channel.getTopic().substring(19);
					for(GameController gameChannel : game)
					{
						if(linkChannel.equals(gameChannel.channel.getName()) 
								&& channel.getGuild() == gameChannel.channel.getGuild())
						{
							System.out.println("Result Channel: " + channel.getName() + " : " + linkChannel);
							gameChannel.setResultChannel(channel);
							//We found the right channel, so
							break;
						}
					}
				}
			}
		}
	}
}
