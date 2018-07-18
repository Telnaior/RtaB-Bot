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
 
0.6.0 (planned)
 - Created more enums (game status, player alive/dead)
 - Cleaned up join/quit code
 - Added multiple-player games! The board size is 5 plus another five for each player in the game, 
 	and the winstreak and win bonuses are multiplied based on how many players lose.
 	
0.5.1 (planned)
 - Money messages now mention the final amount earned, not just the pre-booster amount.
 
0.5.0 (planned)
 - Added boosters, which multiply all cash earned.
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