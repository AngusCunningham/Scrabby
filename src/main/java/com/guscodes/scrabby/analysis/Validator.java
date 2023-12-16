package com.guscodes.scrabby.analysis;

import com.guscodes.scrabby.gameitems.Square;
import com.guscodes.scrabby.Utils;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.gameitems.Board;
import com.guscodes.scrabby.lexicon.WordHandler;

import java.util.*;

public class Validator {
    private Board board;
    private final Set<String> dictionary;
    public Validator(WordHandler wordHandler) {
        this.dictionary = wordHandler.getDefinedDictionary().keySet();
    }

    public Word[] validate(Word word, Board board) {
        /*
        Returns the incidentals which would be created if the word was played to the board
        The first incidental will be the word itself, extended if it made any inline incidentals
        Will return null if the word is invalid
        */

        this.board = board;

        if (word == null || word.getWord() == null || word.getWord().equals("") || word.getLocations() == null) {
            return null;
        }

        //check that the word uses an accessible square to attach to other words
        int[] locations = word.getLocations();
        if (!(usesAccessibleSquare(locations))){
            return null;
        }

        // find out about which squares are blanks in the word
        String inputWord = word.getWord();
        Set<Integer> blankLocations = new HashSet<>();
        for (int index = 0; index < inputWord.length(); index++){
            if (Character.isLowerCase(inputWord.charAt(index))) {
                blankLocations.add(locations[index]);
            }
        }

        // check that the incidental created inline is a real word
        String plainWord = inputWord.toUpperCase();
        char orientation = word.getOrientation();
        Square[] currentBoardSquares = board.getSquares();

        Word inlineIncidental = findInlineIncidental(plainWord, locations, orientation, currentBoardSquares);
        if (! inlineIncidentalIsValid(inlineIncidental)) {
            return null;
        }

        // check that any perpendicular incidentals created are real words
        String inlineWord = inlineIncidental.getWord();
        locations = inlineIncidental.getLocations();
        char[] letters = inlineWord.toCharArray();
        Word[] perpendicularIncidentals = findPerpendicularIncidentals(letters, locations, orientation,
                                                                                            currentBoardSquares);
        if (! perpendicularIncidentalsAreValid(perpendicularIncidentals)) {
            return null;
        }

        // create a single array containing all incidentals
        Word[] allIncidentals = new Word[perpendicularIncidentals.length + 1];
        allIncidentals[0] = inlineIncidental;
        for (int index = 1; index < allIncidentals.length; index++) {
            Word perpendicularIncidental = perpendicularIncidentals[index-1];
            allIncidentals[index] = perpendicularIncidental;
        }

        // add blanks back into all the incidentals at their correct locations
        allIncidentals = blankReAdd(allIncidentals, blankLocations);

        return allIncidentals;
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

    private boolean perpendicularIncidentalsAreValid(Word[] perpendicularIncidentals) {
        // for each occurring incidental word, check that it is a real word, and it is not using only played squares
        for (int index = 0; index < perpendicularIncidentals.length; index++) {
            Word incidental = perpendicularIncidentals[index];

            // perpendicularIncidentals other than [0] might be null if no incidental occurred at the selected index
            if (incidental != null) {
                String incidentalWord = incidental.getWord();
                if (incidentalWord.length() > 1 && (! dictionary.contains(incidentalWord.toUpperCase()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean inlineIncidentalIsValid(Word inlineIncidental) {
        String inlineWord = inlineIncidental.getWord();

        if (!(dictionary.contains(inlineWord))) {
            //System.out.printf("Incidental: %s is not a real word", incidentalWord);
            return false;
        }

        int[] locations = inlineIncidental.getLocations();

        // check it has not already been played
        int incidentalLength = inlineWord.length();
        char[] incidentalLetters = inlineWord.toCharArray();
        int reusedSquares = 0;
        for (int wordIndex = 0; wordIndex < incidentalLength; wordIndex++) {
            int location = locations[wordIndex];
            if (board.getPlayedLocations().contains(location)) {
                reusedSquares += 1;
                char letterOnBoard = Character.toUpperCase(board.getSquares()[location].getContents());
                char letterInWord = Character.toUpperCase(incidentalLetters[wordIndex]);
                if (letterInWord != letterOnBoard) {
                    return false;
                }
            }
            // if all the squares in the proposed core word are actually re-used squares, return false
            // e.g. playing "GRIN" on top of the first four squares of "GRINCH" would return false
            if (reusedSquares >= incidentalLength) {
                return false;
            }
        }
        return true;
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
                    for (char ignored : prefixChars) {
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
            currentLocation = Utils.lastLocation(currentLocation, incidentalDirection);
            if (currentLocation < 0) {
                // edge of board exceeded
                break;
            }

            char contents = boardSquares[currentLocation].getContents();

            if (contents != '_'){
                prefix = contents + prefix;
            }
            else {
                // square found is blank
                break;
            }
        }
        return prefix;
    }

    private String findIncidentalSuffix(int location, char incidentalDirection, Square[] boardSquares) {

        String suffix = "";
        int currentLocation = location;
        while (true) {
            currentLocation = Utils.nextLocation(currentLocation, incidentalDirection);
            if (currentLocation == -1) {
                //edge of board exceeded
                break;
            }

            char contents = boardSquares[currentLocation].getContents();

            if (contents != '_'){
                suffix += contents;
            }
            else {
                // next square is blank
                break;
            }
        }
        return suffix;
    }

    private Word[] blankReAdd(Word[] incidentalsCreated, Set<Integer> blankLocations) {
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
}

