package tel.discord.rtab.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class TopCommand extends Command {
    public TopCommand()
    {
        this.name = "top";
        this.help = "view the top ten players on the leaderboard";
        this.guildOnly = false;
    }
	@Override
	protected void execute(CommandEvent event) {
		try {
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
			StringBuilder response = new StringBuilder().append("```\n");
			String[] record;
			int money, moneyLength = 0;
			//Get top 10, or fewer if list isn't long enough
			for(int i=0; i<Math.min(list.size(),10); i++)
			{
				/*
				 * record format:
				 * record[0] = uID
				 * record[1] = name
				 * record[2] = money
				 */
				record = list.get(i).split(":");
				money = Integer.parseInt(record[2]);
				//Need to actually have money to be listed
				if(money <= 0)
					break;
				//Get the length to format all values to
				if(i == 0)
				{
					moneyLength = String.valueOf(money).length();
					moneyLength += (moneyLength-1)/3;
				}
				response.append("#" + String.format("%02d",(i+1)) + ": $"); 
				response.append(String.format("%,"+moneyLength+"d",money));
				response.append(" - " + record[1] + "\n");
			}
			response.append("```");
			event.reply(response.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
