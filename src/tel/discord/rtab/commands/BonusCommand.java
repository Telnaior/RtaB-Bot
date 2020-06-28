package tel.discord.rtab.commands;

import tel.discord.rtab.GameController;
import tel.discord.rtab.RaceToABillionBot;
import tel.discord.rtab.enums.SpaceType;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BonusCommand extends Command
{
	public BonusCommand()
	{
		this.name = "bonus";
		this.help = "take a dip into the bonus bag instead of selecting a space from the board";
		this.hidden = true;
	}
	@Override
	protected void execute(CommandEvent event)
	{
		for(GameController game : RaceToABillionBot.game)
		{
			if(game.channel.equals(event.getChannel()))
			{
				SpaceType desire;
				switch(event.getArgs().toUpperCase())
				{
				case "CASH":
				case "C":
				case "MONEY":
				case "M":
					desire = SpaceType.CASH;
					break;
				case "BOOST":
				case "BOOSTER":
				case "B":
					desire = SpaceType.BOOSTER;
					break;
				case "MINIGAME":
				case "GAME":
				case "G":
					desire = SpaceType.GAME;
					break;
				case "EVENT":
				case "E":
				case "SPLIT AND SHARE":
					desire = SpaceType.EVENT;
					break;
				case "BOMB":
					event.reply("Well, if you say so.");
				case "GRAB BAG":
				case "BILLION":
				case "ONE BILLION DOLLARS":
				case "A BILLION DOLLARS":
				case "A BILLION":
				case "AMULET OF YENDOR":
				case "THE AMULET OF YENDOR":
					desire = SpaceType.GRAB_BAG; //greedy
					break;
					//Useless memes follow
				case "BLAMMO":
					if(Math.random() < 0.5)
						event.reply("Does this look like !blammo to you?");
					else
						event.reply("There are no blammos in the bonus bag.");
					return;
				case "NUMBERWANG":
				case "NEGATIVE BILLION":
				case "A NEGATIVE BILLION":
					event.reply("You found **Numberwang**, worth shinty-six. Your score is unaffected.");
					return;
				case "TRIFORCE":
					event.reply("You found **The Triforce**! "
							+ "Click here to claim your prize: <https://www.youtube.com/watch?v=dQw4w9WgXcQ>");
					return;
				case "MEME":
					event.reply("https://niceme.me");
					return;
				case "HIDDEN":
				case "HIDDEN COMMAND":
				case "A HIDDEN COMMAND":
					event.reply("If you're using this command, you already have one.");
					return;
				case "BLESSED FIXED GREASED +2 GRAY DRAGON SCALE MAIL":
				case "BLESSED FIXED GREASED +2 GREY DRAGON SCALE MAIL":
				case "BLESSED FIXED GREASED +2 SILVER DRAGON SCALE MAIL":
					event.reply("Unfortunately, nothing happens. (It's the bonus bag, not a wand of wishing)");
					return;
				default:
					event.reply("Use !bonus cash, !bonus boost, !bonus game, or !bonus event to specify what you want.");
					return;
				}
				if(!game.useBonusBag(event.getAuthor(),desire))
					event.reply("You can't use this right now.");
				return;
			}
		}
		//We aren't in a game channel? Uh...
		event.reply("This is not a game channel.");
	}
}