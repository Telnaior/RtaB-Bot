package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class FoldCommand extends Command
{
	public FoldCommand()
	{
		this.name = "fold";
		this.help = "voluntarily exit the round, keeping your booster and minigames";
		this.hidden = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel.equals(event.getChannel()))
			{
				if(!game.useFold(event.getAuthor()))
					event.reply("You can't fold right now.");
				return;
			}
		}
		//We aren't in a game channel? Uh...
		event.reply("This is not a game channel.");
	}
}