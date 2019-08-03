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
import tel.discord.rtab.commands.*;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

public class RaceToABillionBot
{
	final static boolean AUTOSCHEDULE = false;
	public static JDA yayBot;
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
		utilities.addCommands(new HelpCommand(),
							//Basic Game Commands
							new JoinCommand(), new QuitCommand(), new PeekCommand(),
							//Hidden Commands
							new FoldCommand(), new RepelCommand(), new BlammoCommand(), new DefuseCommand(),
							//Info Commands
							new PlayersCommand(), new BoardCommand(), new TotalsCommand(), new NextCommand(),
							new LivesCommand(), new RankCommand(), new TopCommand(), new StatsCommand(), new HistoryCommand(),
							//Betting Commands
							new BetCommand(), new ViewBetsCommand(), new BetRankCommand(), new BetTopCommand(),
							//Mod Commands
							new PingBotCommand(), new StartCommand(), new ResetCommand(),
							new ReconnectCommand(), new ReloadCommand(), new ViewBombsCommand(), new ViewConnectionsCommand(),
							new ShutdownBotCommand(), new AddBotCommand(), new DemoCommand(),
							//Joke Commands
							new MemeCommand(), new LuckyNumberCommand(), new MysteryChanceCommand(),
							//Misc Commands
							new ShopCommand());
		JDABuilder prepareBot = new JDABuilder(AccountType.BOT);
		prepareBot.setToken(token);
		prepareBot.addEventListener(utilities.build());
		prepareBot.addEventListener(waiter);
		yayBot = prepareBot.buildBlocking();
		connectToChannels(AUTOSCHEDULE);
	}
	
	public static void connectToChannels(boolean autoSchedule)
	{
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
					game.add(new GameController(channel,true,true,true,false));
					System.out.println("Main Channel: " + channel.getName() + " ("+ channel.getId() + ")");
				}
				else if(channel.getTopic().startsWith("~ GAME CHANNEL ~"))
				{
					game.add(new GameController(channel,true,false,false,true));
					System.out.println("Game Channel: " + channel.getName() + " ("+ channel.getId() + ")");
				}
				else if(channel.getTopic().startsWith("~ CHALLENGE CHANNEL ~"))
				{
					SuperBotChallenge challengeHandler = new SuperBotChallenge();
					challenge.add(challengeHandler);
					game.add(challengeHandler.initialise(channel,autoSchedule));
					System.out.println("Challenge Channel: " + channel.getName() + " ("+ channel.getId() + ")");
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
