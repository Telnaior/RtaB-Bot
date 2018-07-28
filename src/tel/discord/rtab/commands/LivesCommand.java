package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class LivesCommand extends Command {
	public LivesCommand()
    {
        this.name = "lives";
        this.help = "see how many lives you have left, and how long until they refill";
        this.guildOnly = false;
    }
	
	@Override
	protected void execute(CommandEvent event)
	{
		event.reply(GameController.checkLives(event.getAuthor().getId()));
	}

}
