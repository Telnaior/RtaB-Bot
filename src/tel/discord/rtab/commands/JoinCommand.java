package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class JoinCommand extends Command
{
	public JoinCommand()
	{
		this.name = "join";
		this.aliases = new String[]{"in","enter"};
		this.help = "join the game";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		switch(GameController.addPlayer(event.getChannel(),event.getMember()))
		{
		case JOINED:
			event.reply(event.getMember().getEffectiveName() + " successfully joined the game.");
			break;
		case UPDATED:
			event.reply("Updated in-game name.");
			break;
		case INPROGRESS:
			event.reply("Cannot join game: Game already running.");
			break;
		case ALREADYIN:
			event.reply("Cannot join game: Already joined game.");
			break;
		case WRONGCHANNEL:
			event.reply("Cannot join game: Game running in channel " + GameController.channel.getName());
			break;
		case BADNAME:
			event.reply("Cannot join game: Illegal name");
			break;
		}
	}

}