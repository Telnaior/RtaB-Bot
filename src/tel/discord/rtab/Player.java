package tel.discord.rtab;

import net.dv8tion.jda.core.entities.User;


class Player
{
	User user;
	int money;
	Player(User playerName)
	{
		user = playerName;
		money = 0;
	}
}
