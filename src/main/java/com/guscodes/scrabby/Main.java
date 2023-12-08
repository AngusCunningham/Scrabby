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

            int maxIterations = mainThread.getIterationCountFromUser();
            SimulatedGame sim = new SimulatedGame(dictHandler);

            sim.simulateNGames(maxIterations);

            System.out.println("\n\nIterations Complete!");
        }

        else {
            Board board = new Board(dictHandler);
            Scorer scorer = new Scorer(board);
            LetterExtender letterExtender = new LetterExtender(board.getSquares(), dictHandler);
            Validator validator = new Validator(board, dictHandler.getDictionary());
            Generator generator = new Generator(letterExtender, validator, scorer, false,
                    0, true, board);
            Word topRecommendation = null;

            //Main loop
            while (true) {
                board.show('L');

                // find out if the user would like to add a word or get a suggestion
                String action = mainThread.getActionFromUser();

                if (action.equals("A")) {

                    //add a new word to the board
                    String word = mainThread.getWordFromUser(topRecommendation);
                    if (word.equals("PLAY_TOP_REC") && topRecommendation != null) {
                        board.addWord(topRecommendation);
                    }

                    else {
                        char orientation = mainThread.getOrientationFromUser();
                        int startCol = mainThread.getStartColFromUser();
                        int startRow = mainThread.getStartRowFromUser();

                        int[] letterLocations = {0};

                        try {
                            letterLocations = Utils.letterLocations(word, startCol, startRow, orientation);
                            try {
                                Word wordToAdd = new Word(word, letterLocations, orientation);
                                board.addWord(wordToAdd);
                            } catch (IllegalArgumentException e) {
                                System.out.println("That play was not valid, please make a new choice\n");
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            System.out.println("Can't play there, word would be off the board\n");
                        }
                    }
                }

                //get suggestions
                else if (action.equals("G")) {

                    //get the user's tray letters
                    String tray = mainThread.getTrayFromUser();

                    //generate suggestions based on tray
                    Set<Word> possibleWords;
                    try {
                        possibleWords = generator.getSuggestions(tray, board);
                    } catch (IllegalStateException e) {
                        System.out.println("Too many blanks in the tray given, maximum of two!");
                        continue;
                    }

                    topRecommendation = mainThread.displaySuggestionsToUser(possibleWords);
                }
            }
        }
    }

    private String getModeFromUser() {
        //get functionality from user, loop only used to deal with invalid input
        System.out.printf("Enter (D)iagnostic mode?");
        String mode = scanner.nextLine().toUpperCase();
        return mode;
    }

    private String getActionFromUser() {
        while (true) {
            //get functionality from user, loop only used to deal with invalid input
            System.out.printf("Would you like to (A)dd a word to the board or (G)et a suggestion? ");
            String action = scanner.nextLine().toUpperCase();

            if (!(action.equals("A")) && !(action.equals("G"))) {
                System.out.println("Please only enter the letter A or G");
                continue;
            }
            return action;
        }
    }

    private String getWordFromUser(Word topReccomendation) {
        while (true) {
            System.out.printf("Type your word to add (represent blanks with lower case): ");
            String word = scanner.nextLine();
            if (word.length() > 15) {
                System.out.println("Your word must be 15 characters or fewer");
                continue;
            }
            if (topReccomendation != null && word.equals("")) {
                word = "PLAY_TOP_REC";
            }
            else if (word.length() < 2) {
                System.out.println("Your word must be longer than one letter");
                continue;
            }
            return word;
        }
    }

    private char getOrientationFromUser() {
        while (true) {
            System.out.printf("(H)orizontally or (V)ertically? ");
            String orientationString = scanner.nextLine().toUpperCase();
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
            System.out.printf("Column word starts in (enter a number between 0 and 14): ");
            try {
                int startCol = Integer.parseInt(scanner.nextLine());
                if (startCol < 0 || startCol > 14) {
                    System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    continue;
                }
                return startCol;
            }
            catch (NumberFormatException e) {
                System.out.println("Please only enter a number between 0 and 14 (inclusive)");
            }
        }
    }

    private int getStartRowFromUser() {
        while (true) {
            //find the starting row
            System.out.printf("Row word starts in (enter a number between 0 and 14): ");
            try {
                int startRow = Integer.parseInt(scanner.nextLine());
                if (startRow < 0 || startRow > 14) {
                    System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    continue;
                }
                return startRow;
            }
            catch (NumberFormatException e) {
                System.out.println("Please only enter a number between 0 and 14 (inclusive)");
            }
        }
    }

    private String getTrayFromUser() {
        while (true) {
            System.out.printf("Please enter the letters in your tray (represent blank tiles with '~'): ");
            String tray = scanner.nextLine().toUpperCase();
            if (tray.length() > Utils.MAX_TRAY_SIZE) {
                System.out.printf("Please enter %d letters or fewer", Utils.MAX_TRAY_SIZE);
                continue;
            }
            return tray;
        }
    }

    private Word displaySuggestionsToUser(Set<Word> possibleWords) {
        List<Word> sortedPossibleWords = Utils.sortWordsByRating(possibleWords);

        //display top 10 suggestions sorted by highest score last
        int numberOfSuggestions = sortedPossibleWords.size();
        System.out.printf("\nBest plays are: \n");

        int wordsPassed = 0;
        Word bestWord = null;
        for (Word word : sortedPossibleWords) {
            bestWord = word;
            if (wordsPassed > numberOfSuggestions - 10 || numberOfSuggestions < 10) {
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
        return bestWord;
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