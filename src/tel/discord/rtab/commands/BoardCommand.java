package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BoardCommand extends Command
{
	public BoardCommand()
	{
		this.name = "board";
		this.aliases = new String[]{"table"};
		this.help = "displays the current board";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		GameController.displayBoardAndStatus();

	}

}
