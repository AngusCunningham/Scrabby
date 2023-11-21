package com.guscodes.scrabby;

import java.util.ArrayList;
import java.util.List;

public class SimulatedGame {
    private DictHandler dictHandler;
    private Scorer scorer;
    private Validator validator;

    private List<Integer> player1Scores = new ArrayList<>();
    private List<Integer> player2Scores = new ArrayList<>();


    public SimulatedGame(DictHandler dictHandler, Scorer scorer, Validator validator, ) {
        this.dictHandler = dictHandler;
        this.scorer = scorer;
        this.validator = validator;
    }

    public List<List<Integer>> simulateNGames(int n) {
        int gamesPlayed = 0;
        while (gamesPlayed < n) {
            playOneRound();
        }
        List<List<Integer>> scores =  new ArrayList<List<Integer>>();
        scores.add(player1Scores);
        scores.add(player2Scores);

        return scores;
    }

    private void playOneRound() {
        Board board = new Board(dictHandler);
        TileBag tileBag = new TileBag();

        Generator baseGen = new Generator(board, dictHandler, validator, scorer, false);
        Generator testGen = new Generator(board, dictHandler, validator, scorer, true);

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
        player1Scores.add(player1.getScore());
        player2Scores.add(player2.getScore());
        board.show('L');
    }
}