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
 
0.4.0 (planned)
 - Cash amounts now vary from $10,000 to $1,000,000, along with -$10,000 to -$50,000.
 - The winner now gets a bonus for every space left on the board.
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