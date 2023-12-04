package com.guscodes.scrabby;

import java.util.*;

public class Generator {
    private Validator validator;
    private Scorer scorer;
    private LetterExtender letEx;
    boolean useExperimentalFeatures;
    private float testParameter;
    boolean verbose = false;

    public Generator(LetterExtender letterExtender, Validator validator, Scorer scorer,
                     boolean useExperimentalFeatures, double testParameter) {
        this.validator = validator;
        this.scorer = scorer;
        this.useExperimentalFeatures = useExperimentalFeatures;
        this.testParameter = (float) testParameter;
        this.letEx = letterExtender;
    }

    public Set<Word> getSuggestions(String tray) {
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
            - Prefer to play words which score more per new tile used - IN PROGRESS
            - Prefer to use high scoring tiles for words which score more highly
            - Prefer to play words which take bonus squares from opponent or block the use of bonus squares
            - Prefer to play words which open up fewer bonus squares for opponent to use
            - Prefer not to play words which can be easily extended with s, ed, er etc right before a triple word
            - Prefer to keep RETAINS tiles in tray when possible
             */

            boolean efficientBlankUsage = true;
            boolean preferMoreScorePerNewTile = false;
            boolean preferRETAINSTray = false;

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

            if (preferMoreScorePerNewTile) {
                for (Word word : scoredAndValidatedSuggestions) {
                    int numberOfNewTilesUsed = word.getTrayLettersUsed().size();
                    double scorePerNewTile = (double) word.getScore() / numberOfNewTilesUsed;
                    if (testParameter == 0) {
                        word.modifyRatingByFactor(1);
                    }
                    else {
                        word.modifyRatingByFactor(scorePerNewTile * testParameter);
                    }
                }
            }

            if (preferRETAINSTray) {
                char[] trayLetters = tray.toCharArray();
                for (Word word : scoredAndValidatedSuggestions) {
                    double leaveScore = 10;

                    String remainingTray = tray;
                    List<String> tilesUsedFromTray = word.getTrayLettersUsed();

                    // remove used letters from the tray
                    for (String letter : tilesUsedFromTray) {
                        remainingTray.replaceFirst(letter, "");
                        if (Character.isLowerCase(letter.charAt(0))) {
                            remainingTray.replaceFirst("~", "");
                        }
                    }
                    if (verbose) {
                        System.out.printf("Playing %s would leave behind %s from an original tray of %s\n", word.getWord(), remainingTray, tray);
                    }
                    // calculate the quantity of each letter remaining in the tray
                    HashMap<Character, Integer> letterQuantities = new HashMap<>();
                    for (char letter : remainingTray.toCharArray()) {
                        if (letterQuantities.containsKey(letter)) {
                            letterQuantities.put(letter, letterQuantities.get(letter) + 1);
                        }
                        else {
                            letterQuantities.put(letter, 1);
                        }
                    }
                }
            }
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

    public double similarity(String sample, String control) {
        String longer = sample;
        String shorter = control;
        if (sample.length() < control.length()) {
            longer = control;
            shorter = sample;
        }

        int longerLength = longer.length();

        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private int editDistance(String string1, String string2) {
        for (String letter : string1.split("")) {
            if (Character.isLowerCase(letter.charAt(0))) {
                string1 = string1.replaceFirst(letter, "~");
            }
        }

        for (String letter : string2.split("")) {
            if (Character.isLowerCase(letter.charAt(0))) {
                string2 = string2.replaceFirst(letter, "~");
            }
        }

        string1 = string1.toUpperCase();
        string2 = string2.toUpperCase();

        int[] costs = new int[string2.length() + 1];

        // for each letter in string 1...
        for (int positionInString1 = 0; positionInString1 <= string1.length(); positionInString1++) {
            int lastValue = positionInString1;

            // for each letter in string 2...
            for (int positionInString2 = 0; positionInString2 <= string2.length(); positionInString2++) {
                if (positionInString1 == 0)
                    costs[positionInString2] = positionInString2;

                else {
                    if (positionInString2 > 0) {
                        int newValue = costs[positionInString2 - 1];

                        if (string1.charAt(positionInString1 - 1) != string2.charAt(positionInString2 - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[positionInString2]) + 1;
                        }

                        costs[positionInString2 - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }

            if (positionInString1 > 0)
                costs[string2.length()] = lastValue;
        }
        return costs[string2.length()];
    }

}
