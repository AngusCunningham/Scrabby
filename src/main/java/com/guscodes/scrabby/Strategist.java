package com.guscodes.scrabby;

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
- Prefer not to play words which can be easily extended with s, ed, er etc right before a triple word

In the end game
- Prefer to play words which use higher scoring letters from the rack - COMPLETE - OPTIMAL MULTIPLIER IS THE REMAINING TILE SCORE SUM
- Keep track of the remaining tiles based on what has been played and what is on the generator's rack, use this to disadvantage the other player
 */

public class Strategist {
    private Board board;
    private Scorer scorer;

    //flags
    boolean efficientBlankUsage = false;
    boolean preferLowerRackScore = false;
    boolean preferNoRepeatsInRack = false;
    boolean preferRETAINSRack = true;

    public Strategist(Board board, Scorer scorer) {
        this.board = board;
        this.scorer = scorer;
    }

    public Set<Word> getStrategicRatings(Set<Word> words, String originalTray, double testParameter) {
        boolean isEndGame = board.getPlayedLocations().size() > 85;
        //System.out.printf("Endgame: %b\n", isEndGame);
        for (Word word : words) {
            //System.out.printf("%s old rating: %d\n", word.getWord(), word.getRating());
            if (! isEndGame) {
                if (efficientBlankUsage) {
                    word.modifyRatingByMultiplier(rateBlankUsage(word));
                }

                if (preferNoRepeatsInRack) {
                    double modifier = (double) 1 / (1 + (rateTrayRepetition(word, originalTray)) * testParameter);
                    //System.out.println(modifier);
                    word.modifyRatingByMultiplier(modifier);
                }

                if (preferRETAINSRack) {
                    double modifier = 1 + ((rateRETAINSQuality(word, originalTray) * testParameter));
                    word.modifyRatingByMultiplier(modifier);
                }
            }

            if (isEndGame) {
                if (preferLowerRackScore) {
                    word.modifyRatingByMultiplier(1 + (rateRemainingTrayInEndgame(word) * testParameter));
                }
            }
            //System.out.printf("%s new rating: %d\n", word.getWord(), word.getRating());
        }

        return words;
    }

    private int rateBlankUsage(Word word) {
        if (word.getTrayLettersUsed().size() < Data.EMPTY_RACK_BONUS && word.getScore() < 50) {
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
            fromTrayTotalPoints += scorer.getLetterScore(letter.charAt(0));
        }
        return fromTrayTotalPoints;
    }

    private int rateTrayRepetition(Word word, String originalTray) {
        String remainingTray = getTrayLeave(originalTray, word.getTrayLettersUsed());
        if (remainingTray.length() == 0) {
            return 0;
        }

        Map<Character, Integer> remainingQuantities = new HashMap<>();
        for (char tile : remainingTray.toCharArray()) {
            if (remainingQuantities.keySet().contains(tile)) {
                int newQuantity = remainingQuantities.get(tile) + 1;
                remainingQuantities.put(tile, newQuantity);
            }
            else {
                remainingQuantities.put(tile, 1);
            }
        }

        int repetitionScore = 0;
        for (char tile : remainingQuantities.keySet()) {
            if (remainingQuantities.get(tile) > 1) {
                repetitionScore += remainingQuantities.get(tile) - 1;
            }
        }

        return repetitionScore;
    }

    private int rateRETAINSQuality(Word word, String originalTray) {
        String remainingTray = getTrayLeave(originalTray, word.getTrayLettersUsed());

        int trayRating = Data.MAX_TRAY_SIZE;
        for (char letter : remainingTray.toCharArray()) {
            if (! Arrays.asList( "RETAINS~".split("")).contains(letter)) {
                trayRating -= 1;
            }
        }

        //System.out.printf("After playing %s, %s remains in tray, tray rated %d\n", word.getWord(), remainingTray, trayRating);
        return trayRating;
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
