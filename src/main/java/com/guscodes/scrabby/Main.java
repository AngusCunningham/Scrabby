package com.guscodes.scrabby;

import java.util.*;

public class Main {
    Scanner scanner = new Scanner(System.in);

    public static void main (String[] args) {
        //Dependencies
        Main mainThread = new Main();
        DictHandler dictHandler = new DictHandler();

        String welcomeString = "\nWelcome to Scrabby, your scrabble word finder\n";
        System.out.println(welcomeString);

        // check if suggesting mode or playing mode
        String mode = mainThread.getModeFromUser();
        if (mode.equals("D")) {
            //todo: refilling tray immediately after clearing letters from it, to have accurate minus scores in win/pass conditions

            int maxIterations = mainThread.getIterationCountFromUser();

            int controlTotalScore = 0;
            int gamesWonByControl = 0;
            int testTotalScore = 0;
            int gamesWonByTest = 0;
            int drawnGames = 0;

            int iterationsPlayed = 0;
            while (iterationsPlayed < maxIterations) {
                Board board = new Board(dictHandler);
                Scorer scorer = new Scorer(board);
                Validator validator = new Validator(board, dictHandler.getDictionary());

                Generator controlSubject = new Generator(board, dictHandler, validator, scorer, false);
                Generator testSubject = new Generator(board, dictHandler, validator, scorer, true);

                int controlGameScore = 0;
                int testGameScore = 0;

                TileBag tileBag = new TileBag();

                String controlTray = "";
                String testTray = "";

                int consecutiveTestScoresZero = 0;
                int consecutiveControlScoresZero = 0;

                while (true) {
                    //System.out.println(controlTray);
                    // control plays first to give max advantage
                    // while tray is not full, get random letter from bag
                    //System.out.println("\n\nControl subject starts playing");
                    int controlLettersToTake = Utils.MAX_TRAY_SIZE - controlTray.length();
                    int controlLettersTaken = 0;
                    while (controlLettersTaken < controlLettersToTake) {
                        try {
                            controlTray += tileBag.getLetter();
                            controlLettersTaken += 1;
                        }
                        catch (IllegalStateException e) {
                            // tile bag is empty
                            break;
                        }
                    }

                    //System.out.println("Control Tray: " + controlTray);
                    if (controlTray.length() == 0){
                        // game is over, subtract Test's remaining tile scores from their total score
                        char[] testRemainingTiles = testTray.toCharArray();
                        int testMinusScore = 0;
                        for (char tile : testRemainingTiles) {
                            testMinusScore += scorer.getLetterScore(tile);
                        }

                        testGameScore -= testMinusScore;
                        break;
                    }

                    Set<Word> controlSuggestions = controlSubject.getSuggestions(controlTray);
                    if (controlSuggestions.size() == 0) {
                        // if there are no words possible from the current tray/board
                        //todo: return tiles to bag and skip turn
                        for (char tile : controlTray.toCharArray()) {
                            tileBag.returnTileToBag(tile);
                        }
                        controlTray = "";
                        consecutiveControlScoresZero += 1;
                    }

                    else {
                        List<Word> sortedControlSuggestions = Utils.sortWordsByRating(controlSuggestions);
                        Word bestControlPlay = sortedControlSuggestions.get(sortedControlSuggestions.size() - 1);
                        board.addWord(bestControlPlay);
                        System.out.println("Word rating: " + bestControlPlay.getRating());

                        List<String> controlTrayLettersUsed = bestControlPlay.getTrayLettersUsed();
                        for (String letter : controlTrayLettersUsed) {
                            controlTray = controlTray.replaceFirst(letter, "");
                            if (Character.isLowerCase(letter.charAt(0))) {
                                controlTray = controlTray.replaceFirst("~", "");
                            }
                        }

                        controlGameScore += bestControlPlay.getScore();
                        consecutiveControlScoresZero = 0;
                    }


                    // #########################################################################################
                    // test plays second
                    //System.out.println("\n\nTest subject starts playing");
                    int testLettersToTake = Utils.MAX_TRAY_SIZE - testTray.length();
                    int testLettersTaken = 0;
                    while (testLettersTaken < testLettersToTake) {
                        try {
                            testTray += tileBag.getLetter();
                            testLettersTaken += 1;
                        }
                        catch (IllegalStateException e) {
                            // tile bag is empty
                            break;
                        }
                    }
                    //System.out.println("Test Tray: " + testTray);

                    if (testTray.length() == 0) {
                        // game is over, subtract control's remaining tile scores from their total score
                        char[] controlRemainingTiles = controlTray.toCharArray();
                        int controlMinusScore = 0;
                        for (char tile : controlRemainingTiles) {
                            controlMinusScore += scorer.getLetterScore(tile);
                        }

                        controlGameScore -= controlMinusScore;
                        break;
                    }

                    Set<Word> testSuggestions = testSubject.getSuggestions(testTray);
                    if (testSuggestions.size() == 0) {
                        // if there are no words possible from the current tray/board
                        //todo: return tiles to bag and skip turn
                        for (char tile : testTray.toCharArray()) {
                            tileBag.returnTileToBag(tile);
                        }
                        consecutiveTestScoresZero += 1;

                        testTray = "";

                    }
                    else {
                        List<Word> sortedTestSuggestions = Utils.sortWordsByRating(testSuggestions);
                        Word bestTestPlay = sortedTestSuggestions.get(sortedTestSuggestions.size() - 1);
                        Word highestScoringTestPlay = null;
                        int highestScore = 0;
                        for (Word word : sortedTestSuggestions) {
                            if (word.getScore() > highestScore) {
                                highestScore = word.getScore();
                                highestScoringTestPlay = word;
                            }
                        }
                        board.addWord(bestTestPlay);
                        System.out.println("Word rating: " + bestTestPlay.getRating());
                        System.out.printf("Otherwise would have played %s scoring %d\n", highestScoringTestPlay.getWord(), highestScore);

                        List<String> testTrayLettersUsed = bestTestPlay.getTrayLettersUsed();
                        for (String letter : testTrayLettersUsed) {
                            testTray = testTray.replaceFirst(letter, "");
                            if (Character.isLowerCase(letter.charAt(0))) {
                                testTray = testTray.replaceFirst("~", "");
                            }
                        }
                        testGameScore += bestTestPlay.getScore();
                        consecutiveTestScoresZero = 0;
                    }

                    if (consecutiveControlScoresZero > 2 && consecutiveTestScoresZero > 2) {
                        break;
                    }
                }

                controlTotalScore += controlGameScore;
                testTotalScore += testGameScore;

                if (controlGameScore > testGameScore) {
                    System.out.println("Control won this game!");
                    gamesWonByControl += 1;
                }

                else if (controlGameScore < testGameScore) {
                    System.out.println("Test won this game!");
                    gamesWonByTest += 1;
                }

                board.show('L');
                System.out.printf("Game %d of %d: control score = %d, test score = %d\n", iterationsPlayed + 1,
                        maxIterations, controlGameScore, testGameScore);
                iterationsPlayed += 1;

                float controlAverageScore = (float) controlTotalScore / iterationsPlayed;
                float testAverageScore = (float) testTotalScore / iterationsPlayed;
                float fractionGamesWonByControl = (float) gamesWonByControl / iterationsPlayed;
                float fractionGamesWonByTest = (float) gamesWonByTest / iterationsPlayed;

                System.out.println("Test Average Score: " + testAverageScore);
                System.out.println("Test Win Fraction: " + fractionGamesWonByTest);
                System.out.println();
                System.out.println("Control Average Score: " + controlAverageScore);
                System.out.println("Control Win Fraction: " + fractionGamesWonByControl);
                System.out.println("\n\n");
            }
            System.out.println("\n\nIterations Complete!");
        }

        else {
            Board board = new Board(dictHandler);
            Scorer scorer = new Scorer(board);
            Validator validator = new Validator(board, dictHandler.getDictionary());
            Generator generator = new Generator(board, dictHandler, validator, scorer, false);
            //Main loop
            while (true) {
                board.show('L');

                // find out if the user would like to add a word or get a suggestion
                String action = mainThread.getActionFromUser();

                if (action.equals("A")) {

                    //add a new word to the board
                    String word = mainThread.getWordFromUser();
                    char orientation = mainThread.getOrientationFromUser();
                    int startCol = mainThread.getStartColFromUser();
                    int startRow = mainThread.getStartRowFromUser();

                    try {
                        Word wordToAdd = new Word(word, Utils.letterLocations(word, startCol, startRow, orientation),
                                orientation);
                        board.addWord(wordToAdd);
                    } catch (IllegalArgumentException e) {
                        System.out.println("That play was not valid, please make a new choice\n");
                    }
                }

                //get suggestions
                else if (action.equals("G")) {

                    //get the user's tray letters
                    String tray = mainThread.getTrayFromUser();

                    //generate suggestions based on tray
                    System.out.println("Scrabby is thinking.....");

                    Set<Word> possibleWords;
                    try {
                        possibleWords = generator.getSuggestions(tray);
                    } catch (IllegalStateException e) {
                        System.out.println("Too many blanks in the tray given, maximum of two!");
                        continue;
                    }

                    mainThread.displaySuggestionsToUser(possibleWords);
                }
            }
        }
    }

