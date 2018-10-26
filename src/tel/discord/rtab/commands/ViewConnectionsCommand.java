package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ViewConnectionsCommand extends Command {
	public ViewConnectionsCommand()
	{
		this.name = "viewconnections";
		this.help = "See the list of channels the bot is currently connected to";
		this.hidden = true;
		this.ownerCommand = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			System.out.println(String.format("%1$s (%2$s)", game.channel.getName(), game.channel.getId()));
		}
	}
}
