package com.guscodes.scrabby.newclasses;

import java.util.List;

interface Play {
    String getWord();
    List<Integer> getSquares();
    boolean isVertical();
    int getScore();
    int getRating();
    int getLength();
}
