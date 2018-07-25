package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.examples.command.ShutdownCommand;

public class ShutdownBotCommand extends ShutdownCommand
{
	public ShutdownBotCommand()
	{
		this.hidden = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		GameController.timer.cancel();
		super.execute(event);
	}
}