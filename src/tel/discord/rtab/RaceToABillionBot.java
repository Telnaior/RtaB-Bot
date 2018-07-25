package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import tel.discord.rtab.commands.BoardCommand;
import tel.discord.rtab.commands.Help2Command;
import tel.discord.rtab.commands.HelpCommand;
import tel.discord.rtab.commands.JoinCommand;
import tel.discord.rtab.commands.PingBotCommand;
import tel.discord.rtab.commands.PlayersCommand;
import tel.discord.rtab.commands.QuitCommand;
import tel.discord.rtab.commands.RankCommand;
import tel.discord.rtab.commands.ResetCommand;
import tel.discord.rtab.commands.ShutdownBotCommand;
import tel.discord.rtab.commands.StartCommand;
import tel.discord.rtab.commands.TopCommand;
import tel.discord.rtab.commands.TotalsCommand;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

public class RaceToABillionBot
{
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
		utilities.addCommand(new Help2Command());
		utilities.addCommand(new JoinCommand());
		utilities.addCommand(new StartCommand());
		utilities.addCommand(new QuitCommand());
		utilities.addCommand(new PlayersCommand());
		utilities.addCommand(new BoardCommand());
		utilities.addCommand(new TotalsCommand());
		utilities.addCommand(new RankCommand());
		utilities.addCommand(new TopCommand());
		utilities.addCommand(new PingBotCommand());
		utilities.addCommand(new ResetCommand());
		utilities.addCommand(new ShutdownBotCommand());
		JDABuilder yayBot = new JDABuilder(AccountType.BOT);
		yayBot.setToken(token);
		yayBot.addEventListener(utilities.build());
		yayBot.addEventListener(waiter);
		yayBot.buildBlocking();
	}
}