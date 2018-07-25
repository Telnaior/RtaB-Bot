package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class Help2Command extends Command
{
    public Help2Command()
    {
        this.name = "help2";
        this.help = "read about the spaces on the gameboard";
        this.hidden = true;
        this.guildOnly = false;
    }
	@Override
	protected void execute(CommandEvent event)
	{
		event.replyInDm("Most spaces on the gameboard are cash. If you find one, the money is added to your total bank.");
		event.replyInDm("Other spaces hide boosters - these apply a multiplier to all the cash you win.");
		event.replyInDm("Boosters carry over between games, but will (usually) be reset when you hit a bomb.");
		event.replyInDm("The maximum booster possible is 999%, and the minimum is 10%.");
		event.replyInDm("There are also minigames on the board. If you find one, you must win the game to be able to play it.");
		event.replyInDm("These give you the chance to win some extra cash, so try your best.");
		event.replyInDm("Lastly, there are events hidden on the board that can trigger all sorts of special things.");
		event.replyInDm("Events are unpredictable and could help you or hurt you, so good luck!");
	}
}