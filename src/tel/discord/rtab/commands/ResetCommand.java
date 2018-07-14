package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ResetCommand extends Command
{
	public ResetCommand()
	{
		this.name = "reset";
		this.help = "resets the game state, in case something gets bugged";
		this.ownerCommand = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		GameController.reset();
	}
}