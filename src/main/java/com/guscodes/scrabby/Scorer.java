package com.guscodes.scrabby;

import java.util.HashMap;
import java.util.Map;

public class Scorer {
    private Map<Character, Integer> letterScores = new HashMap<>();
    private int[] doubleLetters = {3, 11, 36, 38, 45, 52, 59, 92, 96, 98, 102, 108, 116, 122, 126, 128, 132, 165, 172,
            179, 186, 188, 213, 221};
    private int[] tripleLetters = {20, 24, 76, 80, 84, 88, 136, 140, 144, 148, 200, 204};
    private int[] doubleWords = {16, 28, 32, 42, 48, 56, 64, 70, 112, 154, 160, 168, 176, 182, 192, 196, 208};
    private int[] tripleWords = {0, 7, 14, 105, 119, 210, 217, 224};
    private Board board;


    public Scorer(Board board) {
        this.board = board;

        letterScores.put('A', 1);
        letterScores.put('B', 3);
        letterScores.put('C', 3);
        letterScores.put('D', 2);
        letterScores.put('E', 1);
        letterScores.put('F', 4);
        letterScores.put('G', 2);
        letterScores.put('H', 4);
        letterScores.put('I', 1);
        letterScores.put('J', 8);
        letterScores.put('K', 5);
        letterScores.put('L', 1);
        letterScores.put('M', 3);
        letterScores.put('N', 1);
        letterScores.put('O', 1);
        letterScores.put('P', 3);
        letterScores.put('Q', 10);
        letterScores.put('R', 1);
        letterScores.put('S', 1);
        letterScores.put('T', 1);
        letterScores.put('U', 1);
        letterScores.put('V', 4);
        letterScores.put('W', 4);
        letterScores.put('X', 8);
        letterScores.put('Y', 4);
        letterScores.put('Z', 10);
        letterScores.put('~', 0);

        // lower case letters represent blanks
        for (char lowerCaseLetter : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
            letterScores.put(lowerCaseLetter, 0);
        }
    }

    public int getScore(Word[] wordAndIncidentals) {
        int totalScore = 0;
        for (Word word : wordAndIncidentals) {
            if (!(word == null)) {
                if (!(board.getPlayedWords().contains(word))) {
                    totalScore += scoreAWord(word);
                }
            }
        }
        return totalScore;
    }


    public int scoreAWord(Word wordWithLocations) {
        int wordScore = 0;
        int wordMultiplier = 1;
        int newLettersAdded = 0;

        String word = wordWithLocations.getWord();
        int[]locations = wordWithLocations.getLocations();

        char[] wordLetters = word.toCharArray();

        for (int index = 0; index < word.length(); index++) {
            int letterScore;
            try {
                letterScore = letterScores.get(wordLetters[index]);
            }
            catch (NullPointerException e) {
                System.out.printf("Letter %c not found in letterScoresTable\n", wordLetters[index]);
                throw e;
            }
            int currentLocation = locations[index];
            //System.out.printf("Tile score: %d\n", letterScore);
            if (!(board.getPlayedLocations().contains(currentLocation))) {
                newLettersAdded += 1;
                wordWithLocations.addLetterFromTray(String.valueOf(wordLetters[index]));
                if (Utils.arrayContainsInt(doubleLetters, currentLocation)) {
                    letterScore *= 2;
                }

                if (Utils.arrayContainsInt(tripleLetters, currentLocation)) {
                    letterScore *= 3;
                }

                if (Utils.arrayContainsInt(doubleWords, currentLocation)) {
                    wordMultiplier *= 2;
                }

                if (Utils.arrayContainsInt(tripleWords, currentLocation)) {
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

    public int getLetterScore(Character letter) {
        char letterUpper = Character.toUpperCase(letter);
        return letterScores.get(letterUpper);
    }
}
