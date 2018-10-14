package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class AddBotCommand extends Command {

	public AddBotCommand()
	{
		this.name = "addbot";
		this.help = "adds the specified bot";
		this.hidden = true;
		this.ownerCommand = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				if(event.getArgs().equals(""))
				{
					game.addRandomBot();
				}
				//Let's have some fun
				else if(event.getArgs().equals("all"))
				{
					for(int i=0; i<80; i++)
						game.addBot(i);
				}
				else
				{
					game.addBot(Integer.parseInt(event.getArgs()));
				}
				//We found the right channel, so
				return;
			}
		}
		
	}
}