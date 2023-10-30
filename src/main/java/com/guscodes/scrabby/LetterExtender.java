package com.guscodes.scrabby;

import java.util.*;

public class LetterExtender {
    private Board board;
    private Set<Word> wordsFound = new HashSet<>();
    private TrieNode rootNode;
    public LetterExtender(Board currentBoard, DictHandler dictHandler) {
        this.board = currentBoard;
        rootNode = dictHandler.getTrieRoot();
    }

    public Set<Word> getLegalMovesAnchoredAt(int startLocation, List<String> tray) {
        this.wordsFound.clear();
        extendAfter(rootNode, startLocation, 'H', startLocation, tray, "");
        extendAfter(rootNode, startLocation, 'V', startLocation, tray, "");
        return this.wordsFound;
    }

    private void extendAfter(TrieNode startNode, int startLocation, char orientation,
                                    int originalStart, List<String> tray, String word) {
        Square[] boardSquares = board.getSquares();
        Square startSquare = boardSquares[startLocation];
        String contents = String.valueOf(startSquare.getContents());
        HashMap<String, TrieNode> children = startNode.getChildren();

        // if square is empty
        if ("_".equals(contents)) {

            // if this node is the end of a word
            if (startNode.isTerminal()) {
                Word newWordFound = new Word();
                newWordFound.setWord(word);
                newWordFound.setOrientation(orientation);
                newWordFound.setLocations(Utils.letterLocations(word, originalStart, orientation));
                wordsFound.add(newWordFound);
            }

            // see if any words can continue from the square trying each of the remaining tray letters
            for (String letter : tray) {
                if (children.keySet().contains(letter.toUpperCase())) {
                    TrieNode newNode = children.get(letter.toUpperCase());
                    List<String> newTray = new ArrayList<>(tray);
                    newTray.remove(letter);
                    String newWord = word + letter;
                    try {
                        int nextLocation = Utils.nextLocation(startLocation, orientation);
                        extendAfter(newNode, nextLocation, orientation, originalStart, newTray, newWord);
                    }
                    catch (IndexOutOfBoundsException e) {
                        // the edge of the board has been reached, so the nextLocation function will throw an exception
                        // no need to do anything, just let the loop continue to the next letter
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Orientation used in LetterExtender must only be 'H' or 'V'");
                    }

                }
            }
        }

        // if square has already been played, check if the tile in it can add to the word at all
        else if (children.keySet().contains(contents.toUpperCase())) {
            TrieNode newNode = children.get(contents.toUpperCase());
            String newWord = word + contents;
            try {
                int nextLocation = Utils.nextLocation(startLocation, orientation);
                extendAfter(newNode, nextLocation, orientation, originalStart, tray, newWord);
            }
            catch (IndexOutOfBoundsException e) {
                // the edge of the board has been reached, so the nextLocation function will throw an exception
                // no need to do anything, just let the loop continue to the next letter
            }
            catch (IllegalArgumentException e) {
                System.out.println("Orientation used in LetterExtender must only be 'H' or 'V'");
            }
        }
    }
}
