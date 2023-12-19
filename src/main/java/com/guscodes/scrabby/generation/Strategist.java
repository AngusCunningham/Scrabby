package com.guscodes.scrabby.generation;

import com.guscodes.scrabby.Data;
import com.guscodes.scrabby.Utils;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.gameitems.Board;
import com.guscodes.scrabby.lexicon.DictHandler;

import java.util.*;

/* FEATURES PLANNED / COMPLETED / IN PROGRESS:

Before the end game
- Prefer to save blanks for words scoring more highly - COMPLETE - OPTIMAL MULTIPLIER IS 0

- Prefer to keep RETAINS tiles in tray when possible - IN PROGRESS
- Prefer not to have double letters in tray - IN PROGRESS
- Prefer a balance of vowels and consonants in tray

- Prefer to play words which score more per new tile used
- Prefer to use high scoring tiles for words which score more highly

- Prefer to play words which take bonus squares from opponent or block the use of bonus squares
- Prefer to play words which open up fewer bonus squares for opponent to use
- Prefer not to play words which can be easily extended with s, ed, er etc. right before a triple word

In the end game
- Prefer to play words which use higher scoring letters from the rack - COMPLETE - OPTIMAL MULTIPLIER IS THE REMAINING TILE SCORE SUM
- Keep track of the remaining tiles based on what has been played and what is on the generator's rack, use this to disadvantage the other player
 */

public class Strategist {
    /*todo: combine rateTrayRepetition() and rateRETAINSQuality() into a rateRemainingTray() function which takes into
            account a) how common a letter is within the lexicon and b) how many of that letter remain in play
            e.g. 'E' is a very useful letter, if there are already 11 out of the 12 'E' tiles placed on the board, having
            an 'E' tile remain in the rack after playing a word is very beneficial. Otherwise, if there is only 1 out of
            the 12 'E' tiles places on the board, it is less beneficial to keep an 'E' on the rack after playing a word,
            however this is still more beneficial than keeping a Q on the rack.
    */
    private Board board;
    private final DictHandler dictHandler;
    private final Map<Character, Double> frequencyTable;
    //flags
    boolean efficientBlankUsage = true;
    boolean preferLowerRackScore = true;
    boolean useTrayLeaveRating = true;

    public Strategist(DictHandler dictHandler) {
        this.dictHandler = dictHandler;
        this.frequencyTable = dictHandler.getFrequencyTable();
    }
    public Set<Word> getStrategicRatings(Set<Word> words, String originalTray, Board board, double testParameter) {
        this.board = board;
        boolean isEndGame = board.getPlayedLocations().size() > 82;
        Map<Character, Integer> unseenTiles = new HashMap<>(board.getUnseenTiles());
        //System.out.printf("Endgame: %b\n", isEndGame);
        for (Word word : words) {

            //System.out.printf("%s old rating: %d\n", word.getWord(), word.getRating());
            if (! isEndGame) {
                if (efficientBlankUsage) {
                    word.modifyRatingByMultiplier(rateBlankUsage(word));
                }

                if (useTrayLeaveRating) {
                    Map<Character, Integer> unseenForWord = new HashMap<>(unseenTiles);
                    for (char tile : originalTray.toCharArray()) {
                        unseenForWord.put(tile, unseenTiles.get(tile) - 1);
                    }
                    String trayLeave = getTrayLeave(originalTray, word.getTrayLettersUsed());
                    Map<Character, Integer> trayLeaveCounter = Utils.counter(trayLeave);
                    List<String> trayLeaveLetters = Arrays.asList(trayLeave.split(""));
                    double modifier = getLeaveRating(trayLeaveCounter, trayLeaveLetters, unseenForWord);
                    word.modifyRatingByMultiplier(1 + (modifier * testParameter));
                }
            }

            if (isEndGame) {
                if (preferLowerRackScore) {
                    word.modifyRatingByMultiplier(1 + (rateRemainingTrayInEndgame(word) * 0.5));
                }
            }
            //System.out.printf("%s new rating: %d\n", word.getWord(), word.getRating());
        }

        return words;
    }

    private int rateBlankUsage(Word word) {
        if ((word.getTrayLettersUsed().size() < Data.EMPTY_RACK_BONUS) && (word.getScore() < 50)) {
            for (String tile : word.getTrayLettersUsed()) {
                if (Character.isLowerCase(tile.charAt(0))) {
                    return 0;
                }
            }
            return 1;
        }
        return 1;
    }

    private int rateRemainingTrayInEndgame(Word word) {
        int fromTrayTotalPoints = 0;
        for (String letter : word.getTrayLettersUsed()) {
            fromTrayTotalPoints += Data.LETTER_SCORES.get(letter.charAt(0));
        }
        return fromTrayTotalPoints;
    }

    private double getLeaveRating(Map<Character, Integer> trayLeaveCounter, List<String> trayLeave,
                                                                                Map<Character, Integer> unseenTiles) {
        // obtain total quantity of tiles as yet unseen by the player
        double unseenTileTotalValue = 0;
        int totalTilesRemaining = 0;
        for (char tile : unseenTiles.keySet()) {
            int quantityOfTileUnseen = unseenTiles.get(tile);
            totalTilesRemaining += quantityOfTileUnseen;
            unseenTileTotalValue += frequencyTable.get(tile);
        }

        // for any open spaces in the rack, fill them with a theoretical tile of average value for word building
        double averageUnseenTileValue = unseenTileTotalValue / totalTilesRemaining;
        int newTileSlotsInTray = Data.MAX_TRAY_SIZE - trayLeave.size();

        double leaveRating = newTileSlotsInTray * averageUnseenTileValue;

        // don't try to analyse the individual tiles on an empty tray
        if (trayLeave.size() != 0) {
            for (Character tile : trayLeaveCounter.keySet()) {
                if (trayLeaveCounter.get(tile) == 1) {
                    leaveRating += frequencyTable.get(tile);
                    // TODO: penalise repeated letter more
                    // TODO: reward rack balance 4 consonant to 3 vowel
                }
            }

            // reward holding specific useful combinations of tile on the rack
            Set<Boolean> combosFound = new HashSet<>();
            combosFound.add(trayLeave.contains("I") && trayLeave.contains("N") && trayLeave.contains("G"));
            combosFound.add(trayLeave.contains("E") && (trayLeave.contains("D") || trayLeave.contains("R")));
            combosFound.add(trayLeave.contains("Q") && trayLeave.contains("U"));
            combosFound.add(trayLeave.contains("S"));
            for (Boolean combo : combosFound) {
                if (combo) leaveRating += 0.2; //todo: this constant should be tweaked for best performance, 0.2 is an arbitrary value
            }
        }

        return leaveRating;
    }

    private String getTrayLeave(String originalTray, List<String> tilesUsed) {
        String remainingTray = originalTray;

        for (String tile : tilesUsed) {
            if (Character.isLowerCase(tile.charAt(0))) {
                remainingTray = remainingTray.replaceFirst("~", "");
                continue;
            }
            remainingTray = remainingTray.replaceFirst(tile, "");
        }

        return remainingTray;
    }
}
