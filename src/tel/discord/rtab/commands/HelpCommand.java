package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class HelpCommand extends Command
{
    public HelpCommand()
    {
        this.name = "help";
        this.help = "read a basic tutorial for playing";
        this.guildOnly = false;
    }
	@Override
	protected void execute(CommandEvent event)
	{
		event.replyInDm("Race to a Billion is a game where you pick spaces off a board to win cash, "
				+ "while trying not to hit your opponent's bombs.");
		event.replyInDm("Type !join to get into a game, and bring another player with you ot join as well. "
				+ "If you need to leave a game before it starts, type !quit.");
		event.replyInDm("Once the game starts, you'll need to DM the bot with one of the 15 spaces to place a bomb.");
		event.replyInDm("Your opponent will do the same, and then you'll take turns choosing spaces.");
		event.replyInDm("If you pick a space with a bomb hidden behind it, you'll blow up and lose the game. Good luck!");
	}
}