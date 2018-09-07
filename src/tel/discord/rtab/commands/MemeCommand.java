package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class MemeCommand extends Command
{
	public MemeCommand()
	{
		this.name = "meme";
		this.aliases = new String[]{"jo","realluckynumber"};
		this.help = "https://niceme.me";
		this.hidden = true;
		this.guildOnly = false;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		event.reply("https://niceme.me");
	}
}