    private String getModeFromUser() {
        //get functionality from user, loop only used to deal with invalid input
        System.out.println("Enter (D)iagnostic mode?");
        String mode = scanner.next().toUpperCase();
        return mode;
    }

    private String getActionFromUser() {
        while (true) {
            //get functionality from user, loop only used to deal with invalid input
            System.out.println("Would you like to (A)dd a word to the board or (G)et a suggestion? ");
            String action = scanner.next().toUpperCase();

            if (!(action.equals("A")) && !(action.equals("G"))) {
                System.out.println("Please only enter the letter A or G");
                continue;
            }
            return action;
        }
    }

    private String getWordFromUser() {
        while (true) {
            System.out.println("Type your word to add (represent blanks with lower case): ");
            String word = scanner.next();
            if (word.length() > 15) {
                System.out.println("Your word must be 15 characters or fewer");
                continue;
            }
            return word;
        }
    }

    private char getOrientationFromUser() {
        while (true) {
            System.out.println("(H)orizontally or (V)ertically?");
            String orientationString = scanner.next().toUpperCase();
            if (!(orientationString.equals("H")) && !(orientationString.equals("V"))) {
                System.out.println("Please only enter the letter H or V");
                continue;
            }

            char orientation = orientationString.charAt(0);
            return orientation;
        }
    }

