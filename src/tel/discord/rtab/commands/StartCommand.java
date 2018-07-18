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
			event.reply("Starting game...");
			GameController.startTheGameAlready();
			break;
		case WRONGCHANNEL:
			event.reply("Game running in channel " + GameController.channel.getName());
		default:
			break;
		}
	}
}
