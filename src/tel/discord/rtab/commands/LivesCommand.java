package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
				try
				{
					List<String> list = Files.readAllLines(Paths.get("scores"+event.getChannel().getId()+".csv"));
					//If no name given, check it for themselves
					int index;
					if(event.getArgs() == "")
						index = GameController.findUserInList(list,event.getAuthor().getId(),false);
					//Otherwise check it for the player named
					else
					{
						index = GameController.findUserInList(list,event.getArgs(),true);
					}
					//Then pass off to the actual controller if they're an actual user
					if(index < 0 || index >= list.size())
						event.reply("User not found.");
					else
						event.reply(game.checkLives(index));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				//We found the right channel, so
				return;
			}
		}
	}
}
