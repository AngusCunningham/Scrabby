package com.guscodes.scrabby.analysis;

import com.guscodes.scrabby.Data;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.gameitems.Board;

public class Scorer {

    public int getTotalScore(Word[] wordAndIncidentals, Board board) {
        int totalScore = 0;
        for (Word word : wordAndIncidentals) {
            if (!(word == null)) {
                if (!(board.getPlayedWords().contains(word))) {
                    totalScore += scoreAWord(word, board);
                }
            }
        }
        return totalScore;
    }


    private int scoreAWord(Word wordToScore, Board board) throws IllegalArgumentException {
        int wordScore = 0;
        int wordMultiplier = 1;
        int newLettersAdded = 0;

        if (wordToScore == null || wordToScore.getWord() == null || wordToScore.getLocations() == null) {
            throw new IllegalArgumentException("Word must not be null and must have an underlying word string " +
                                                                                    "and locations");
        }

        String word = wordToScore.getWord();
        int[]locations = wordToScore.getLocations();

        char[] wordLetters = word.toCharArray();

        for (int index = 0; index < word.length(); index++) {
            int letterScore;
            try {
                letterScore = Data.LETTER_SCORES.get(wordLetters[index]);
            }
            catch (NullPointerException e) {
                System.out.printf("Letter %c not found in letterScoresTable\n", wordLetters[index]);
                throw e;
            }

            int currentLocation = locations[index];
            //System.out.printf("Tile score: %d\n", letterScore);
            if (!(board.getPlayedLocations().contains(currentLocation))) {
                newLettersAdded += 1;
                wordToScore.addLetterFromTray(String.valueOf(wordLetters[index]));
                if (Data.DLS_LOCATIONS.contains(currentLocation)) {
                    letterScore *= 2;
                }

                if (Data.TLS_LOCATIONS.contains(currentLocation)) {
                    letterScore *= 3;
                }

                if (Data.DWS_LOCATIONS.contains(currentLocation)) {
                    wordMultiplier *= 2;
                }

                if (Data.TWS_LOCATIONS.contains(currentLocation)) {
                    wordMultiplier *= 3;
                }
            }
            //System.out.printf("Total score for tile: %d\n", letterScore);
            wordScore += letterScore;
        }

        if (newLettersAdded == Data.EMPTY_RACK_BONUS) {
            //System.out.printf("Empty rack bonus applied to %s!", word);
            return (wordScore * wordMultiplier) + 50;
        }
        //System.out.println(word + ":" + wordScore + "\n\n");
        return wordScore * wordMultiplier;
    }
}
