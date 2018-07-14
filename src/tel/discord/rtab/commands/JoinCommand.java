package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class JoinCommand extends Command
{
	public JoinCommand()
	{
		this.name = "join";
		this.help = "join (or start) game";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		switch(GameController.addPlayer(event.getChannel(),event.getAuthor()))
		{
		case JOINED1:
		case JOINED2:
			break;
		case GAMEFULL:
			event.reply("Cannot join game: Game already full.");
			break;
		case ALREADYIN:
			event.reply("Cannot join game: Already joined game.");
			break;
		default:
			event.reply("Unknown error occurred.");
			break;
		}
	}

}