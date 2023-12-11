package com.guscodes.scrabby.analysis;

import com.guscodes.scrabby.gameitems.Square;
import com.guscodes.scrabby.Utils;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.gameitems.Board;

import java.util.*;

public class Validator {
    private Board board;
    private Set<String> dictionary;
    public Validator(Set<String> dictionary) {
        this.dictionary = dictionary;
    }

    public Word[] getIncidentals(Word word, Board board) {
        /*
        Returns the incidentals which would be created if the word was played to the board
        The first incidental will be the word itself, extended if it made any inline incidentals
        Will return null if the word is invalid
        */

        this.board = board;

        if (word == null || word.getWord() == null || word.getWord() == "" || word.getLocations() == null) {
            return null;
        }

        String inputWord = word.getWord();
        int[] locations = word.getLocations();
        char orientation = word.getOrientation();
        Square[] currentBoardSquares = board.getSquares();

        // find out about which squares are blanks in the word
        Set<Integer> blankLocations = new HashSet<>();
        for (int index = 0; index < inputWord.length(); index++){
            if (Character.isLowerCase(inputWord.charAt(index))) {
                blankLocations.add(locations[index]);
            }
        }

        //check that the word uses an accessible square to attach to other words
        if (!(usesAccessibleSquare(locations))){
            return null;
        }

        // check that the incidental words created with existing words are all real words
        String plainWord = inputWord.toUpperCase();
        Word[] incidentalsCreated = getAllIncidentals(plainWord, locations, orientation, currentBoardSquares);
        if (!(incidentalsAreValid(incidentalsCreated))) {
            return null;
        }

        // add blanks back into all the incidentals at their correct locations
        for (Word incidental : incidentalsCreated) {
            if (!(incidental == null)) {
                int[] incidentalLocations = incidental.getLocations();

                for (int index = 0; index < incidentalLocations.length; index++) {
                    String coreWord = incidental.getWord();
                    int locationAtIndex = incidentalLocations[index];
                    char letterAtIndex = coreWord.charAt(index);
                    char squareContents = board.getSquares()[locationAtIndex].getContents();

                    if (blankLocations.contains(locationAtIndex)) {
                        String newWordStart = coreWord.substring(0, index);
                        char blankTile = Character.toLowerCase(letterAtIndex);
                        String newWordEnd = coreWord.substring(index + 1);
                        String newWord = newWordStart + blankTile + newWordEnd;
                        incidental.setWord(newWord);
                    }

                    else if ((squareContents != '_') && (Character.isLowerCase(squareContents))) {
                        String newWordStart = coreWord.substring(0, index);
                        char blankTile = Character.toLowerCase(letterAtIndex);
                        String newWordEnd = coreWord.substring(index + 1);
                        String newWord = newWordStart + blankTile + newWordEnd;
                        incidental.setWord(newWord);
                    }
                }
            }
        }

        return incidentalsCreated;
    }

    private boolean usesAccessibleSquare(int[] letterLocations) {
        Set<Integer> accessibleLocations = board.getAccessibleLocations();
        for (int location : letterLocations) {
            if ((accessibleLocations.contains(location))) {
                return true;
            }
        }
        return false;
    }

