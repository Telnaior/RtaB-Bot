package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class DefuseCommand extends Command
{
	public DefuseCommand()
	{
		this.name = "defuse";
		this.help = "defuse a space, turning any bomb there into a dud";
		this.hidden = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel.equals(event.getChannel()))
			{
				if(!game.useDefuse(event.getAuthor(),event.getArgs()))
					event.reply("You can't use this right now.");
				return;
			}
		}
		//We aren't in a game channel? Uh...
		event.reply("This is not a game channel.");
	}
}