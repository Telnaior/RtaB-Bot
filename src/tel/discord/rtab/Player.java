package tel.discord.rtab;

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
		uID = user.getId();
		money = 0;
	}
}