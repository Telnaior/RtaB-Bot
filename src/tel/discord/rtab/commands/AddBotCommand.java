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
		//Let's have some fun
		if(event.getArgs().equals("all"))
		{
			for(int i=0; i<80; i++)
				GameController.addBot(i);
		}
		else
		{
			GameController.addBot(Integer.parseInt(event.getArgs()));
		}
	}
}