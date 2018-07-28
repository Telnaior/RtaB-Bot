package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class AddBotCommand extends Command {

	public AddBotCommand()
	{
		this.name = "addbot";
		this.help = "adds the specified bot";
		this.hidden = true;
		this.ownerCommand = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		GameController.addBot(Integer.parseInt(event.getArgs()));
	}
}