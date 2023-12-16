package com.guscodes.scrabby.gameitems;

import com.guscodes.scrabby.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileBag {
    private List<Character> lettersRemaining = new ArrayList<>();
    private Random random = new Random();

    public TileBag() {
        for (char letter : Data.LETTER_QUANTITIES.keySet()) {
            int quantity = Data.LETTER_QUANTITIES.get(letter);
            int addedToTileBag = 0;
            while (addedToTileBag < quantity) {
                lettersRemaining.add(letter);
                addedToTileBag += 1;
            }
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
