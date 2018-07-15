package tel.discord.rtab;

import net.dv8tion.jda.core.entities.User;


class Player
{
	User user;
	String name;
	int money;
	Player(User playerName, String nickname)
	{
		user = playerName;
		name = nickname;
		money = 0;
	}
}
