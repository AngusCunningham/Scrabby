package com.guscodes.scrabby;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Tests {
    DictHandler dictHandler = new DictHandler();
    Board board = new Board(dictHandler);
    Validator validator = new Validator(board, dictHandler.getDictionary());
    Generator generator = new Generator(board, dictHandler, validator, new Scorer(board));

    public void validatorWorks() {
        System.out.println("Testing validator");
        Word word = new Word("DA", new int[]{97, 98}, 'H');
        if (validator.checkValidity(word) == null) System.out.println("All good");
        else System.out.println("Not good\n");
    }

    public void nextSquareWorks(){
        if (Utils.nextLocation(97, 'V') == 112) System.out.println("nextSquare() working H");
        else System.out.println("nextSquare() failed");
    }

    public void generatorWorks() {
        System.out.println("Testing generator");
        Set<Word> suggestedWords = generator.getSuggestions("DA");

        System.out.println("Suggested words: ");
        for (Word word : suggestedWords) {
            System.out.printf("%s: %s\n", word.getWord(), Arrays.toString(word.getLocations()));
        }

        System.out.println("Before second filter: " + suggestedWords.size());
        if (suggestedWords.size() == 15) System.out.println("Good");
        else System.out.println("Not good");


        int count = 0;
        Set<Word> checkedWords = new HashSet<Word>();
        for (Word word : suggestedWords) {
            Word[] checked = validator.checkValidity(word);
            if (validator.checkValidity(word) != null) {
                System.out.printf("%s: %s\n", checked[0].getWord(), Arrays.toString(checked[0].getLocations()));
                checkedWords.add(checked[0]);
                count += 1;
            }
        }

        for (Word checked : checkedWords) {
            System.out.printf("%s: %s\n", checked.getWord(), Arrays.toString(checked.getLocations()));
        }

        System.out.println("After second filter: " + count);


        //15
    }
}
