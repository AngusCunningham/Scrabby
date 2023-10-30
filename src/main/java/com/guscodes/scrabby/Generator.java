package com.guscodes.scrabby;

import java.util.*;

public class Generator {
    private Board board;
    private DictHandler dictHandler;
    private Set<String> dictionary;
    private Validator validator;
    private Scorer scorer;
    private LetterExtender letEx;

    public Generator(Board board, DictHandler dictHandler, Validator validator, Scorer scorer) {
        this.board = board;
        this.dictHandler = dictHandler;
        this.dictionary = dictHandler.getDictionary();
        this.validator = validator;
        this.scorer = scorer;
        this.letEx = new LetterExtender(board, dictHandler);
    }

    public Set<Word> getSuggestions(String tray) {
        long startTime = System.nanoTime();
        Set<Word> suggestedWords = new HashSet<>();
        Set<String> allTrays = trayVersions(tray);
        for (String trayVersion : allTrays) {
            //System.out.println("Tray version is " + trayVersion);
            List<String> trayLetters = new ArrayList<>();
            for (String letter : trayVersion.split("")) {
                trayLetters.add(letter);
            }

            Set<Integer> allPossibleStarts = new HashSet<>();

            for (int location : board.getAccessibleLocations()) {
                allPossibleStarts.addAll(getAllStartLocationsFrom(location));
            }

            for (int location : allPossibleStarts) {
                suggestedWords.addAll(letEx.getLegalMovesAnchoredAt(location, trayLetters));
            }
        }

        Set<Word> scoredAndValidatedSuggestions = filterValidateAndScore(suggestedWords);

        //TODO: each word and the squares it can open up can be added later
        //TODO: does the word take useful letters from the tray which could be kept for later?

        long endTime = System.nanoTime();
        long elapsedTimeInSeconds = (endTime - startTime) / 1000000000;
        System.out.printf("%d plays analysed in %d seconds\n", suggestedWords.size(), elapsedTimeInSeconds);
        return scoredAndValidatedSuggestions;
    }

    private Set<String> trayVersions(String tray) {
        Set<String> trayVersions = new HashSet<>();
        int blankCount = 0;
        for (String letter : tray.split("")) {
            if (letter.equals("~")) {
                blankCount += 1;
                tray = tray.replace(letter, "");
            }
        }

        if (blankCount == 0) {
            trayVersions.add(tray);
        }

        if (blankCount > 0) {
            for (char letter : Utils.ALPHABET) {
                String newTray = tray + Character.toLowerCase(letter);
                trayVersions.add(newTray);
            }
        }

        if (blankCount > 1) {
            Set<String> copyOfTrayVersions = new HashSet<>(trayVersions);
            for (String copyOfTray : copyOfTrayVersions) {
                for (char letter : Utils.ALPHABET) {
                    String newTray = copyOfTray + Character.toLowerCase(letter);
                    trayVersions.add(newTray);
                }
            }
        }

        if (blankCount > 2) {
            throw new IllegalStateException("Too many blanks in the game!!");
        }

        return trayVersions;
    }

    private Set<Integer> getAllStartLocationsFrom(int anchorLocation) {
        Set<Integer> allStartLocations = new HashSet<>();
        allStartLocations.add(anchorLocation);
        int thisLocation = anchorLocation;
        while (true) {
            try {
                thisLocation = Utils.lastLocation(thisLocation, 'H');
                allStartLocations.add(thisLocation);
            }
            catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        thisLocation = anchorLocation;
        while (true) {
            try {
                thisLocation = Utils.lastLocation(thisLocation, 'V');
                allStartLocations.add(thisLocation);
            }
            catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        return allStartLocations;
    }


    private Set<Word> filterValidateAndScore(Set<Word> potentialWords) {
        Set<Word> checkedWords = new HashSet<>();
        for (Word word : potentialWords) {
            Word[] checked = validator.checkValidity(word);
            if (validator.checkValidity(word) == null) {
                continue;
            }
            Word checkedWord = checked[0];
            checkedWord.setOrientation(word.getOrientation());
            checkedWord.setScore(scorer.getScore(checked));
            checkedWord.setRating(checkedWord.getScore());
            checkedWords.add(checkedWord);
        }
        return checkedWords;
    }

    private int wordOpensBonuses() {
        return 0;
    }
}
