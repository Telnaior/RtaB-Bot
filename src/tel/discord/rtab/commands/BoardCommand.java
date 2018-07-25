package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.enums.GameStatus;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BoardCommand extends Command
{
	public BoardCommand()
	{
		this.name = "board";
		this.aliases = new String[]{"table"};
		this.help = "displays the current board (in-game)";
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
			GameController.displayBoardAndStatus();
		}
	}

}
