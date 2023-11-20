package com.guscodes.scrabby;

import java.util.HashMap;
import java.util.Random;

public class TileBag {
    private HashMap<Character, Integer> lettersRemaining = new HashMap<>();
    private Random random = new Random();

    public TileBag() {
        lettersRemaining.put('A', 9);
        lettersRemaining.put('B', 2);
        lettersRemaining.put('C', 2);
        lettersRemaining.put('D', 4);
        lettersRemaining.put('E', 12);
        lettersRemaining.put('F', 2);
        lettersRemaining.put('G', 3);
        lettersRemaining.put('H', 2);
        lettersRemaining.put('I', 9);
        lettersRemaining.put('J', 1);
        lettersRemaining.put('K', 1);
        lettersRemaining.put('L', 4);
        lettersRemaining.put('M', 2);
        lettersRemaining.put('N', 6);
        lettersRemaining.put('O', 8);
        lettersRemaining.put('P', 2);
        lettersRemaining.put('Q', 1);
        lettersRemaining.put('R', 6);
        lettersRemaining.put('S', 4);
        lettersRemaining.put('T', 6);
        lettersRemaining.put('U', 4);
        lettersRemaining.put('V', 2);
        lettersRemaining.put('W', 2);
        lettersRemaining.put('X', 1);
        lettersRemaining.put('Y', 2);
        lettersRemaining.put('Z', 1);
        lettersRemaining.put('~', 2);
    }

    public char getLetter() throws IllegalStateException {
        String allTiles = "ABCDEFGHIJKLMNOPQRSTUVWXYZ~";
        if (this.isEmpty()) throw new IllegalStateException("Tile bag is empty");
        else {
            while (true) {
                int tileIndex = random.nextInt(27);
                char letter = allTiles.charAt(tileIndex);
                int remainingQuantity = lettersRemaining.get(letter);
                if (remainingQuantity > 0) {
                    int newRemainingQuantity = remainingQuantity - 1;
                    //System.out.println(letter + remainingQuantity + newRemainingQuantity);
                    lettersRemaining.put(letter, newRemainingQuantity);
                    return letter;
                }
            }
        }
    }

    private boolean isEmpty() {
        for (Character tile : lettersRemaining.keySet()) {
            if (lettersRemaining.get(tile) > 0) return false;
        }
        return true;
    }
}
