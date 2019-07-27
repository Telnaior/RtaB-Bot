package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import tel.discord.rtab.RaceToABillionBot;
import tel.discord.rtab.SuperBotChallenge;

public class ReloadCommand extends Command
{
	public ReloadCommand()
	{
		this.name = "reload";
		this.aliases = new String[] {"loadgames"};
		this.help = "reloads the SBC schedule";
		this.hidden = true;
		this.ownerCommand = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(SuperBotChallenge game : RaceToABillionBot.challenge)
		{
			if(game.channel.equals(event.getChannel()))
			{
				game.loadGames();
				return;
			}
		}
		//If no channel found, actually say something
		event.getChannel().sendMessage("Bad channel.").queue();
	}
}