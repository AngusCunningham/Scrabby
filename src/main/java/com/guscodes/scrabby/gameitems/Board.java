package com.guscodes.scrabby.gameitems;

import com.guscodes.scrabby.*;
import com.guscodes.scrabby.analysis.Validator;
import com.guscodes.scrabby.analysis.Scorer;

import java.util.*;

public class Board {
    //keeping track of squares, words etc
    private Square[] squares = new Square[225];
    private Set<Square> anchorableSquares = new HashSet<>();
    private Set<Word> playedWords = new HashSet<>();
    private List<Square> playedSquares = new ArrayList<>();

    private List<Board> states = new ArrayList<>();
    private Map<Character, Integer> unseenTiles = new HashMap<>(Data.LETTER_QUANTITIES);
    boolean allowUndo;

    private Validator validator;
    private Scorer scorer;
    boolean verbose;

    public Board(Validator validator, Scorer scorer, boolean verbose, boolean allowUndo) {
        this.validator = validator;
        this.scorer = scorer;
        this.verbose = verbose;
        this.allowUndo = allowUndo;

        for (int squareLocation = 0; squareLocation < 225; squareLocation++) {
            Square newSquare = new Square(squareLocation);

            if (Data.DLS_LOCATIONS.contains(squareLocation)) {
                newSquare.setBonus('l');
            }
            else if (Data.TLS_LOCATIONS.contains(squareLocation)) {
                newSquare.setBonus('L');
            }
            else if (Data.DWS_LOCATIONS.contains(squareLocation)) {
                newSquare.setBonus('w');
            }
            else if (Data.TWS_LOCATIONS.contains(squareLocation)) {
                newSquare.setBonus('W');
            }
            else {
                newSquare.setBonus('O');
            }

            if (squareLocation == 112){
                // mark the centre square as anchorable
                newSquare.setAnchorable();
            }

            squares[squareLocation] = newSquare;
        }

        for (Square square : squares) {
            int location = square.getLocation();

            if (location > 14) {
                // if square is not on top row
                square.setAbove(squares[Utils.locationAbove(location)]);
            }
            if (location < 210) {
                // if square is not on bottom row
                square.setBelow(squares[Utils.locationBelow(location)]);
            }
            if (location % 15 != 0) {
                // if square is not on leftmost column
                square.setLeft(squares[Utils.locationToLeft(location)]);
            }
            if ((location + 1) % 15 != 0) {
                // if square is not on rightmost column
                square.setRight(squares[Utils.locationToRight(location)]);
            }
        }
        refresh();
    }

    public void show(char toShow) {
        System.out.printf("    ");
        for (Integer i = 0; i < 15; i++) {
            String colNum = i + " ";
            if (i < 10) {
                colNum += " ";
            }
            System.out.printf(colNum);
        }
        System.out.println();
        for (Integer line = 0; line < 15; line ++) {
            String row = line + "  ";
            if (line < 10) {
                row += " ";
            }
            for (int column = 0; column < 15; column ++) {
                int position = (line * 15) + column;
                if (toShow == 'L') {
                    row += squares[position].getContents();
                }
                if (toShow == 'B') {
                    row += squares[position].getBonus();
                }
                if (toShow == 'A') {
                    if (squares[position].isAnchorable()) {
                        row += 'A';
                    }
                    else {
                        row += 'N';
                    }
                }
                if (toShow == 'N') {
                    int number = squares[position].getLocation();
                    row += number;
                    if (number < 10) {
                        row += "  ";
                    }
                    else if (number < 100) {
                        row += " ";
                    }
                }
                row += "  ";
            }
            System.out.println(row);
        }
    }

    public List<Word> getPlayedWords() {
        return new ArrayList<>(this.playedWords);
    }

    public Set<Integer> getPlayedLocations() {
        Set<Integer> playedLocations = new HashSet<>();
        for (int index = 0; index < playedSquares.size(); index++) {
            playedLocations.add(playedSquares.get(index).getLocation());
        }
        return playedLocations;
    }

    public Set<Integer> getAccessibleLocations() {
        Set<Integer> accessibleLocations = new HashSet<>();
        for (Square square : anchorableSquares) {
            accessibleLocations.add(square.getLocation());
        }
        return accessibleLocations;
    }

    public Square[] getSquares() {
        return squares;
    }

    public void addWord(Word word) throws IllegalArgumentException {
        Word[] incidentalsFormed = validator.validate(word, this);
        if (incidentalsFormed == null) {
            throw new IllegalArgumentException("Suggested play is not valid");
        }

        if (allowUndo) this.states.add(deepCopy());

        Word mainWord = incidentalsFormed[0];
        char[] wordLetters = mainWord.getWord().toCharArray();
        int[] letterLocations = mainWord.getLocations();

        for (int index=0; index < wordLetters.length; index++) {
            //add letters to board squares
            char tileChar = wordLetters[index];
            squares[letterLocations[index]].setContents(tileChar);

            // update unseen tile quantities
            char tile;
            if (Character.isLowerCase(tileChar)) tile = '~';
            else tile = tileChar;

            unseenTiles.put(tile, unseenTiles.get(tile) - 1);
        }

        int score = scorer.getTotalScore(incidentalsFormed, this);

        for (Word playedWord : incidentalsFormed) {
            playedWords.add(playedWord);
        }

        for (int location : letterLocations) {
            playedSquares.add(squares[location]);
        }

        if (verbose) {
            System.out.printf("%s played, scoring %d \n", mainWord.getWord(), score);
        }

        refresh();
    }

    public void refresh() {
        Set<Square> anchorableSquaresNow = new HashSet<>();
        for (Square square : squares) {
            if (square.isAnchorable()) {
                anchorableSquaresNow.add(square);
            }
        }
        this.anchorableSquares = anchorableSquaresNow;
    }

    private Board deepCopy() {
        Board boardDeepCopy = new Board(validator, scorer, verbose, allowUndo);

        for (Square square : this.squares) {
            boardDeepCopy.squares[square.getLocation()].setContents(square.getContents());
            if (square.isAnchorable()) {
                boardDeepCopy.squares[square.getLocation()].setAnchorable();
            }
        }

        for (Word word : this.playedWords) if (word != null) {
            Word playedWordCopy = new Word();
            playedWordCopy.setScore(word.getScore());
            playedWordCopy.setWord(word.getWord());
            playedWordCopy.setLocations(word.getLocations());
            playedWordCopy.setOrientation(word.getOrientation());
            boardDeepCopy.playedWords.add(word);
        }

        for (Square square : this.anchorableSquares) {
            boardDeepCopy.anchorableSquares.add(boardDeepCopy.squares[square.getLocation()]);
        }

        for(Square square : this.playedSquares) {
            boardDeepCopy.playedSquares.add(boardDeepCopy.squares[square.getLocation()]);
        }

        for (Board board : this.states) {
            boardDeepCopy.states.add(board);
        }

        return boardDeepCopy;
    }

    public Board getPreviousState() {
        System.out.printf("There are currently %s states saved\n", states.size());
        Board previousState = states.get(states.size() - 1);
        System.out.println("Previous board to revert to: ");
        previousState.show('L');
        return states.get(states.size() - 1);
    }

    public Map<Character, Integer> getUnseenTiles() {
        return this.unseenTiles;
    }
}

