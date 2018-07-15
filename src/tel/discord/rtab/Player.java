package tel.discord.rtab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;


class Player
{
	User user;
	String name;
	String uID;
	int money;
	Player(Member playerName)
	{
		user = playerName.getUser();
		name = playerName.getNickname();
		if(name == null)
			name = user.getName();
		uID = user.getId();
		money = 0;
		try
		{
			List<String> list = Files.readAllLines(Paths.get("scores.csv"));
			String[] record;
			for(int i=0; i<list.size(); i++)
			{
				/*
				 * record format:
				 * record[0] = uID
				 * record[1] = name
				 * record[2] = money
				 */
				record = list.get(i).split(":");
				if(record[0].equals(uID))
				{
					money = Integer.parseInt(record[2]);
					break;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}