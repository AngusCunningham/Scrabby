package com.guscodes.scrabby;

import java.util.ArrayList;
import java.util.List;

public class SimulatedGame {
    private DictHandler dictHandler;

    private int player1TotalScore;
    private int player2TotalScore;
    private int gamesWonBy1;
    private int gamesWonBy2;

    private int totalIterations;
    private int iterationsPlayed;

    public SimulatedGame(DictHandler dictHandler) {
        this.dictHandler = dictHandler;
    }

    public void simulateNGames(int n) {
        player1TotalScore = 0;
        player2TotalScore = 0;
        gamesWonBy1 = 0;
        gamesWonBy2 = 0;

        totalIterations = n;
        iterationsPlayed = 0;
        while (iterationsPlayed < n) {
            playOneRound();
        }
    }

    private void playOneRound() {
        Board board = new Board(dictHandler);
        Scorer scorer = new Scorer(board);
        Validator validator = new Validator(board, dictHandler.getDictionary());
        TileBag tileBag = new TileBag();

        Generator baseGen = new Generator(board, dictHandler, validator, scorer, false);
        Generator testGen = new Generator(board, dictHandler, validator, scorer, false);

        VirtualPlayer player1 = new VirtualPlayer(baseGen, board, tileBag, scorer);
        VirtualPlayer player2 = new VirtualPlayer(testGen, board, tileBag, scorer);

        VirtualPlayer[] players = {player1, player2};

        boolean playingOn = true;

        while (playingOn) {
            for (VirtualPlayer player : players) {
                player.takeTurn();

                // if the player has an empty tray, the game ends
                boolean playerFinished = player.getTray().length() == 0;

                // if both players have passed 3 times, the game ends
                boolean passLimitReached = ((player1.getConsecutivePassCount()) > 2 &&
                                                                        (player2.getConsecutivePassCount() > 2));

                if (passLimitReached || playerFinished) {
                    player1.calculateFinalScore();
                    player2.calculateFinalScore();
                    playingOn = false;
                    break;
                }
            }
        }

        iterationsPlayed += 1;

        int finalScore1 = player1.getScore();
        int finalScore2 = player2.getScore();
        player1TotalScore += finalScore1;
        player2TotalScore += finalScore2;

        if (finalScore1 > finalScore2) {
            gamesWonBy1 += 1;
        }

        if (finalScore2 > finalScore1) {
            gamesWonBy2 += 1;
        }

        board.show('L');
        System.out.printf("Game %d of %d completed\n\n", iterationsPlayed, totalIterations);
        System.out.printf("Player 1 scored: %d\n", finalScore1);
        System.out.printf("Player 2 scored: %d\n", finalScore2);

        float player1Average = (float) player1TotalScore / iterationsPlayed;
        float player2Average = (float) player2TotalScore / iterationsPlayed;
        float fractionGamesWonByPlayer1 = (float) gamesWonBy1 / iterationsPlayed;
        float fractionGamesWonByPlayer2 = (float) gamesWonBy2 / iterationsPlayed;

        System.out.println("\nBase Average Score: " + player1Average);
        System.out.println("Base Win Fraction: " + fractionGamesWonByPlayer1);
        System.out.println();
        System.out.println("Test Average Score: " + player2Average);
        System.out.println("Test Win Fraction: " + fractionGamesWonByPlayer2);
        System.out.println();
        System.out.println();
    }
}