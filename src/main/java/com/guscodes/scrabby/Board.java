package com.guscodes.scrabby;

import java.util.*;

public class Board {

    //bonus square locations
    private int[] doubleLetters = {3, 11, 36, 38, 45, 52, 59, 92, 96, 98, 102, 108, 116, 122, 126, 128, 132, 165, 172,
            179, 186, 188, 213, 221};
    private int[] tripleLetters = {20, 24, 76, 80, 84, 88, 136, 140, 144, 148, 200, 204};
    private int[] doubleWords = {16, 28, 32, 42, 48, 56, 64, 70, 112, 154, 160, 168, 176, 182, 192, 196, 208};
    private int[] tripleWords = {0, 7, 14, 105, 119, 210, 217, 224};

    //keeping track of squares, words etc
    private Square[] squares = new Square[225];
    private Set<Square> anchorableSquares = new HashSet<>();
    private List<Word> playedWords = new ArrayList<>();
    private List<Square> playedSquares = new ArrayList<>();

    //dependencies
    private DictHandler dictHandler;
    private Validator validator;
    private Scorer scorer = new Scorer(this);

    public Board(DictHandler dictHandler) {
        for (int count = 0; count < 225; count++) {
            this.dictHandler = dictHandler;
            this.validator = new Validator(this, dictHandler.getDictionary());

            Square newSquare = new Square(count);

            if (Utils.arrayContainsInt(doubleLetters, count)) {
                newSquare.setBonus('l');
            }
            else if (Utils.arrayContainsInt(tripleLetters, count)) {
                newSquare.setBonus('L');
            }
            else if (Utils.arrayContainsInt(doubleWords, count)) {
                newSquare.setBonus('w');
            }
            else if (Utils.arrayContainsInt(tripleWords, count)) {
                newSquare.setBonus('W');
            }
            else {
                newSquare.setBonus('O');
            }

            if (count == 112){
                // mark the centre square as anchorable
                newSquare.setAnchorable();
            }

            squares[count] = newSquare;
        }

        for (Square square : squares) {
            int location = square.getLocation();

            if (location > 14) {
                // if square is not on top row
                square.setAbove(squares[Utils.sAbove(location)]);
            }
            if (location < 210) {
                // if square is not on bottom row
                square.setBelow(squares[Utils.sBelow(location)]);
            }
            if (location % 15 != 0) {
                // if square is not on leftmost column
                square.setLeft(squares[Utils.sLeft(location)]);
            }
            if ((location + 1) % 15 != 0) {
                // if square is not on rightmost column
                square.setRight(squares[Utils.sRight(location)]);
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
        return this.playedWords;
    }

    public int[] getAllLocations() {
        int[] allLocations = new int[squares.length];
        for (Square square : squares) {
            int location = square.getLocation();
            allLocations[location] = location;
        }
        return allLocations;
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
        Word[] incidentalsFormed = validator.checkValidity(word);
        if (incidentalsFormed == null) {
            throw new IllegalArgumentException("Suggested play is not valid");
        }
        Word mainWord = incidentalsFormed[0];
        char[] wordLetters = mainWord.getWord().toCharArray();
        int[] letterLocations = mainWord.getLocations();

        for (int index=0; index < wordLetters.length; index++) {
            //add letters to board squares
            squares[letterLocations[index]].setContents(wordLetters[index]);
        }

        int score = scorer.getScore(incidentalsFormed);

        for (Word playedWord : incidentalsFormed) {
            playedWords.add(playedWord);
        }
        for (int location : letterLocations) {
            playedSquares.add(squares[location]);
        }
        //System.out.printf("%s played, scoring %d \n", mainWord.getWord(), score);
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
}

