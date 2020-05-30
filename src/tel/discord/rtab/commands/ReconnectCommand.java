package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;
import tel.discord.rtab.SuperBotChallenge;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ReconnectCommand extends Command
{
	public ReconnectCommand()
	{
		this.name = "reconnect";
		this.help = "reconnects the bot to its game channels";
		this.hidden = true;
		this.requiredRole = "Mod";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			game.timer.purge();
			game.timer.shutdownNow();
		}
		RaceToABillionBot.game.clear();
		
		for(SuperBotChallenge challenge : RaceToABillionBot.challenge)
		{
			challenge.timer.purge();
			challenge.timer.shutdownNow();
		}
		RaceToABillionBot.challenge.clear();
		
		RaceToABillionBot.connectToChannels();
	}
}