package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ShopCommand extends Command
{
	public ShopCommand()
	{
		this.name = "shop";
		this.help = "See a list of rewards you can earn for supporting Race to a Billion";
		this.guildOnly = false;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		event.reply("Support Race to a Billion and earn rewards!");
		event.reply("https://i.imgur.com/gcsGHev.png");
	}
}