package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class HelpCommand extends Command
{
    public HelpCommand()
    {
        this.name = "help";
        this.help = "learn about the rules of the game";
        this.guildOnly = false;
    }
	@Override
	protected void execute(CommandEvent event)
	{
		String name = event.getArgs();
		switch (name)
		{
		case "":
			event.replyInDm("Race to a Billion is a game where you pick spaces off a board to win cash, "
					+ "while trying not to hit your opponents' bombs.");
			event.replyInDm("Type !join to get into a game. If you need to leave a game before it starts, type !quit.");
			event.replyInDm("Once the game starts, you'll need to DM the bot with a space to place a bomb on.");
			event.replyInDm("Your opponents will do the same, and then you'll take turns choosing spaces.");
			event.replyInDm("If you pick a space with a bomb hidden behind it, you'll blow up and lose the game.");
			event.replyInDm("To hear more about what you might find on the gameboard, type '!help 2'.");
			break;
		case "2":
			event.replyInDm("Most spaces on the gameboard are cash. If you find one, the money is added to your total bank.");
			event.replyInDm("Other spaces hide boosters - these apply a multiplier to all the cash you win.");
			event.replyInDm("Boosters carry over between games, but will (usually) be reset when you hit a bomb.");
			event.replyInDm("The maximum booster possible is 999%, and the minimum is 10%.");
			event.replyInDm("There are also minigames on the board. If you find one, you must win the game to be able to play it.");
			event.replyInDm("These give you the chance to win some extra cash, so try your best.");
			event.replyInDm("Lastly, there are events hidden on the board that can trigger all sorts of special things.");
			event.replyInDm("Events are unpredictable and could help you or hurt you, so good luck!");
			break;
		}
	}
}