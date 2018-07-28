package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.enums.GameStatus;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class TotalsCommand extends Command
{
	public TotalsCommand()
	{
		this.name = "totals";
		this.help = "displays the total scores of the in-game players";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		if(GameController.gameStatus == GameStatus.SIGNUPS_OPEN)
		{
			//No board to display if the game isn't running!
			event.reply("No game currently running.");
		}
		else
		{	
			GameController.displayBoardAndStatus(false, true);
		}
	}

}
