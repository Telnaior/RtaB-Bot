package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

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
		if(GameController.playersJoined == 0)
			event.reply("No one currently in game.");
		else
			GameController.listPlayers(false);
	}

}
