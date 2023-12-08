package com.guscodes.scrabby;

import java.util.*;

public class Generator {
    private Validator validator;
    private Scorer scorer;
    private LetterExtender letEx;
    boolean useExperimentalFeatures;
    private float testParameter;
    boolean verbose;
    Board board;
    Strategist strategist;

    public Generator(LetterExtender letterExtender, Validator validator, Scorer scorer,
                     boolean useExperimentalFeatures, double testParameter, boolean verbose, Board board) {
        this.validator = validator;
        this.scorer = scorer;
        this.useExperimentalFeatures = useExperimentalFeatures;
        this.testParameter = (float) testParameter;
        this.letEx = letterExtender;
        this.verbose = verbose;
        this.board = board;
        this.strategist = new Strategist(board, scorer);
    }

    public Set<Word> getSuggestions(String tray, Board board) {
        int tilesDown = board.getPlayedLocations().size();

        if (verbose) {
            System.out.println("Scrabby is thinking.....");
        }

        long startTime = System.nanoTime();
        Set<Word> suggestedWords = new HashSet<>();
        Set<String> allTrays = trayVersions(tray);
        for (String trayVersion : allTrays) {
            List<String> trayLetters = new ArrayList<>();
            for (String letter : trayVersion.split("")) {
                trayLetters.add(letter);
            }

            for (int location = 0; location < 225; location++) {
                suggestedWords.addAll(letEx.getAllLegalMovesFrom(location, trayLetters));
            }
        }

        Set<Word> scoredAndValidatedSuggestions = filterValidateAndScore(suggestedWords);

        // ########################################################################################################
        // HEURISTIC SCORE IMPROVEMENTS

        // experimental strategy
        //System.out.println("Experimental features: " + useExperimentalFeatures);
        if (useExperimentalFeatures) {
            //System.out.println("Using experimental features");
            scoredAndValidatedSuggestions = strategist.getStrategicRatings(scoredAndValidatedSuggestions, tray, testParameter);
        }

        // #########################################################################################################

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1000000000;
        if (verbose) {
            System.out.printf("%d plays analysed in %f seconds, %d valid plays found\n", suggestedWords.size(),
                    elapsedTimeInSeconds, scoredAndValidatedSuggestions.size());
        }
        return scoredAndValidatedSuggestions;
    }

    private Set<String> trayVersions(String tray) throws IllegalStateException {
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
            checkedWords.add(checkedWord);
        }
        return checkedWords;
    }
}
