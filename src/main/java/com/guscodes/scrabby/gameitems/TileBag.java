package com.guscodes.scrabby.gameitems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileBag {
    private List<Character> lettersRemaining = new ArrayList<>();
    private Random random = new Random();

    public TileBag() {
        lettersRemaining.add('X');
        lettersRemaining.add('Z');
        lettersRemaining.add('Q');
        lettersRemaining.add('J');
        lettersRemaining.add('K');

        for (int i = 0; i < 2; i++) {
            lettersRemaining.add('B');
            lettersRemaining.add('C');
            lettersRemaining.add('M');
            lettersRemaining.add('F');
            lettersRemaining.add('H');
            lettersRemaining.add('P');
            lettersRemaining.add('V');
            lettersRemaining.add('W');
            lettersRemaining.add('Y');
            lettersRemaining.add('~');
        }

        for (int i = 0; i < 3; i++) {
            lettersRemaining.add('G');
        }

        for (int i = 0; i < 4; i++) {
            lettersRemaining.add('D');
            lettersRemaining.add('L');
            lettersRemaining.add('U');
            lettersRemaining.add('S');
        }

        for (int i = 0; i < 6; i++) {
            lettersRemaining.add('R');
            lettersRemaining.add('T');
            lettersRemaining.add('N');
        }

        for (int i = 0; i < 8; i++) {
            lettersRemaining.add('O');
        }

        for (int i = 0; i < 9; i++) {
            lettersRemaining.add('A');
            lettersRemaining.add('I');
        }

        for (int i = 0; i < 12; i++) {
            lettersRemaining.add('E');
        }
    }

    public char getLetter() throws IllegalStateException {
        if (lettersRemaining.size() == 0) throw new IllegalStateException("Tile bag is empty");
        else {
            int tileIndex = random.nextInt(lettersRemaining.size());
            char letter = lettersRemaining.get(tileIndex);
            lettersRemaining.remove(tileIndex);
            return letter;
        }
    }

    public void returnTileToBag(char letter) {
        lettersRemaining.add(letter);
    }
}
