package tel.discord.rtab.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BetRankCommand extends Command {
    public BetRankCommand()
    {
        this.name = "betrank";
		this.aliases = new String[]{"balance","funds","credits"};
        this.help = "view the rank of a player by name, or by rank with #[number]";
        this.guildOnly = false;
    }
	@Override
	protected void execute(CommandEvent event) {
		try
		{
			List<String> list = Files.readAllLines(Paths.get("bettors"+event.getChannel().getId()+".csv"));
			String name = event.getArgs();
			int index;
			//Search for own ID if no name given (to ensure match even if name changed)
			if(name.equals(""))
				index = GameController.findUserInList(list,event.getAuthor().getId(),false);
			else if(name.startsWith("#"))
			{
				try
				{
					index = Integer.parseInt(name.substring(1))-1;
				}
				catch(NumberFormatException e1)
				{
					index = GameController.findUserInList(list,name,true);
				}
			}
			else
				index = GameController.findUserInList(list,name,true);
			if(index < 0 || index >= list.size())
			{
				if(name.equals(""))
					event.reply("You haven't made any bets yet - your starting balance is 10,000.");
				else
					event.reply("User not found.");
			}
			else
			{
				String[] record = list.get(index).split("#");
				int funds = Integer.parseInt(record[2]);
				int prestige = Integer.parseInt(record[3]);
				StringBuilder response = new StringBuilder();
				response.append(record[1] + ": ");
				if(prestige > 0)
					response.append(String.format("%,d BILLION + ",prestige));
				response.append(String.format("¤%,d",funds));
				response.append(" (Rank #" + (index+1) + "/" + list.size() + ")");
				event.reply(response.toString());
			}
		} catch (IOException e)
		{
			event.reply("This command must be used in a game channel.");
		}
	}

}
