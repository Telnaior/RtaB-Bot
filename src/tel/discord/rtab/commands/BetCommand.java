package tel.discord.rtab.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.entities.Member;
import tel.discord.rtab.GameController;
import tel.discord.rtab.Player;
import tel.discord.rtab.RaceToABillionBot;
import tel.discord.rtab.enums.GameStatus;

public class BetCommand extends Command
{
	public BetCommand()
	{
		this.name = "bet";
		this.aliases = new String[]{"placebet"};
		this.help = "place a bet (if this is a betting channel)";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		//Start by parsing the bet
		Member bettor = event.getMember();
		String[] args = event.getArgs().split(", ");
		int amount;
		String player;
		//Make sure there's actually two arguments here
		if(args.length < 2)
		{
			event.reply("Bet format: !bet [amount], [player]");
			return;
		}
		//Now let's figure out which one's the amount
		if(checkValidNumber(args[0]))
		{
			amount = Integer.parseInt(args[0]);
			player = args[1].toUpperCase();
		}
		else if(checkValidNumber(args[1]))
		{
			amount = Integer.parseInt(args[1]);
			player = args[0].toUpperCase();
		}
		else
		{
			event.reply("Bet format: !bet [amount], [player]");
			return;
		}
		//Limit the max bet
		if(amount > 10000000)
		{
			event.reply("The maximum bet is 10,000,000. Sorry.");
			return;
		}
		//So at this point we've got our bet laid out, let's get the right channel
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel == event.getChannel())
			{
				//If it's not a betting channel, break out to the "no bets here" message
				if(game.betManager == null)
					break;
				//Make sure the player we're betting on exists
				boolean playerExists = false;
				for(Player target : game.players)
				{
					if(player.equals(target.getName().toUpperCase()))
					{
						playerExists = true;
						break;
					}
				}
				if(!playerExists)
				{
					event.reply("That player is not in the game.");
					event.reply(game.listPlayers(false));
					return;
				}
				//Make sure bets are open, then send it across
				if(game.gameStatus == GameStatus.SIGNUPS_OPEN)
					game.betManager.placeBet(bettor, amount, player);
				else
					event.reply("Bets are not allowed at this time.");
				//We found the right channel, so 
				return;
			}
		}
		//We aren't in a bet channel? Uh...
		event.reply("Bets are not permitted here.");
	}
	boolean checkValidNumber(String message)
	{
		try
		{
			int location = Integer.parseInt(message);
			return (location > 0);
		}
		catch(NumberFormatException e1)
		{
			return false;
		}
	}
}