package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.Player;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ViewBombsCommand extends Command {
	public ViewBombsCommand()
	{
		this.name = "viewbombs";
		this.help = "See who placed their bomb where (can only use when not in game)";
		this.hidden = true;
		this.requiredRole = "Mod";
	}
	@Override
	protected void execute(CommandEvent event) {
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				if(game.findPlayerInGame(event.getAuthor().getId()) != -1)
					event.reply("You can't view bombs for a game you're in!");
				else
					for(Player nextPlayer : game.players)
					{
						event.replyInDm(nextPlayer.printBombs());
					}
				//We found the right channel, so
				return;
			}
		}
	}

}
