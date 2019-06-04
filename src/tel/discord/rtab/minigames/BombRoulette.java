package tel.discord.rtab.minigames;

import java.util.LinkedList;

public class BombRoulette implements MiniGame {
    static final String NAME = "Bomb Roulette";
    static final boolean BONUS = false;
    boolean isAlive; 
        int score;
        boolean hasJoker;
        enum WheelSpace {CASH, DOUBLE, JOKER, BANKRUPT, BOMB};
        int[] spaceValues;
        WheelSpace[] spaceTypes;
        int pointer, cashLeft, cashSpaces, doubleSpaces, jokerSpaces,
                bankruptSpaces, bombSpaces;
        
    public LinkedList<String> initialiseGame()
    {
        LinkedList<String> output = new LinkedList<>();
        //Initialise wheel
        
        isAlive = true;
        score = 0;
        hasJoker = false;
        /* It might not initially make sense to assign non-cash space a cash 
         * value; but the bot uses this information to determine its strategy.
         */
        spaceValues = new int[] {10000, 10000, 10000, 10000, 10000, 15000,
                15000, 15000, 20000, 20000, 20000, 25000, 25000, 30000, 40000,
                50000, 75000, 100000, 0, 0, 0, 0, 0, 0};
        spaceTypes = new WheelSpace[] {WheelSpace.CASH, WheelSpace.CASH,
                WheelSpace.CASH, WheelSpace.CASH, WheelSpace.CASH,
                WheelSpace.CASH, WheelSpace.CASH, WheelSpace.CASH,
                WheelSpace.CASH, WheelSpace.CASH, WheelSpace.CASH,
                WheelSpace.CASH, WheelSpace.CASH, WheelSpace.CASH,
                WheelSpace.CASH, WheelSpace.CASH, WheelSpace.CASH,
                WheelSpace.CASH, WheelSpace.DOUBLE, WheelSpace.DOUBLE,
                WheelSpace.JOKER, WheelSpace.BANKRUPT, WheelSpace.BANKRUPT,
                WheelSpace.BOMB};
        for (int i = 0; i < spaceTypes.length; i++) {
            switch (spaceTypes[i]) {
                case CASH:
                    cashLeft += spaceValues[i];
                    cashSpaces++;
                    break;
                case DOUBLE:
                    doubleSpaces++;
                    break;
                case JOKER:
                    jokerSpaces++;
                    break;
                case BANKRUPT:
                    bankruptSpaces++;
                    break;
                case BOMB:
                    bombSpaces++;
                    break;
            }
        }
                
        //Display instructions
        output.add("In Bomb Roulette, you will be spinning a 24-space wheel "
                + "trying to collect as much cash as possible.");
        output.add("Eighteen of those spaces have various amounts of cash "
                + "ranging from $10,000 to $100,000. The total amount on the "
                + "wheel at the beginning of the game is $500,000.");
        output.add("Two are **Double** spaces, which will double your score up " 
                + "to that point.");
        output.add("Two are **Bankrupt** spaces, which will reset your score "
                + "back to $0.");
        output.add("One is a **Joker** space, which will save you in the event "
                + "that you hit...");
        output.add("...a **BOMB** space, which costs you all your winnings and "
                + "the game. There's only one on the wheel right now, but each "
                + "space you hit will be replaced with another BOMB space.");
        output.add("If you had an unused joker from the main game, it "
                + "unfortunately does not carry over. Sorry.");
        output.add("After you've spun the wheel at least once, you are free to "
                + "walk away with your winning if you wish.");
        output.add("Good luck! Type SPIN when you're ready.");
        output.add(generateBoard());
        return output;
    }
    
