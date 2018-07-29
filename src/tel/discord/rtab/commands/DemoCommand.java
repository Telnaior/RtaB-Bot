package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class DemoCommand extends Command {

	public DemoCommand()
	{
		this.name = "demo";
		this.help = "starts a game with four bots";
		this.hidden = true;
		this.ownerCommand = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		GameController.channel = event.getChannel();
		for(int i=0; i<4; i++)
		{
			GameController.addRandomBot();
		}
		GameController.startTheGameAlready();
	}
}