package com.guscodes.scrabby;

import com.guscodes.scrabby.gameitems.Word;

import java.util.*;

public class Utils {

    //board navigation static functions
    public static int locationAbove(int location) throws IndexOutOfBoundsException {
        if (location > 14) return location - 15;
        throw new IndexOutOfBoundsException("No square above this one as it is at the top of the board");
    }

    public static int locationBelow(int location) throws IndexOutOfBoundsException {
        if (location + 15 < 225) return location + 15;
        throw new IndexOutOfBoundsException("No square below this one as it is at the bottom of the board");
    }

    public static int locationToRight(int location) throws IndexOutOfBoundsException {
        if ((location + 1) % 15 != 0) return location + 1;
        else {
            throw new IndexOutOfBoundsException("No square right of this one as it is at the right of the board");
        }
    }

    public static int locationToLeft(int location) throws IndexOutOfBoundsException {
        if (location % 15 != 0) return location - 1;
        throw new IndexOutOfBoundsException("No square left of this one as it is at the left of the board");
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

    public static int nextLocation(int location, char orientation) throws IndexOutOfBoundsException,
            IllegalArgumentException {
        if (orientation == 'H') {
            try {
                return locationToRight(location);
            }
            catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Next square horizontally would be off the board");
            }
        }
        if (orientation == 'V') {
            try{
                return locationBelow(location);
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
                return locationToLeft(location);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Last square horizontally would be off the board");
            }
        }
        if (orientation == 'V') {
            try {
                return locationAbove(location);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Last square vertically would be off the board");
            }
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
}
