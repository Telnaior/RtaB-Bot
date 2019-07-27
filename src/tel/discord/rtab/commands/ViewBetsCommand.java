package tel.discord.rtab.commands;

import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import tel.discord.rtab.GameController;
import tel.discord.rtab.Player;
import tel.discord.rtab.RaceToABillionBot;

public class ViewBetsCommand extends Command
{
	public ViewBetsCommand()
	{
		this.name = "viewbets";
		this.aliases = new String[]{"viewbet"};
		this.help = "displays the bets currently on the table";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel.equals(event.getChannel()))
			{
				if(game.betManager.getBetCount() > 0)
				{
					List<String> playerNames = new ArrayList<>(4);
					for(Player nextPlayer : game.players)
						playerNames.add(nextPlayer.getName());
					String output = game.betManager.listBets(playerNames).getLeft();
					event.reply(output);
				}
				else
					event.reply("No bets currently placed.");
			}
		}
	}

}
