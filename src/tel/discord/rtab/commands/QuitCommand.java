package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class QuitCommand extends Command {
	public QuitCommand()
	{
		this.name = "quit";
		this.aliases = new String[]{"leave","out"};
		this.help = "leaves the game, if it hasn't started yet";
	}
	@Override
	protected void execute(CommandEvent event) {
		String name = event.getMember().getEffectiveName();
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				switch(game.removePlayer(event.getChannel(),event.getAuthor()))
				{
				case SUCCESS:
					event.reply(name + " left the game.");
					break;
				case NOTINGAME:
					event.reply(name + " was never in the game.");
					break;
				case GAMEINPROGRESS:
					event.reply("The game cannot be left after it has started.");
					break;
				default:
					event.reply("Unknown error occurred.");
					break;
				}
				//We found the right channel, so
				return;
			}
		}
	}
}