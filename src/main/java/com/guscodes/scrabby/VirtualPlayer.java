package com.guscodes.scrabby;

import java.util.List;
import java.util.Set;

public class VirtualPlayer implements Player {
    private Generator generator;
    private Board board;
    private TileBag tileBag;
    private Scorer scorer;
    private String tray = "";
    private int score = 0;
    private int consecutivePassCount = 0;

    public VirtualPlayer(Generator generator, Board board, TileBag tileBag, Scorer scorer) {
        this.generator = generator;
        this.board = board;
        this.tileBag = tileBag;
        this.scorer = scorer;
        replenishTray();
    }

    @Override
    public void takeTurn() {

        // get suggestions based on the current tray
        Set<Word> suggestedPlays = generator.getSuggestions(tray, board);

        // if there are no valid suggestions...
        if (suggestedPlays.size() == 0) {

            //return current tiles to bag
            for (char tile : tray.toCharArray()) {
                tileBag.returnTileToBag(tile);
            }

            // empty tray and record the pass
            tray = "";
            consecutivePassCount += 1;
        }

        // if there are valid suggestions..
        else {

            // rank suggestions and play best one
            List<Word> rankedSuggestions = Utils.sortWordsByRating(suggestedPlays);
            Word bestWord = rankedSuggestions.get(rankedSuggestions.size() - 1);
            board.addWord(bestWord);
            score += bestWord.getScore();

            // remove the tiles used from the tray
            List<String> trayLettersUsed = bestWord.getTrayLettersUsed();
            for (String letter : trayLettersUsed) {
                tray = tray.replaceFirst(letter, "");
                if (Character.isLowerCase(letter.charAt(0))) {
                    tray = tray.replaceFirst("~", "");
                }
            }

            // reset consecutive pass count
            consecutivePassCount = 0;
        }

        // refill tray from tile bag
        replenishTray();

        // end of player's turn
    }

    public String getTray() {
        return tray;
    }

    public int getScore() {
        return score;
    }

    public void calculateFinalScore() {
        char[] remainingTiles = tray.toCharArray();
        int deductedPoints = 0;
        for (char tile : remainingTiles) {
            deductedPoints += scorer.getLetterScore(tile);
        }

        score -= deductedPoints;
    }

    public int getConsecutivePassCount() {
        return consecutivePassCount;
    }
    private void replenishTray() {
        int numLettersToTake = Utils.MAX_TRAY_SIZE - tray.length();
        int numLettersTaken = 0;
        while (numLettersTaken < numLettersToTake) {
            try {
                tray += tileBag.getLetter();
                numLettersTaken += 1;
            }
            catch (IllegalStateException e) {

                // tile bag is empty
                break;
            }
        }
    }
}
