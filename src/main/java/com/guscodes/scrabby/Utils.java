package com.guscodes.scrabby;

import com.guscodes.scrabby.gameitems.Word;

import java.util.*;

public class Utils {

    //board navigation static functions
    public static int locationAbove(int location) {
        if (location > 14) return location - 15;
        return -1;
    }

    public static int locationBelow(int location) {
        if (location + 15 < 225) return location + 15;
        return -1;
    }

    public static int locationToRight(int location) {
        if ((location + 1) % 15 != 0) return location + 1;
        return -1;
    }

    public static int locationToLeft(int location) {
        if (location % 15 != 0) return location - 1;
        return -1;
    }

    public static int toBoardLocation(int col, int row) {
        return (row * 15) + col;
    }

    public static int[] toBoardCoordinates(int location) {
        int col = 0;
        int row = 0;

        while (true) {
            if (location <= 14) {
                col = location;
                break;
            }
            row += 1;
            location -= 15;
        }
        return new int[] {col, row};
    }

    public static int nextLocation(int location, char orientation) throws IllegalArgumentException {
        if (orientation == 'H') {
            return locationToRight(location);
        }
        if (orientation == 'V') {
            return locationBelow(location);
        }
        else {
            throw new IllegalArgumentException("Orientation must be either 'V' or 'H'");
        }
    }

    public static int lastLocation(int location, char orientation) throws IllegalArgumentException {
        if (orientation == 'H') {
            return locationToLeft(location);
        }
        if (orientation == 'V') {
            return locationAbove(location);
        } else {
            throw new IllegalArgumentException("Orientation must be either 'V' or 'H'");
        }
    }

    public static int[] letterLocations(String word, int startCol, int startRow, char orientation) {
        int wordLength = word.length();
        int startLocation = toBoardLocation(startCol, startRow);
        int[] letterLocations = new int[word.length()];

        letterLocations[0] = startLocation;
        for (int index=1; index < wordLength; index++) {
            int nextSquare = nextLocation(letterLocations[index-1], orientation);
            letterLocations[index] = nextSquare;
        }
        return letterLocations;

    }

    public static int[] letterLocations(String word, int startLocation, char orientation) {
        int[] coordinates = toBoardCoordinates(startLocation);
        return letterLocations(word, coordinates[0], coordinates[1], orientation);
    }

    public static List<Word> sortWordsByRating(Collection<Word> wordsToSort) {
        List<Word> sortedWords = new ArrayList<>(wordsToSort);
        Collections.sort(sortedWords);
        return sortedWords;
    }

    public static Map<Character, Integer> counter(String stringToCount) {
        Map<Character, Integer> count = new HashMap<>();
        for (char alphabetLetter : "ABCDEFGHIJKLMNOPQRSTUVWXYZ~".toCharArray()) count.put(alphabetLetter, 0);
        char[] stringChars = stringToCount.toCharArray();
        for (Character stringLetter : stringChars) {
            count.put(stringLetter, count.get(stringLetter) + 1);
        }
        return count;
    }
}
