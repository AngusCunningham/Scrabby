package com.guscodes.scrabby.newclasses;

import java.util.Set;

interface UserInterface {
    void showBoard(Board board);
    void showBonuses(Board board);
    void showAnchors(Board board);
    void showStarts(Board board);
    void showRecommendations(Set<Play> recommendations);
}