    public LinkedList<String> playNextTurn(String pick)
    {
        LinkedList<String> output = new LinkedList<>();
                
        if (pick.toUpperCase().equals("STOP")) {
            // Prevent accidentally stopping with nothing if the player hasn't spun yet
            if (bombSpaces == 1)
                return output;
            
            isAlive = false;
        }
                
        else if (pick.toUpperCase().equals("SPIN")) {
            output.add("Spinning wheel...");
            pointer = (int)(Math.random() * spaceTypes.length);
                    
            if (spaceTypes[pointer] == WheelSpace.BANKRUPT ||
                    spaceTypes[pointer] == WheelSpace.BOMB ||
                    Math.random() < (double)(bombSpaces + bankruptSpaces)
                    / spaceTypes.length) {
                output.add("...");
            }
                    
            switch (spaceTypes[pointer]) {
                case CASH:
                    output.add(String.format("**$%,d**!", spaceValues
                            [pointer]));
                    score += spaceValues[pointer];
                    break;
                case DOUBLE:
                    output.add("It's a **Double**!");
                    score *= 2;
                    break;
                case JOKER:
                    output.add("It's the **Joker**!");
                    hasJoker = true;
                break;
                case BANKRUPT:
                    output.add("**BANKRUPT**");
                    score = 0;
                    
                    if (cashSpaces == 0) {
                        output.add("...which normally doesn't eliminate you, "
                                + "but unfortunately, it's mathematically "
                                + "impossible to make any of your money back. "
                                + "Better luck next time.");
                        isAlive = false;
                    }
                    break;
                case BOMB:
                    output.add("It's a **BOMB**.");
                    if (hasJoker) {
                        output.add("But since you have a joker, we'll take that "
                                + "instead of your money!");
                        hasJoker = false;
                    }
                    else {
                        output.add("It goes **BOOM**.");
                        score = 0;
                        isAlive = false;
                    }
            }
                    
            if (isAlive) {
                bombSpace(pointer);
                        
                if (cashSpaces == 0 && doubleSpaces == 0) {
                    output.add("You've earned everything you can!");
                    isAlive = false;
                }
                else {
                    output.add(generateBoard());
                    output.add("SPIN again if you dare, or type STOP to stop "
                            + "with your current total.");
                }
            }
        }
                
        return output;
    }
        
    String generateBoard()
    {
        StringBuilder display = new StringBuilder();
        display.append("```\n");
        display.append("  B O M B   R O U L E T T E\n");
                display.append("  Total: $" + String.format("%,9d", score));
                if (hasJoker)
                    display.append(" + Joker");
                display.append("\n\n");
                if (cashSpaces > 0)
                    display.append(String.format("%,2dx Cash", cashSpaces) +
                            String.format(" ($%,7d Remaining)\n", cashLeft));
                if (doubleSpaces > 0)
                    display.append(String.format("%,2dx Double\n",
                            doubleSpaces));
                if (jokerSpaces > 0)
                    display.append(String.format("%,2dx Joker\n", jokerSpaces));
                if (bankruptSpaces > 0)
                    display.append(String.format("%,2dx Bankrupt\n",
                            bankruptSpaces));
                display.append(String.format("%,2dx Bomb\n", bombSpaces));
        display.append("```");
        return display.toString();
    }
    
        void bombSpace(int space) {
            // check what kind space it originally was, then update the stats to
            // match
            switch(spaceTypes[space]) {
                case CASH:
                    cashSpaces--;
                    cashLeft -= spaceValues[space];
                    break;
                case DOUBLE:
                    doubleSpaces--;
                    break;
                case JOKER:
                    jokerSpaces--;
                    hasJoker = true;
                    break;
                case BANKRUPT:
                    bankruptSpaces--;
                    break;
                case BOMB:
                    bombSpaces--; // it'll go back up, but deleting this line creates a bug
                    break;
            }
            
            spaceTypes[space] = WheelSpace.BOMB;
            bombSpaces++;
            
            for (int i = 0; i < spaceTypes.length; i++) {
                switch (spaceTypes[i]) {
                    case DOUBLE:
                        spaceValues[i] = score;
                        break;
                    case BANKRUPT:
                        spaceValues[i] = score * -1;
                        break;
                    case BOMB:
                        if (hasJoker)
                            spaceValues[i] = 0;
                        else spaceValues[i] = score * -1;
                        break;
                    default: // do nothing
                        break;
                }
            }
        }
        
        public int getExpectedValue() {
            int sum = 0;
            
            for (int i = 0; i < spaceValues.length; i++)
                sum += spaceValues[i];
            
            return sum/spaceValues.length;
        }
        
    @Override
    public boolean isGameOver()
    {
        return !isAlive;
    }

    @Override
    public int getMoneyWon()
    {
        return score;
    }

    @Override
    public boolean isBonusGame() {
        return BONUS;
    }

    @Override
    public String getBotPick() {
        if (score == 0 || getExpectedValue() > 0)
            return "SPIN";
            
        int testSpin = (int)(Math.random() * 24);
        if (spaceTypes[testSpin] == WheelSpace.BANKRUPT || (!hasJoker &&
                spaceTypes[testSpin] == WheelSpace.BOMB))
            return "STOP";
        return "SPIN";
    }
    
    @Override
    public String toString()
    {
        return NAME;
    }
}
