package com.guscodes.scrabby;

import com.guscodes.scrabby.analysis.Validator;
import com.guscodes.scrabby.analysis.Scorer;
import com.guscodes.scrabby.gameitems.Board;
import com.guscodes.scrabby.gameitems.Word;
import com.guscodes.scrabby.generation.Generator;
import com.guscodes.scrabby.generation.MoveFinder;
import com.guscodes.scrabby.lexicon.WordHandler;
import com.guscodes.scrabby.simulation.SimulatedGame;

import java.util.*;

public class Main {
    public static void main (String[] args) {
        //Dependencies
        Scanner scanner = new Scanner(System.in);
        UserInterface ui = new UserInterface(scanner);
        WordHandler wordHandler = new WordHandler();
        Validator validator = new Validator(wordHandler.getAllWords());
        Scorer scorer = new Scorer();

        String welcomeString = "\nWelcome to Scrabby, your scrabble word finder\n";
        System.out.println(welcomeString);

        // check if suggesting mode or playing mode
        String mode = ui.getModeFromUser();

        if (mode.equals("D")) {
            int maxIterations = ui.getIterationCountFromUser();
            SimulatedGame sim = new SimulatedGame(wordHandler, validator, scorer);
            sim.simulateNGames(maxIterations);
            System.out.println("\n\nSimulations Complete!");
        }

        else {
            Board board = new Board(validator, scorer, true);

            MoveFinder moveFinder = new MoveFinder(board.getSquares(), wordHandler);
            Generator generator = new Generator(moveFinder, validator, scorer, false,
                    0, true, board);
            Word topRecommendation = null;

            //Main loop
            while (true) {
                board.show('L');

                // find out if the user would like to add a word or get a suggestion
                String action = ui.getActionFromUser();

                if (action.equals("A")) {

                    //add a new word to the board
                    String word = ui.getWordFromUser(topRecommendation);
                    if (word.equals("PLAY_TOP_REC") && topRecommendation != null) {
                        board.addWord(topRecommendation);
                    }

                    else {
                        char orientation = ui.getOrientationFromUser();
                        int startCol = ui.getStartColFromUser();
                        int startRow = ui.getStartRowFromUser();

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
            }
        }
    }
}