    private int getStartColFromUser() {
        while (true) {
            System.out.println("Column word starts in (enter a number between 0 and 14): ");
            try {
                int startCol = scanner.nextInt();
                if (startCol < 0 || startCol > 14) {
                    System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    continue;
                }
                return startCol;
            }
            catch (InputMismatchException e) {
                System.out.println("Please only enter a number between 0 and 14 (inclusive)");
            }
        }
    }

    private int getStartRowFromUser() {
        while (true) {
            //find the starting row
            System.out.println("Row word starts in (enter a number between 0 and 14): ");
            try {
                int startRow = scanner.nextInt();
                if (startRow < 0 || startRow > 14) {
                    System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    continue;
                }
                return startRow;
            }
            catch (InputMismatchException e) {
                System.out.println("Please only enter a number between 0 and 14 (inclusive)");
            }
        }
    }

    private String getTrayFromUser() {
        while (true) {
            System.out.println("Please enter the letters in your tray (represent blank tiles with '~'): ");
            String tray = scanner.next().toUpperCase();
            if (tray.length() > Utils.MAX_TRAY_SIZE) {
                System.out.printf("Please enter %d letters or fewer", Utils.MAX_TRAY_SIZE);
                continue;
            }
            return tray;
        }
    }

    private void displaySuggestionsToUser(Set<Word> possibleWords) {
        List<Word> sortedPossibleWords = Utils.sortWordsByRating(possibleWords);

        //display top 10 suggestions sorted by highest score last
        int numberOfSuggestions = sortedPossibleWords.size();
        System.out.printf("\nBest plays are: \n");

        int wordsPassed = 0;
        for (Word word : sortedPossibleWords) {
            if (wordsPassed > numberOfSuggestions - 10) {
                int[] letterLocations = word.getLocations();
                String[] coordinatesToPrint = new String[letterLocations.length];
                for (int index = 0; index < letterLocations.length; index++) {
                    coordinatesToPrint[index] = Arrays.toString(Utils.sCoordinates(letterLocations[index]));
                }
                String directionWord = "UNSET";
                if (word.getOrientation() == 'H') {
                    directionWord = "horizontally";
                }
                if (word.getOrientation() == 'V') {
                    directionWord = "vertically";
                }

                System.out.printf("%s scoring %d %s from %s, play rated %d\n", word.getWord(), word.getScore(),
                        directionWord, coordinatesToPrint[0],
                        word.getRating());
            }
            wordsPassed += 1;
        }
    }

    private int getIterationCountFromUser() {
        while (true) {
            System.out.println("Number of iterations to run: ");
            try {
                int startRow = scanner.nextInt();
                if (startRow < 0) {
                    System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    continue;
                }
                return startRow;
            }
            catch (InputMismatchException e) {
                System.out.println("Please only enter a number between 0 and 14 (inclusive)");
            }
        }
    }
}