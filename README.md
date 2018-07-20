Version Rule:  
Major Version 0 - Features still in development  
Minor Versions are standard updates including new features  
-alpha: features still being worked on  
-beta : features for release finished, working on bugfixes next  
Both alpha and beta replace patch numbering  
Patch Versions are bugfixes found after release of minor version

1.0.0 (planned)
 - Added bots! Bots will play the game if no one else joins within 2 minutes.
 - First public release, heralding the start of Season 1.
 
0.9.0 (planned)
 - Set up timeouts on player action (first offence - waste random space, second offence - autobomb)
 - Set up join cooldowns (once you bomb, you're out for a day)
 - Set up the game actually ending at $1,000,000,000
 
0.8.0 (planned)
 - Events! Chaos for players, and chaos to code in!
 
0.7.1 (planned)
 - Added bonus games, earned by increasing your bonus multiplier to multiples of 5.

0.7.0 (planned)
 - Minigames! Pick one up during the game and survive the round to play it.
   * Strike it Rich - Make three of a kind to win that amount.
   * Math Game - Make an equation out of randomly-selected cash and operations.
   * The Gamble - Pick a value, then take it or try to find a larger one.
 - Improved !rank command, adjusted formatting, !join command now responds with your name
 - Added !players command, to tell you who is in the game
 - Heaps more code cleanup, bugfixes, and formatting improvements.

0.6.4
 - Bugfixes with !join/!quit/!start not caring about the playercount where they should

0.6.3
 - Suspense chance now based on number of players, since larger boards get scarier sooner
 - Specialty bomb text formatting improved

0.6.2
 - Doubled the suspense chance (making them guaranteed for bombs and the final two spaces)
 - Specialty bombs!
   * Bankrupt bombs cost you everything you earned during the round
   * Boost Hold bombs let you keep your booster
   * Chain bombs have up to eight times the bomb penalty
   * Dud bombs don't blow up at all!
 
0.6.1
 - Better error handling
 - Add more variation to booster text based on context (no ! on a negative booster, "drained" if booster < 100%, etc) 
 
0.6.0
 - Created more enums (game status, player alive/dead)
 - Cleaned up join/quit code
 - Made the player list into a list, because seriously that's just so much more convenient.
 - Added multiple-player games! The board size is 5 plus another five for each player in the game, 
 	and the winstreak and win bonuses are multiplied based on how many players lose.
 	
0.5.1
 - Money messages now mention the final amount earned, not just the pre-booster amount.
 - Reworked cash chances, and booster values and chances. Booster caps changed to 10% and 999%.
 
0.5.0
 - Added boosters, capped at 20% and 500%, which multiply all cash earned.
 - Added a winstreak counter to the savefile format to serve as a bonus multiplier.
 - Win bonus added - $20k for each space picked during the game, doubled if the board was wiped clean.
 - Fixed the player objects to be in an array like they should have been the whole time, making everything *much* easier.
 - Cleaned up ugly code in board generation and saving scores to make use of players being kept in an array.
 - No seriously, why didn't I do it this way from the beginning?
 
0.4.0
 - Cash amounts now vary from -$25,000 to $1,000,000.
 - Score file is now sorted, and !rank and !top have been created to take advantage of this.

0.3.0
 - Bot now refers to players by their nicknames rather than their usernames, and uses mentions to alert players during the game.
 - Improved suspense mode to not trigger all the time
 - Improved player tracking, and added persistence! Money now carries over from game to game.

0.2.0
 - Added money tracking in a basic form: No persistence yet, just +100k for a safe space and -250k for a bomb
 - Board display improved, adding a status line to display current turn and money as well
 - Minor optimisations to methods

0.1.0
 - First functional version
 - Runs basic 2p 15-space game, where all spaces are either "SAFE" or a bomb