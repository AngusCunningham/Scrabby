package com.guscodes.scrabby;

import com.guscodes.scrabby.gameitems.Word;

import java.util.*;

public class UserInterface {
    Scanner scanner;

    UserInterface(Scanner scanner) {
        this.scanner = scanner;
    }
    String getModeFromUser() {
        //get functionality from user, loop only used to deal with invalid input
        System.out.println("Press the Return key to start a new game");
        String mode = scanner.nextLine().toUpperCase();
        return mode;
    }

    String getActionFromUser() {
        while (true) {
            //get functionality from user, loop only used to deal with invalid input
            System.out.printf("Would you like to (A)dd a word to the board or (G)et a suggestion? ");
            String action = scanner.nextLine().toUpperCase();

            if (!(action.equals("A")) && !(action.equals("G")) && !(action.equals("U"))) {
                System.out.println("Please only enter the letter A or G");
                continue;
            }
            return action;
        }
    }

    String getWordFromUser(Word topReccomendation) {
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

    char getOrientationFromUser() {
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

    int getStartColFromUser() {
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

    int getStartRowFromUser() {
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

    String getTrayFromUser() {
        while (true) {
            System.out.printf("Please enter the letters in your tray (represent blank tiles with '~'): ");
            String tray = scanner.nextLine().toUpperCase();
            if (tray.length() > Data.MAX_TRAY_SIZE) {
                System.out.printf("Please enter %d letters or fewer", Data.MAX_TRAY_SIZE);
                continue;
            }
            return tray;
        }
    }

    Word displaySuggestionsToUser(Set<Word> possibleWords) {
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
                    coordinatesToPrint[index] = Arrays.toString(Utils.toBoardCoordinates(letterLocations[index]));
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

    int getIterationCountFromUser() {
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
