package com.guscodes.scrabby.newclasses;

import java.util.Set;

interface Board {
    void makePlay(Play play) throws IllegalArgumentException;
    Board getPreviousState();

    char[] getSquares();
    Set<Integer> getPlayedSquares();
    Set<Play> getPlayedWords();
    Set<Integer> getAnchorSquares();
    Set<Integer> getStartSquares();
}
