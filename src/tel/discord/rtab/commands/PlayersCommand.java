package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PlayersCommand extends Command {
    public PlayersCommand()
    {
        this.name = "players";
        this.help = "get a list of the players signed up for the current game";
    }
    
	@Override
	protected void execute(CommandEvent event) {
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				if(game.playersJoined == 0)
					event.reply("No one currently in game.");
				else
					event.reply(game.listPlayers(false));
				//We found the right channel, so
				return;
			}
		}
	}

}
