package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class JoinCommand extends Command
{
	public JoinCommand()
	{
		this.name = "join";
		this.aliases = new String[]{"in","enter","start","play","participate","embark","undertake","venture","engage","partake"};
		this.help = "join the game (or start one if no game is running)";
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel.equals(event.getChannel()))
			{
				switch(game.addPlayer(event.getMember()))
				{
				case CREATED:
					event.reply("Starting a game of Race to a Billion in two minutes. Type !join to sign up.");
				case JOINED:
					event.reply(event.getMember().getEffectiveName() + " successfully joined the game.");
					break;
				case UPDATED:
					event.reply("Updated in-game name.");
					break;
				case INPROGRESS:
					event.reply("Cannot join game: Game already running.");
					break;
				case ALREADYIN:
					event.reply("Cannot join game: Already joined game.");
					break;
				case BADNAME:
					event.reply("Cannot join game: Illegal name");
					break;
				case ELIMINATED:
					event.reply("Cannot join game: You have been eliminated from Race to a Billion.");
					break;
				case TOOMANYPLAYERS:
					event.reply("Cannot join game: Too many players.");
					break;
				case NOTALLOWEDHERE:
					event.reply("Cannot join game: Joining is not permitted in this channel.");
					break;
				}
				//We found the right channel, so 
				return;
			}
		}
		//We aren't in a game channel? Uh...
		event.reply("Cannot join game: This is not a game channel.");
	}
}