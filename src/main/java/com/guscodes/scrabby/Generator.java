package com.guscodes.scrabby;

import java.util.*;

public class Generator {
    private Validator validator;
    private Scorer scorer;
    private LetterExtender letEx;
    boolean useExperimentalFeatures;
    private float testParameter;

    public Generator(LetterExtender letterExtender, Validator validator, Scorer scorer,
                     boolean useExperimentalFeatures, double testParameter) {
        this.validator = validator;
        this.scorer = scorer;
        this.useExperimentalFeatures = useExperimentalFeatures;
        this.testParameter = (float) testParameter;
        this.letEx = letterExtender;
    }

    public Set<Word> getSuggestions(String tray) {
        //System.out.println("Scrabby is thinking.....");
        long startTime = System.nanoTime();
        Set<Word> suggestedWords = new HashSet<>();
        Set<String> allTrays = trayVersions(tray);
        for (String trayVersion : allTrays) {
            List<String> trayLetters = new ArrayList<>();
            for (String letter : trayVersion.split("")) {
                trayLetters.add(letter);
            }

            for (int location = 0; location < 225; location++ ) {
                suggestedWords.addAll(letEx.getLegalMovesAnchoredAt(location, trayLetters));
            }
        }

        Set<Word> scoredAndValidatedSuggestions = filterValidateAndScore(suggestedWords);

        // ########################################################################################################

        //EXPERIMENTAL FEATURE ZONE
        if (useExperimentalFeatures) {
            /* FEATURES PLANNED / In Progress:
            - Prefer to save blanks for words scoring more highly - COMPLETE - OPTIMAL MULTIPLIER IS 0
            - Prefer to play words which score more per new tile used
            - Prefer to use high scoring tiles for words which score more highly
            - Prefer to play words which take bonus squares from opponent or block the use of bonus squares
            - Prefer to play words which open up fewer bonus squares for opponent to use
            - Prefer not to play words which can be easily extended with s, ed, er etc right before a triple word
            - Prefer to keep RETAINS tiles in tray when possible
             */

            boolean efficientBlankUsage = false;

            // prefer to only use blanks for high scoring words
            if (efficientBlankUsage) {
                for (Word word : scoredAndValidatedSuggestions) {
                    List<String> newTilesPlaced = word.getTrayLettersUsed();
                    for (String tile : newTilesPlaced) {
                        if (Character.isLowerCase(tile.charAt(0))) {
                            if (word.getScore() < 50) {
                                word.modifyRatingByFactor(0);
                                // analysis complete, using factor of 0 will give a 6% increase in average win liklihood
                            }
                            break;
                        }
                    }
                }
            }
        }

        // #########################################################################################################

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (double) (endTime - startTime) / 1000000000;
        //System.out.printf("%d plays analysed in %f seconds, %d valid plays found\n", suggestedWords.size(),
                                                //elapsedTimeInSeconds, scoredAndValidatedSuggestions.size());
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
            checkedWord.setRating(checkedWord.getScore());
            checkedWords.add(checkedWord);
        }
        return checkedWords;
    }

    private int wordOpensBonuses() {
        return 0;
    }
}
