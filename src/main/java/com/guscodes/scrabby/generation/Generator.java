package com.guscodes.scrabby.generation;

import com.guscodes.scrabby.analysis.Validator;
import com.guscodes.scrabby.Data;
import com.guscodes.scrabby.analysis.Scorer;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.gameitems.Board;

import java.util.*;

public class Generator {
    private Validator validator;
    private Scorer scorer;
    private MoveFinder moveFinder;
    boolean useExperimentalFeatures;
    private float testParameter;
    boolean verbose;
    Board board;
    Strategist strategist;

    public Generator(MoveFinder moveFinder, Validator validator, Scorer scorer,
                     boolean useExperimentalFeatures, double testParameter, boolean verbose) {
        this.validator = validator;
        this.scorer = scorer;
        this.useExperimentalFeatures = useExperimentalFeatures;
        this.testParameter = (float) testParameter;
        this.moveFinder = moveFinder;
        this.verbose = verbose;
        this.strategist = new Strategist();
    }

    public Set<Word> getSuggestions(String tray, Board board) {
        this.board = board;

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
                suggestedWords.addAll(moveFinder.getAllMovesFrom(location, trayLetters, board.getSquares()));
            }
        }

        Set<Word> scoredAndValidatedSuggestions = filterValidateAndScore(suggestedWords);

        // ########################################################################################################
        // HEURISTIC SCORE IMPROVEMENTS

        // experimental strategy
        //System.out.println("Experimental features: " + useExperimentalFeatures);
        if (useExperimentalFeatures) {
            //System.out.println("Using experimental features");
            scoredAndValidatedSuggestions = strategist.getStrategicRatings(scoredAndValidatedSuggestions, tray, board, testParameter);
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
            for (char letter : Data.ALPHABET) {
                String newTray = tray + Character.toLowerCase(letter);
                trayVersions.add(newTray);
            }
        }

        if (blankCount > 1) {
            Set<String> copyOfTrayVersions = new HashSet<>(trayVersions);
            for (String copyOfTray : copyOfTrayVersions) {
                for (char letter : Data.ALPHABET) {
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
            Word[] checked = validator.validate(word, board);
            if (checked == null) {
                // word is invalid
                continue;
            }
            Word checkedWord = checked[0];
            checkedWord.setOrientation(word.getOrientation());
            checkedWord.setScore(scorer.getTotalScore(checked, board));
            checkedWords.add(checkedWord);
        }
        return checkedWords;
    }
}
