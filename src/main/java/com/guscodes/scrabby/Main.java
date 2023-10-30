package com.guscodes.scrabby;

import java.util.*;

public class Main {
    public static void main (String[] args) {
        //Dependencies
        Scanner scanner = new Scanner(System.in);
        DictHandler dictHandler = new DictHandler();
        Board board = new Board(dictHandler);
        Scorer scorer = new Scorer(board);
        Validator validator = new Validator(board, dictHandler.getDictionary());
        Generator generator = new Generator(board, dictHandler, validator, scorer);

        String welcomeString = "\nWelcome to Scrabby, your scrabble word finder\n";
        System.out.println(welcomeString);

        Word socket = new Word();
        socket.setWord("SOCKET");
        socket.setOrientation('V');
        socket.setLocations(Utils.letterLocations("SOCKET", 112, 'V'));
        board.addWord(socket);

        Word crack = new Word("CRACK", 4, 9, 'H');
        board.addWord(crack);

        Word crated = new Word("CRATED", 4, 9, 'V');
        board.addWord(crated);

        Word dead = new Word("DEAD", 4, 14, 'H');
        board.addWord(dead);

        //begin main loop of program
        while (true) {

            board.show('L');

            String action;
            while (true) {
                //get functionality from user, loop only used to deal with invalid input
                System.out.println("Would you like to (A)dd a word to the board or (G)et a suggestion? ");
                action = scanner.next().toUpperCase();

                if (!(action.equals("A")) && !(action.equals("G"))) {
                    System.out.println("Please only enter the letter A or G");
                    continue;
                }
                break;
            }

            //add a word
            if (action.equals("A")) {

                //get details of word from user
                String word;
                while (true) {
                    //get the actual word
                    System.out.println("Type your word to add (represent blanks with lower case): ");
                    word = scanner.next();
                    if (word.length() > 15) {
                        System.out.println("Your word must be 15 characters or fewer");
                        continue;
                    }
                    break;
                }

                char orientation;
                while (true) {
                    //find out if it lies vertically or horizontally
                    System.out.println("(H)orizontally or (V)ertically?");
                    String orientationString = scanner.next().toUpperCase();
                    if (!(orientationString.equals("H")) && !(orientationString.equals("V"))) {
                        System.out.println("Please only enter the letter H or V");
                        continue;
                    }

                    orientation = orientationString.charAt(0);
                    break;
                }

                int startCol;
                while (true) {
                    //find the starting column
                    System.out.println("Column word starts in (enter a number between 0 and 14): ");
                    try {
                        startCol = scanner.nextInt();
                        if (startCol < 0 || startCol > 14) {
                            System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                            continue;
                        }
                        break;
                    }
                    catch (InputMismatchException e) {
                        System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    }
                }

                int startRow;
                while (true) {
                    //find the starting row
                    System.out.println("Row word starts in (enter a number between 0 and 14): ");
                    try {
                        startRow = scanner.nextInt();
                        if (startRow < 0 || startRow > 14) {
                            System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                            continue;
                        }
                        break;
                    }
                    catch (InputMismatchException e) {
                        System.out.println("Please only enter a number between 0 and 14 (inclusive)");
                    }
                }
                try {
                    Word wordToAdd = new Word(word, Utils.letterLocations(word, startCol, startRow, orientation),
                                                orientation);
                    board.addWord(wordToAdd);
                }
                catch (IllegalArgumentException e) {
                    System.out.println("That play was not valid, please make a new choice\n");
                }
            }

            //get suggestions
            else if (action.equals("G")) {

                //get the user's tray letters
                String tray;
                while (true) {
                    System.out.println("Please enter the letters in your tray (represent blank tiles with '~': ");
                    tray = scanner.next().toUpperCase();
                    if (tray.length() > Utils.MAX_TRAY_SIZE) {
                        System.out.printf("Please enter %d letters or fewer", Utils.MAX_TRAY_SIZE);
                        continue;
                    }
                    break;
                }

                //generate suggestions based on tray
                Set<Word> possibleWords = generator.getSuggestions(tray);
                List<Word> sortedPossibleWords = Utils.sortWordsByScore(possibleWords);

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
                            directionWord = "across";
                        }
                        if (word.getOrientation() == 'V') {
                            directionWord = "down";
                        }

                        System.out.printf("%s scoring %d %s from %s\n", word.getWord(), word.getScore(),
                                                                                directionWord, coordinatesToPrint[0]);
                    }
                    wordsPassed += 1;
                }
            }
        }
    }

    public static boolean arrayContainsInt(int[] array, int value) {
        for (int item : array) {
            if (value == item) return true;
        }
        return false;
    }
}