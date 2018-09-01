package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class LivesCommand extends Command {
	public LivesCommand()
    {
        this.name = "lives";
		this.aliases = new String[]{"refill"};
        this.help = "see how many lives you have left, and how long until they refill";
        this.guildOnly = false;
    }
	
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				event.reply(game.checkLives(event.getAuthor().getId()));
				//We found the right channel, so
				return;
			}
		}
	}

}
