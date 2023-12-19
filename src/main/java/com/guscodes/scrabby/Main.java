package com.guscodes.scrabby;

import com.guscodes.scrabby.analysis.Validator;
import com.guscodes.scrabby.analysis.Scorer;
import com.guscodes.scrabby.gameitems.Board;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.generation.Generator;
import com.guscodes.scrabby.generation.MoveFinder;
import com.guscodes.scrabby.lexicon.DictHandler;
import com.guscodes.scrabby.simulation.SimulatedGame;

import java.util.*;

public class Main {
    public static void main (String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final DictHandler dictHandler = new DictHandler();
        final UserInterface ui = new UserInterface(scanner, dictHandler);
        dictHandler.buildDefinedDictionary("CSW19DEF.txt");
        dictHandler.buildLetterFrequencyTable(dictHandler.getDefinedDictionary().keySet());
        final Validator validator = new Validator(dictHandler);
        final Scorer scorer = new Scorer();

        final String welcomeString = "\nWelcome to Scrabby, your scrabble word finder\n";
        System.out.println(welcomeString);

        // check if suggesting mode or playing mode
        String mode = ui.getModeFromUser();

        if (! mode.equals("D")) {
            Board board = new Board(validator, scorer, true, true);

            final MoveFinder moveFinder = new MoveFinder(dictHandler);
            final Generator generator = new Generator(moveFinder, validator, scorer, true,
                    0.097, true, dictHandler);
            Word topRecommendation = new Word("UNSETWORDTOPLAY", new int[]{7, 7}, 'H');

            //Main loop
            while (true) {

                board.show('L');

                // find out if the user would like to add a word or get a suggestion
                String action = ui.getActionFromUser();

                if (action.equals("A")) {

                    //add a new word to the board
                    String word = ui.getWordFromUser(topRecommendation);
                    if (word.equals("PLAY_TOP_REC") && topRecommendation != null) {
                        try {
                            board.addWord(topRecommendation);
                        }
                        catch (IllegalArgumentException e){
                                System.out.println("That play was not valid, please try again");
                        }
                    }

                    else {
                        char orientation = ui.getOrientationFromUser();
                        int startCol = ui.getStartColFromUser();
                        int startRow = ui.getStartRowFromUser();

                        int[] letterLocations;

                        try {
                            letterLocations = Utils.letterLocations(word, startCol, startRow, orientation);
                            try {
                                Word wordToAdd = new Word(word, letterLocations, orientation);
                                board.addWord(wordToAdd);
                            } catch (IllegalArgumentException e) {
                                String invalidWordAction = ui.getInvalidWordDecisionFromUser();
                                if (invalidWordAction.equals("A")) {
                                    Set<String> userWords = ui.getUserWordsToAdd();
                                    for (String userWord : userWords) {
                                        dictHandler.addUserWord(userWord);
                                    }
                                    System.out.println("Try your play again...");
                                }
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
                    String tray = ui.getTrayFromUser();

                    //generate suggestions based on tray
                    Set<Word> possibleWords;
                    try {
                        possibleWords = generator.getSuggestions(tray, board);
                    } catch (IllegalStateException e) {
                        System.out.println("Too many blanks in the tray given, maximum of two!");
                        continue;
                    }

                    topRecommendation = ui.displaySuggestionsToUser(possibleWords);
                }

                // undo last move
                else if (action.equals("U")) {
                    System.out.println("UNDO CALLED");
                    board = board.getPreviousState();
                }

                // game has been won by one of the players
                else if (action.equals("W")) {
                    break;
                }
            }
        }

        else {
            int maxIterations = ui.getIterationCountFromUser();
            SimulatedGame sim = new SimulatedGame(dictHandler, validator, scorer);
            sim.simulateNGames(maxIterations);
            System.out.println("\n\nSimulations Complete!");
        }
    }
}