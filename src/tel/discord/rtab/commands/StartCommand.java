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
		this.help = "start the game (joining it if not already in)";
		this.ownerCommand = true;
		this.hidden = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{

		switch(GameController.addPlayer(event.getChannel(),event.getMember()))
		{
		case JOINED:
		case UPDATED:
		case ALREADYIN:
			GameController.timer.cancel();
			GameController.timer = new Timer();
			GameController.startTheGameAlready();
			break;
		case WRONGCHANNEL:
			event.reply("Game running in channel " + GameController.channel.getName());
		default: //Includes game already in progress
			break;
		}
	}
}
