package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PeekCommand extends Command
{
	public PeekCommand()
	{
		this.name = "peek";
		this.help = "use your peek (only available in-game)";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel.equals(event.getChannel()))
			{
				String peekSpot = event.getArgs();
				switch(game.validatePeek(event.getAuthor(),peekSpot))
				{
				case NOPEEK:
					event.reply("You don't have a peek to use.");
					break;
				case BADSPACE:
					event.reply("That is not a valid space.");
					break;
				case CASH:
					event.replyInDm("Space "+peekSpot+" is **CASH**.");
					break;
				case GAME:
					event.replyInDm("Space "+peekSpot+" is a **MINIGAME**.");
					break;
				case BOOST:
					event.replyInDm("Space "+peekSpot+" is a **BOOSTER**.");
					break;
				case EVENT:
					event.replyInDm("Space "+peekSpot+" is an **EVENT**.");
					break;
				case BOMB:
					event.replyInDm("Space "+peekSpot+" is a **BOMB**.");
					break;
				}
				//We found the right channel, so 
				return;
			}
		}
		//We aren't in a game channel? Uh...
		event.reply("This is not a game channel.");
	}
}