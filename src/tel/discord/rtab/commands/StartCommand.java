package tel.discord.rtab.commands;

import java.util.Timer;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class StartCommand extends Command
{
	public StartCommand()
	{
		this.name = "forcestart";
		this.help = "starts the game immediately";
		this.hidden = true;
		this.requiredRole = "Mod";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				game.timer.cancel();
				game.timer = new Timer();
				game.startTheGameAlready();
				//We found the right channel, so
				return;
			}
		}
	}
}
