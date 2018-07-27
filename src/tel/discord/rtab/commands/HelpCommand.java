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
		StringBuilder helpList = new StringBuilder();
		switch (name)
		{
		case "":
			helpList.append("Race to a Billion is a game where you pick spaces off a board to win cash, "
					+ "while trying not to hit your opponents' bombs.\n");
			helpList.append("Type !join to get into a game. If you need to leave a game before it starts, type !quit.\n");
			helpList.append("Once the game starts, you'll need to DM the bot with a space to place a bomb on.\n");
			helpList.append("Your opponents will do the same, and then you'll take turns choosing spaces.\n");
			helpList.append("All actions have a time limit, so don't go AFK or bad things may happen!\n");
			helpList.append("If you pick a space with a bomb hidden behind it, "
					+ "you'll blow up, lose $250,000, your booster, and your streak bonus, and be ejected from the game.\n");
			helpList.append("Your total cash bank builds up from round to round, "
					+ "and the objective is to reach one billion dollars.\n");
			helpList.append("To see a list of help files, type '!help list'.\n");
			break;
		case "spaces":
			helpList.append("Most spaces on the gameboard are cash. If you find one, the money is added to your total bank.\n");
			helpList.append("Other spaces hide boosters - these apply a multiplier to all the cash you win.\n");
			helpList.append("There are also minigames on the board. "
					+ "If you find one, you must win the game to be able to play it.\n");
			helpList.append("These give you the chance to win some extra cash, so try your best.\n");
			helpList.append("Finally, there are events hidden on the board that can trigger all sorts of special things.\n");
			helpList.append("Events are unpredictable and could help you or hurt you, so good luck!");
			break;
		case "boost":
		case "booster":
			helpList.append("You have a 'booster', which defaults to 100% and is displayed next to your score.\n");
			helpList.append("Some spaces on the board can add or subtract from this booster.\n");
			helpList.append("Most of the money you earn and lose will be multiplied by your current booster.\n");
			helpList.append("This includes the bomb penalty, so watch out!\n");
			helpList.append("The maximum booster possible is 999%, and the minimum is 10%.\n");
			helpList.append("Boosters carry over between games, but will (usually) be reset when you hit a bomb.\n");
			break;
		case "streak":
			helpList.append("When you win a game, you earn a streak bonus.\n");
			helpList.append("Every opponent you beat gains you +1 to this multiplier.\n");
			helpList.append("For example, if you win alone in a four-player game, you will gain +3 to your streak bonus.\n");
			helpList.append("You also receive a win bonus. "
					+ "The base win bonus is $20,000 for every space picked during the game.\n");
			helpList.append("If the board was cleared entirely, the win bonus is doubled.\n");
			helpList.append("Finally, if there are multiple winners, the win bonus is shared between them.\n");
			helpList.append("Each player's share of the win bonus is then multiplied by their booster and streak bonus.\n");
			helpList.append("Any minigames won also have the streak bonus multiplier applied to anything you win from them.\n");
			helpList.append("And if you get your streak bonus high enough, "
					+ "you will get to play special bonus games that can win you millions of dollars in one fell swoop.\n");
			helpList.append("However, when you lose a game, your streak bonus resets to x0. "
					+ "You have to keep winning if you want to keep it!");
			break;
		case "newbie":
			helpList.append("For your first ten games in the season, you are considered to be under newbie protection.\n");
			helpList.append("During this time, the standard bomb penalty is reduced from $250,000 to $100,000.\n");
			helpList.append("Specialty bombs and blammos can still multiply this value, of course.\n");
			helpList.append("In addition, you do not lose lives while under newbie protection, "
					+ "so you can play freely while learning about the game.");
			break;
		case "lives":
			helpList.append("Once you are out of newbie protection, you will have a limited number of lives.\n");
			helpList.append("By default, you have five lives. Every time you blow up in a game, you lose a life.\n");
			helpList.append("You can check how many lives you have remaining with !lives. "
					+ "If you run out of lives, you won't be able to play any more games.\n");
			helpList.append("But never fear! They'll all come back tomorrow. "
					+ "20 hours after you lose your first life, you'll be restocked to 5.");
			break;
			/*
		case "bombs":
			helpList.append("The standard bomb will simply dock you $250,000, "
					+ "reset your booster, and eject you from the round.\n");
			helpList.append("However, you can sometimes run into 'specialty' bombs that can do more (or less) harm.\n");
			helpList.append("The first kind is the 'boost hold' bomb, which doesn't reset your booster.\n");
			helpList.append("This means you'll still get to carry it through to the next round!\n");
			helpList.append("The second kind is the 'bankrupt' bomb. "
					+ "This bomb removes all the money you gained (or lost) during the round, "
					+ "and then applies the standard penalty.\n");
			helpList.append("The third kind is the 'cluster' (or 'chain') bomb. "
					+ "This bomb explodes multiple times (2, 4, 8, or even more!), "
					+ "multiplying the bomb penalty by the explosions.\n");
			helpList.append("Finally, although it's very rare, some bombs fail to explode at all. "
					+ "If this happens to you, count yourself lucky!");
			break;
			*/
		case "list":
		default:
			helpList.append("```\n");
			helpList.append("!commands    - View a full list of commands\n");
			helpList.append("!help        - Explains the basics of the game\n");
			helpList.append("!help spaces - Explains the types of spaces on the board\n");
			helpList.append("!help boost  - Explains the booster mechanics\n");
			helpList.append("!help streak - Explains the streak bonus multiplier\n");
			helpList.append("!help newbie - Explains newbie protection\n");
			helpList.append("!help lives  - Explains the life system\n");
			//helpList.append("!help bombs  - Explains the various specialty bombs\n");
			helpList.append("```");
			break;
		}
		event.replyInDm(helpList.toString());
	}
}