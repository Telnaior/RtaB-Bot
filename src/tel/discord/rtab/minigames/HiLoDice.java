package tel.discord.rtab.minigames;

import java.util.LinkedList;
import tel.discord.rtab.objs.Dice;

/**
 *
 * @author Jeffrey Hope
 */
public class HiLoDice implements MiniGame {
    static final String NAME = "Hi/Lo Dice";
    static final boolean BONUS = false;
    boolean[] closedSpaces;
    Dice dice;
    boolean isAlive; 
    int score, lastRoll;
    
    @Override
    public LinkedList<String> initialiseGame(String channelID, int baseMultiplier)
    {
    	//TODO make base multiplier matter
        LinkedList<String> output = new LinkedList<>();
        
        dice = new Dice();
        closedSpaces = new boolean[dice.getDice().length * dice.getNumFaces()
                - (dice.getDice().length - 1)];
        isAlive = true;
        score = 0;
        
        output.add("In Hi/Lo Dice, the object is to predict how long you can " 
                + "roll two six-sided dice without repeating a number and "
                + "whether each roll is higher or lower than the roll before "
                + "it.");
        output.add("You will start off with $10,000 times the first roll.");
        output.add("For each successful higher/lower prediction, you'll get "
                + "$50,000 plus $10,000 times the amount thrown.");
        output.add("But if you predict incorrectly or you roll a number that "
                + "you've already rolled, you lose everything.");
        output.add("You are free to walk away with your winnings at any point, "
                + "however.");
        output.add("Good luck!");
        
        dice.rollDice();
        score += dice.getDiceTotal() * 10000;
        closedSpaces[dice.getDiceTotal() - 2] = true;
        lastRoll = dice.getDiceTotal();
        output.add("You rolled: " + dice.toString());
        
        output.add(promptChoice());

        return output;
    }
    
    @Override
    public LinkedList<String> playNextTurn(String pick) {
        LinkedList<String> output = new LinkedList<>();
        
        if (pick.toUpperCase().equals("STOP")) {
            isAlive = false;
        }
        else if (pick.toUpperCase().equals("HIGHER")) {
            LinkedList<String> outputResult = outputResult(true);
            while (!outputResult.isEmpty())
                output.add(outputResult.pollFirst());
        }
        else if (pick.toUpperCase().equals("LOWER")) {
            LinkedList<String> outputResult = outputResult(false);
            while (!outputResult.isEmpty())
                output.add(outputResult.pollFirst());
        }
        return output;
    }

    String promptChoice() {
        // Make it grammatically correct
        StringBuilder display = new StringBuilder();
        if (allSpacesClosed()) {
            display.append("You rolled all the numbers!");
            isAlive = false;
        }
        else {
            display.append(generateBoard());
        
            display.append("Type STOP to stop with your total, or you can try to "
                    + "guess whether the next roll will be HIGHER or LOWER than a");
            if (dice.getDiceTotal() == 8 || dice.getDiceTotal() == 11)
                display.append("n");
            display.append(" " + dice.getDiceTotal() + ".");
        }
        return display.toString();
    }

    LinkedList<String> outputResult(boolean guessHigher) {
        LinkedList<String> output = new LinkedList<>();
        
        dice.rollDice();
        output.add("You rolled: " + dice.toString());
        if(dice.getDiceTotal() == lastRoll)
        {
        	output.add(dice.getDiceTotal() + " is not " + (guessHigher ? "higher" : "lower") + " than itself. Sorry.");
        	score = 0;
        	isAlive = false;
        }
        else if (guessHigher && dice.getDiceTotal() < lastRoll) {
            output.add(dice.getDiceTotal() + " is not higher than "
                    + lastRoll + ". Sorry.");
            score = 0;
            isAlive = false;
        }
        else if (!guessHigher && dice.getDiceTotal() > lastRoll) {
            output.add(dice.getDiceTotal() + " is not lower than "
                    + lastRoll + ". Sorry.");
            score = 0;
            isAlive = false;
        }
        else if (closedSpaces[dice.getDiceTotal() - 2]) {
            // Make the sentence grammatically correct
            String s = "You already rolled a";
            if (dice.getDiceTotal() == 8 || dice.getDiceTotal() == 11)
                s += "n";
            s += " " + dice.getDiceTotal() + ". Sorry.";
            output.add(s);
            score = 0;
            isAlive = false;
        }
        else {
            output.add("Correct prediction!");
            score += 50000 + 10000 * dice.getDiceTotal();
            closedSpaces[dice.getDiceTotal() - 2] = true;
            lastRoll = dice.getDiceTotal();
            output.add(promptChoice());
        }
        
        return output;
    }

    String generateBoard() {
        StringBuilder display = new StringBuilder();
		display.append("```\n");
		display.append("   H I / L O   D I C E\n");
		display.append("    Total: $" + String.format("%,9d", score));
                display.append("\n\n");
                for (int i = 0; i < closedSpaces.length; i++) {
                    if (!closedSpaces[i]) {
                        display.append((i+dice.getDice().length) + " ");
                    }
                    else {
                        display.append("  ");
                        if (i+dice.getDice().length >= 10)
                            display.append(" ");
                    }
                }
		display.append("\n```");
		return display.toString();
    }

    boolean allSpacesClosed() {
        for (int i = 0; i < closedSpaces.length; i++)
            if (closedSpaces[i] == false)
                return false;
        return true;
    }
    
    @Override
    public boolean isGameOver() {
        return !isAlive;
    }

    @Override
    public int getMoneyWon() {
        return score;
    }

    @Override
    public boolean isBonusGame() {
        return BONUS;
    }

    @Override
    public String getBotPick() {
        boolean tryHigherFirst;
        
        if (lastRoll < 7)
            tryHigherFirst = true;
        else if (lastRoll > 7)
            tryHigherFirst = false;
        else tryHigherFirst = Math.random() > 0.5;
        
        dice.rollDice();
        
        if (tryHigherFirst) {
            if (dice.getDiceTotal() > lastRoll &&
                    !closedSpaces[dice.getDiceTotal() - dice.getDice().length])
                return "HIGHER";
            else if (lastRoll != 2) {
                dice.rollDice();
                if (dice.getDiceTotal() < lastRoll &&
                        !closedSpaces[dice.getDiceTotal() -
                        dice.getDice().length])
                return "LOWER";
            }
        }
        else { // Go in the reverse order
            if (dice.getDiceTotal() < lastRoll &&
                    !closedSpaces[dice.getDiceTotal() - dice.getDice().length])
                return "LOWER";
            else if (lastRoll != 12) {
                dice.rollDice();
                if (dice.getDiceTotal() > lastRoll &&
                        !closedSpaces[dice.getDiceTotal() -
                        dice.getDice().length])
                return "HIGHER";
            }            
        }
        
        return "STOP";
    }
    
    @Override
    public String toString()
    {
        return NAME;
    }
}