    private boolean incidentalsAreValid(Word[] allIncidentals) {
        // for each occurring incidental word, check that it is a real word, and it is not using only played squares
        for (int index = 0; index < allIncidentals.length; index++) {
            Word incidental = allIncidentals[index];

            // incidentals other than [0] might be null if no incidental occurred at the selected index
            if (incidental != null) {
                String incidentalWord = incidental.getWord();
                int[] incidentalLocations = incidental.getLocations();
                if (incidentalWord.length() > 1) {
                    boolean wordIsReal = dictionary.contains(incidentalWord.toUpperCase());
                    if (!(wordIsReal)) {
                        //System.out.printf("Incidental: %s is not a real word", incidentalWord);
                        return false;
                    }

                    // for incidental at [0] (which is the core word), check it has not already been played
                    int incidentalLength = incidentalWord.length();
                    char[] incidentalLetters = incidentalWord.toCharArray();
                    int reusedSquares = 0;
                    if (index == 0) {
                        for (int wordIndex = 0; wordIndex < incidentalLength; wordIndex++) {
                            int location = incidentalLocations[wordIndex];
                            if (board.getPlayedLocations().contains(location)) {
                                reusedSquares += 1;
                                char letterAtIndex = Character.toUpperCase(board.getSquares()[location].getContents());
                                char letterInSquare = Character.toUpperCase(incidentalLetters[wordIndex]);
                                if (letterInSquare != letterAtIndex) {
                                    return false;
                                }
                            }
                        }
                        // if all the squares in the proposed core word are actually re-used squares, return false
                        // e.g. playing "GRIN" on top of the first four squares of "GRINCH" would return false
                        if (reusedSquares >= incidentalLength) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private Word[] getAllIncidentals(String word, int[] letterLocations, char orientation, Square[] boardSquares) {

        //find in-line incidentals
        Word inlineIncidental = findInlineIncidental(word, letterLocations, orientation, boardSquares);
        word = inlineIncidental.getWord();
        letterLocations = inlineIncidental.getLocations();

        //find perpendicular incidentals
        char[] letters = word.toCharArray();
        Word[] perpendicularIncidentals = findPerpendicularIncidentals(letters, letterLocations, orientation, boardSquares);

        //return array containing all incidentals with core word at position 0
        Word[] allIncidentals = new Word[perpendicularIncidentals.length + 1];
        allIncidentals[0] = inlineIncidental;
        for (int index = 1; index < allIncidentals.length; index++) {
            Word perpInc = perpendicularIncidentals[index-1];
            allIncidentals[index] = perpInc;
        }
        return allIncidentals;
    }

    private Word findInlineIncidental(String word, int[] letterLocations, char orientation, Square[] boardSquares) {
        String prefix = findIncidentalPrefix(letterLocations[0], orientation, boardSquares);
        String suffix = findIncidentalSuffix(letterLocations[word.length() - 1], orientation, boardSquares);
        String incidentalFormed = prefix + word + suffix;
        int newStart = letterLocations[0];
        if (prefix.length() > 0) {
            char[] prefixChars = prefix.toCharArray();
            for (char ignored : prefixChars) {
                newStart = Utils.lastLocation(newStart, orientation);
            }
        }

        int[] newStartCoordinates = Utils.toBoardCoordinates(newStart);
        int[] incidentalSquares = Utils.letterLocations(incidentalFormed, newStartCoordinates[0], newStartCoordinates[1], orientation);
        Word inlineIncidental = new Word();
        inlineIncidental.setWord(incidentalFormed);
        inlineIncidental.setLocations(incidentalSquares);

        return inlineIncidental;
    }

    private Word[] findPerpendicularIncidentals(char[] wordAsLetters, int[] letterLocations,
                                                char orientation, Square[] boardSquares) {
        int length = wordAsLetters.length;
        Word[] perpendicularIncidentals = new Word[length];

        char perpendicularOrientation;
        if (orientation == 'H') perpendicularOrientation = 'V';
        else if (orientation == 'V') perpendicularOrientation = 'H';
        else throw new IllegalArgumentException("Orientation must be either H or V");

        for (int index = 0; index < length; index++) {
            char currentLetter = wordAsLetters[index];
            String prefix = findIncidentalPrefix(letterLocations[index], perpendicularOrientation, boardSquares);
            String suffix = findIncidentalSuffix(letterLocations[index], perpendicularOrientation, boardSquares);
            String incidentalFormed = prefix + currentLetter + suffix;

            if (incidentalFormed.length() > 1) {
                //find the squares used by the perpendicular incidental
                int newStart = letterLocations[index];
                if (prefix.length() > 0) {
                    char[] prefixChars = prefix.toCharArray();
                    for (char letter : prefixChars) {
                        newStart = Utils.lastLocation(newStart, perpendicularOrientation);
                    }
                }

                int[] newStartCoordinates = Utils.toBoardCoordinates(newStart);
                int[] incidentalSquares = Utils.letterLocations(incidentalFormed, newStartCoordinates[0],
                        newStartCoordinates[1], perpendicularOrientation);

                Word perpendicularIncidental = new Word();
                perpendicularIncidental.setWord(incidentalFormed);
                perpendicularIncidental.setLocations(incidentalSquares);

                perpendicularIncidentals[index] = perpendicularIncidental;
            }
        }
        return perpendicularIncidentals;
    }

    private String findIncidentalPrefix(int location, char incidentalDirection, Square[] boardSquares) {

        String prefix = "";
        int currentLocation = location;
        while (true) {
            try {
                currentLocation = Utils.lastLocation(currentLocation, incidentalDirection);
                char contents = boardSquares[currentLocation].getContents();

                if (contents != '_'){
                    prefix = contents + prefix;
                }
                else {
                    //square found is blank
                    break;
                }

            } catch (IndexOutOfBoundsException e) {
                //edge of board reached
                break;
            }
        }
        return prefix;
    }

    private String findIncidentalSuffix(int location, char incidentalDirection, Square[] boardSquares) {

        String suffix = "";
        int currentLocation = location;
        while (true) {
            try {
                currentLocation = Utils.nextLocation(currentLocation, incidentalDirection);
                char contents = boardSquares[currentLocation].getContents();

                if (contents != '_'){
                    suffix += contents;
                }
                else {
                    //next square is blank
                    break;
                }

            } catch (IndexOutOfBoundsException e) {
                //edge of board reached
                break;
            }
        }
        return suffix;
    }
}

