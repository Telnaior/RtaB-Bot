package tel.discord.rtab.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import tel.discord.rtab.GameController;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class RankCommand extends Command {
    public RankCommand()
    {
        this.name = "rank";
        this.help = "view the rank of a player (or yourself if there's no player listed)";
        this.guildOnly = false;
    }
	@Override
	protected void execute(CommandEvent event) {
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
			String name = event.getArgs();
			int index;
			//Search for own ID if no name given (to ensure match even if name changed)
			if(name.equals(""))
				index = GameController.findUserInList(list,event.getAuthor().getId(),false);
			else
				index = GameController.findUserInList(list,name,true);
			if(index == -1)
				event.reply("User not found.");
			else
			{
				String[] record = list.get(index).split(":");
				int money = Integer.parseInt(record[2]);
				StringBuilder response = new StringBuilder();
				response.append(record[1] + ": ");
				if(money<0)
					response.append("-");
				response.append("$");
				response.append(String.format("%,d",Math.abs(money)));
				response.append(" - Rank #" + (index+1) + "/" + list.size());
				event.reply(response.toString());
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
