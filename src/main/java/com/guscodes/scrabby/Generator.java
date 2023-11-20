package com.guscodes.scrabby;

import java.util.*;

public class Generator {
    private Board board;
    private DictHandler dictHandler;
    private Set<String> dictionary;
    private Validator validator;
    private Scorer scorer;
    private LetterExtender letEx;
    boolean useExperimentalFeatures;

    private HashMap<Character, Float> letterFrequencies;

    public Generator(Board board, DictHandler dictHandler, Validator validator, Scorer scorer,
                     boolean useExperimentalFeatures) {
        this.board = board;
        this.dictHandler = dictHandler;
        this.dictionary = dictHandler.getDictionary();
        this.validator = validator;
        this.scorer = scorer;
        this.letEx = new LetterExtender(board.getSquares(), dictHandler);
        this.letterFrequencies = dictHandler.getLetterFreqs();
        this.useExperimentalFeatures = useExperimentalFeatures;
    }

    public Set<Word> getSuggestions(String tray) {
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
            // is the word using the least useful letters from the tray first?
            for (Word word : scoredAndValidatedSuggestions) {
                float commonalityScore = 0;
                List<String> trayLettersUsed = word.getTrayLettersUsed();
                for (String letter : trayLettersUsed) {
                    char letterChar = Character.toUpperCase(letter.charAt(0));
                    commonalityScore += letterFrequencies.get(letterChar);
                    //System.out.println("Commonality score of " + word.getWord() + " : " + commonalityScore);
                    //System.out.println(word.getScore());
                }

                word.setRating(Math.round((float) word.getScore() - ((float) 1.5 * commonalityScore)));
            }

            //TODO: each word and the squares it can open up can be added later
        }

        // #########################################################################################################

        long endTime = System.nanoTime();
        float elapsedTimeInSeconds = (endTime - startTime) / 1000000000;
        System.out.printf("%d plays analysed in %f seconds, %d valid plays found\n", suggestedWords.size(),
                                                elapsedTimeInSeconds, scoredAndValidatedSuggestions.size());
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
