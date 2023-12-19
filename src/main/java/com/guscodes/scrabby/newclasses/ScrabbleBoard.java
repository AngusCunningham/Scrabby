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

    ScrabbleBoard() {
        for (int i = 0; i < 225; i++) {
            squares[i] = Data.EMPTY_SQUARE;
            if (i == 112) {
                anchorSquares.add(i);
            }
            if ((i % 7 == 0 || (i < 120 && i > 104)) && i != 0) {
                startSquares.add(i);
            }
        }
    }


    @Override
    public void makePlay(Play play) {
        // play the word
        List<Integer> wordSquares = play.getSquares();

        for (int square : wordSquares) {
            if (squares[square] != Data.EMPTY_SQUARE) {
                squares[square] = word[i];
            }
        }

        String word = play.getWord();
        // add the word to the playedWords set
        // add the squares to the played squares set
        // update the anchorSquares and startSquares lists
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
        char[] boardSquares = new char[255];
        for (int i=0; i<225;i++) {
            boardSquares[i] = squares[i];
        }
        return boardSquares;
    }
}
