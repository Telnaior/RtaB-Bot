package tel.discord.rtab.minigames;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Scanner;

import tel.discord.rtab.enums.Games;

public class MinigameTest
{
	static Scanner sc = new Scanner(System.in);
	static EnumSet<Games> games = EnumSet.allOf(Games.class);
	public static void main(String[] args)
	{
		askForNextGame();
	}
	static void askForNextGame()
	{
		System.out.println("Games available for testing:");
		//Go through every minigame and add their short-name to the list
		StringBuilder gameList = new StringBuilder();
		games.forEach(minigame -> gameList.append(minigame.getShortName()+" "));
		System.out.println(gameList);
		//Now ask them for which one to play
		System.out.print("Choose a game to test (q to quit): ");
		String gameChoice = sc.nextLine();
		//If they chose to quit, close the scanner and call it a day
		if(gameChoice.equals("q"))
		{
			sc.close();
			return;
		}
		//Otherwise find the game they chose
		Optional<Games> gameChosen = games.stream().filter
				(minigame -> gameChoice.toLowerCase().equals(minigame.getShortName().toLowerCase())).findFirst();
		//If they didn't match any game, error them out as such
		if(!gameChosen.isPresent())
		{
			System.out.println("Game not found.");
			askForNextGame();
			return;
		}
		//Now we know they matched, so...
		System.out.print("Have a bot play for you? (Y/N) ");
		String botChoice = sc.nextLine();
		if(botChoice.toLowerCase().equals("y"))
			runGame(gameChosen.get().getGame(),true);
		else
			runGame(gameChosen.get().getGame(),false);
	}
	static void runGame(MiniGame game, boolean isBot)
	{
		game.initialiseGame("test", 1).forEach(line -> System.out.println(line));
		while(!game.isGameOver())
		{
			String nextChoice;
			if(isBot)
			{
				nextChoice = game.getBotPick();
				System.out.println("> "+nextChoice);
			}
			else
			{
				System.out.print("> ");
				nextChoice = sc.nextLine();
			}
			game.playNextTurn(nextChoice).forEach(line -> System.out.println(line));	
		}
		System.out.println(String.format("Game Over. Winnings: $%,d",game.getMoneyWon()));
		askForNextGame();
	}
}
