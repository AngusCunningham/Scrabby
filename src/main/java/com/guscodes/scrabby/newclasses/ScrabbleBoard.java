package com.guscodes.scrabby.newclasses;

import com.guscodes.scrabby.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ScrabbleBoard implements Board{
    private char squares[] = new char[225];
    private Set<Integer> playedSquares = new HashSet<>();
    private Set<Integer> anchorSquares = new HashSet<>(); // any played word must use at least one anchor square
    private Set<Integer> startSquares = new HashSet<>(); // words can start anywhere in a row or column before an anchor
    private Set<Play> playedWords = new HashSet<>();
    private List<Board> previousStates = new ArrayList<>();

    private Validator validator;

    ScrabbleBoard() {
        for (int i = 0; i < 225; i++) {
            squares[i] = Data.EMPTY_SQUARE;
            if (i == 112) {
                anchorSquares.add(i);
            }
            if ((i % 7 == 0 || (i > 104)) && i != 0 && i < 113) {
                startSquares.add(i);
            }
        }
    }


    @Override
    public void makePlay(Play play) {
        // play the word
        String word;
        List<Integer> wordSquares;
        if (validator.isValid(play, this)) {
            word = play.getWord();
            wordSquares = play.getSquares();
            for (int index = 0; index < wordSquares.size(); index++) {
                int squareToPlay = wordSquares.get(index);
                if (squares[squareToPlay] == Data.EMPTY_SQUARE) {
                    squares[squareToPlay] = word.charAt(index);
                    playedSquares.add(index);
                }
            }
            playedWords.add(play);
        }
        else {
            String exceptionString = "Play " + play.getWord() + " on " + play.getSquares().toString() + " is invalid";
            throw new IllegalArgumentException(exceptionString);
        }
    }

    @Override
    public Board getPreviousState() {
        return previousStates.get(previousStates.size() - 1);
    }

    @Override
    public Set<Integer> getPlayedSquares() {
        return new HashSet<>(playedSquares);
    }

    @Override
    public Set<Play> getPlayedWords() {
        return new HashSet<>(playedWords);
    }

    @Override
    public Set<Integer> getAnchorSquares() {
        return new HashSet<>(anchorSquares);
    }

    @Override
    public Set<Integer> getStartSquares() {
        return new HashSet<>(startSquares);
    }

    @Override
    public char[] getSquares() {
        char[] boardSquares = new char[225];
        for (int i=0; i<225;i++) {
            boardSquares[i] = squares[i];
        }
        return boardSquares;
    }
}
