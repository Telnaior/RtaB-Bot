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
		int playerCount = 4;
		if(!event.getArgs().equals(""))
			playerCount = Integer.parseInt(event.getArgs());
		GameController.channel = event.getChannel();
		for(int i=0; i<playerCount; i++)
		{
			GameController.addRandomBot();
		}
		GameController.startTheGameAlready();
	}
}