package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class StartCommand extends Command
{
	public StartCommand()
	{
		this.name = "start";
		this.help = "start the game (joining it if not already in)";
	}
	@Override
	protected void execute(CommandEvent event)
	{

		switch(GameController.addPlayer(event.getChannel(),event.getMember()))
		{
		case JOINED:
		case UPDATED:
		case ALREADYIN:
			if(GameController.playersJoined >= 2)
			{
				event.reply("Starting game...");
				GameController.startTheGameAlready();
			}
			else
				event.reply("Still need another player.");
			break;
		case WRONGCHANNEL:
			event.reply("Game running in channel " + GameController.channel.getName());
		default: //Includes game already in progress
			break;
		}
	}
}
