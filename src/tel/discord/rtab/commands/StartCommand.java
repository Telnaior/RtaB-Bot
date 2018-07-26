package tel.discord.rtab.commands;

import java.util.Timer;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class StartCommand extends Command
{
	public StartCommand()
	{
		this.name = "forcestart";
		this.help = "starts the game immediately";
		this.ownerCommand = true;
		this.hidden = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		GameController.timer.cancel();
		GameController.timer = new Timer();
		GameController.startTheGameAlready();
	}
}
