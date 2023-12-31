package com.guscodes.scrabby.simulation;
import com.guscodes.scrabby.analysis.Validator;
import com.guscodes.scrabby.analysis.Scorer;
import com.guscodes.scrabby.gameitems.Board;
import com.guscodes.scrabby.gameitems.TileBag;
import com.guscodes.scrabby.generation.Generator;
import com.guscodes.scrabby.generation.MoveFinder;
import com.guscodes.scrabby.lexicon.WordHandler;

import java.awt.Toolkit;
import java.util.HashMap;

public class SimulatedGame {
    private final WordHandler wordHandler;
    private final Validator validator;
    private final Scorer scorer;

    private int controlTotalScore;
    private int testTotalScore;
    private int gamesWonByControl;
    private int gamesWonByTest;

    private int totalIterations;
    private int iterationsPlayed;

    public SimulatedGame(WordHandler wordHandler, Validator validator, Scorer scorer) {
        this.wordHandler = wordHandler;
        this.validator = validator;
        this.scorer = scorer;
    }

    public void simulateNGames(int n) {

        double[] testParameterValues = {70, 80, 85, 50, 60, 90};

        HashMap<Double, Double> finalResults = new HashMap<>();

        for (double testValue : testParameterValues) {
            finalResults.put(testValue, 0.0);

        }

        int valuesExamined = 0;
        int valuesToExamine = testParameterValues.length;

        for (double testValue : testParameterValues) {

            controlTotalScore = 0;
            testTotalScore = 0;
            gamesWonByControl = 0;
            gamesWonByTest = 0;

            totalIterations = n;
            iterationsPlayed = 0;

            valuesExamined += 1;

            System.out.printf("Beginning test on value %f which is %d out of %d values to test at %d iterations each\n",
                    testValue, valuesExamined, valuesToExamine, n);

            while (iterationsPlayed < n) {
                playOneRound(testValue);
            }

            double testWinFraction = (double) gamesWonByTest / iterationsPlayed;
            finalResults.put(testValue, testWinFraction);
            System.out.printf("Using a multiplier of %f, testPlayer won %f of the games played\n\n",
                                                                                    testValue, testWinFraction);
        }

        System.out.println("\n");
        for (double testValue : testParameterValues) {
            System.out.printf("Value of %f produces a test win fraction of: %f \n",
                    testValue, finalResults.get(testValue));
        }

        Toolkit.getDefaultToolkit().beep();
    }

    private void playOneRound(double testParameter) {
        Board board = new Board(validator, scorer, false, false);
        TileBag tileBag = new TileBag();
        MoveFinder moveFinder = new MoveFinder(wordHandler);

        Generator baseGen = new Generator(moveFinder, validator, scorer, false, 0, false);
        Generator testGen = new Generator(moveFinder, validator, scorer, true, testParameter, false);

        VirtualPlayer controlPlayer = new VirtualPlayer(baseGen, board, tileBag, scorer);
        VirtualPlayer testPlayer = new VirtualPlayer(testGen, board, tileBag, scorer);

        VirtualPlayer[] players = {controlPlayer, testPlayer};

        boolean playingOn = true;

        System.out.printf("Beginning game %d out of %d\n", iterationsPlayed + 1, totalIterations);
        while (playingOn) {
            for (VirtualPlayer player : players) {
                player.takeTurn();

                // if the player has an empty tray, the game ends
                boolean playerFinished = player.getTray().length() == 0;

                // if both players have passed 3 times, the game ends
                boolean passLimitReached = ((controlPlayer.getConsecutivePassCount()) > 2 &&
                                                                        (testPlayer.getConsecutivePassCount() > 2));

                if (passLimitReached || playerFinished) {
                    controlPlayer.calculateFinalScore();
                    testPlayer.calculateFinalScore();
                    playingOn = false;
                    break;
                }
            }
        }

        iterationsPlayed += 1;

        int controlFinalScore = controlPlayer.getScore();
        int testFinalScore = testPlayer.getScore();
        controlTotalScore += controlFinalScore;
        testTotalScore += testFinalScore;

        if (controlFinalScore > testFinalScore) {
            gamesWonByControl += 1;
        }

        if (testFinalScore > controlFinalScore) {
            gamesWonByTest += 1;
        }


        // board.show('L');
        //System.out.printf("Game %d of %d completed\n\n", iterationsPlayed, totalIterations);
        //System.out.printf("Player 1 scored: %d\n", controlFinalScore);
        //System.out.printf("Player 2 scored: %d\n", testFinalScore);

        float player1Average = (float) controlTotalScore / iterationsPlayed;
        float player2Average = (float) testTotalScore / iterationsPlayed;
        //float fractionGamesWonByPlayer1 = (float) gamesWonByControl / iterationsPlayed;
        float fractionGamesWonByTest = (float) gamesWonByTest / iterationsPlayed;

        System.out.println("Control Average Score: " + player1Average);
        // System.out.println("Base Win Fraction: " + fractionGamesWonByPlayer1);
        // System.out.println();
        System.out.println("Test Average Score: " + player2Average);
        System.out.println("Test Win Fraction: " + fractionGamesWonByTest);
        System.out.println();
        //System.out.println();

    }
}