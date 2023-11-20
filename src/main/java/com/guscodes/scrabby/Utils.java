package com.guscodes.scrabby;

import java.util.*;

public class Utils {
    public static final int MAX_TRAY_SIZE = 7;
    public static final int EMPTY_RACK_BONUS = 7;
    public static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();


    public static HashSet<Character> getAlphabetSet() {
        HashSet<Character> setty = new HashSet<>();
        for (char letter : ALPHABET) {
            setty.add(letter);
        }
        return setty;
    }

    //board navigation static functions
    public static int sAbove(int location) throws IndexOutOfBoundsException {
        if (location > 14) return location - 15;
        throw new IndexOutOfBoundsException("No square above this one as it is at the top of the board");
    }

    public static int sBelow(int location) throws IndexOutOfBoundsException {
        if (location + 15 < 225) return location + 15;
        throw new IndexOutOfBoundsException("No square below this one as it is at the bottom of the board");
    }

    public static int sRight(int location) throws IndexOutOfBoundsException {
        if ((location + 1) % 15 != 0) return location + 1;
        else {
            throw new IndexOutOfBoundsException("No square right of this one as it is at the right of the board");
        }
    }

    public static int sLeft(int location) throws IndexOutOfBoundsException {
        if (location % 15 != 0) return location - 1;
        throw new IndexOutOfBoundsException("No square left of this one as it is at the left of the board");
    }

    public static int sLocation(int col, int row) {
        return (row * 15) + col;
    }

    public static int[] sCoordinates(int location) {
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

    public static int nextLocation(int location, char orientation) throws IndexOutOfBoundsException,
            IllegalArgumentException {
        if (orientation == 'H') {
            try {
                return sRight(location);
            }
            catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Next square horizontally would be off the board");
            }
        }
        if (orientation == 'V') {
            try{
                return sBelow(location);
            }
            catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Next square vertically would be off the board");
            }
        }
        else {
            throw new IllegalArgumentException("Orientation must be either 'V' or 'H'");
        }
    }

    public static int lastLocation(int location, char orientation) throws IndexOutOfBoundsException,
            IllegalArgumentException {
        if (orientation == 'H') {
            try {
                return sLeft(location);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Last square horizontally would be off the board");
            }
        }
        if (orientation == 'V') {
            try {
                return sAbove(location);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Last square vertically would be off the board");
            }
        } else {
            throw new IllegalArgumentException("Orientation must be either 'V' or 'H'");
        }
    }

    public static int[] letterLocations(String word, int startCol, int startRow, char orientation) {
        int wordLength = word.length();
        int startLocation = sLocation(startCol, startRow);
        int[] letterLocations = new int[word.length()];

        letterLocations[0] = startLocation;
        for (int index=1; index < wordLength; index++) {
            int nextSquare = nextLocation(letterLocations[index-1], orientation);
            letterLocations[index] = nextSquare;
        }
        return letterLocations;

    }

    public static int[] letterLocations(String word, int startLocation, char orientation) {
        int[] coordinates = sCoordinates(startLocation);
        return letterLocations(word, coordinates[0], coordinates[1], orientation);
    }


    public static int distanceBetween(int location1, int location2) throws IllegalArgumentException {
        int[] coord1 = sCoordinates(location1);
        int[] coord2 = sCoordinates(location2);

        if (coord1[0] == coord2[0]) {
            return coord1[1] - coord2[1];
        }
        else if (coord1[1] == coord2[1]) {
            return coord1[0] - coord2[0];
        }
        else {
            throw new IllegalArgumentException("Cannot calculate distance between squares on different rows/cols");
        }
    }

    public static List<Word> sortWordsByScore(Collection<Word> wordsToSort) {
        List<Word> sortedWords = new ArrayList<>(wordsToSort);
        Collections.sort(sortedWords);
        return sortedWords;
    }

    public static boolean arrayContainsInt(int[] array, int value) {
        for (int item : array) {
            if (value == item) return true;
        }
        return false;
    }
}
