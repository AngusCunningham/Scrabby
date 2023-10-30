package com.guscodes.scrabby;

import java.util.*;

public class LetterExtender {
    private Square[] boardSquares;
    private Set<Word> wordsFound = new HashSet<>();
    private TrieNode rootNode;
    public LetterExtender(Square[] boardSquares, DictHandler dictHandler) {
        this.boardSquares = boardSquares;
        rootNode = dictHandler.getTrieRoot();
    }

    public Set<Word> getLegalMovesAnchoredAt(int startLocation, List<String> tray) {
        this.wordsFound.clear();
        extendAfter(rootNode, startLocation, 'H', startLocation, tray, "");
        extendAfter(rootNode, startLocation, 'V', startLocation, tray, "");
        return this.wordsFound;
    }

    private void extendAfter(TrieNode node, int location, char orientation,
                                    int originalStart, List<String> tray, String partialWord) {

        Square startSquare = boardSquares[location];
        String contents = String.valueOf(startSquare.getContents());
        HashMap<String, TrieNode> nextLetters = node.getChildren();

        // if square is empty
        if ("_".equals(contents)) {

            // if this node is the end of a word in the trie
            if (node.isTerminal()) {
                Word newWordFound = new Word(partialWord, originalStart, orientation);
                wordsFound.add(newWordFound);
            }

            // see if any words can continue from the square trying each of the remaining tray letters
            for (String letter : tray) {
                if (nextLetters.keySet().contains(letter.toUpperCase())) {
                    TrieNode newNode = nextLetters.get(letter.toUpperCase());
                    List<String> newTray = new ArrayList<>(tray);
                    newTray.remove(letter);
                    String newWord = partialWord + letter;
                    try {
                        int nextLocation = Utils.nextLocation(location, orientation);
                        extendAfter(newNode, nextLocation, orientation, originalStart, newTray, newWord);
                    }
                    catch (IndexOutOfBoundsException e) {
                        // next square would be the edge of the board so the word must end here
                        if (newNode.isTerminal()) {
                            Word newWordFound = new Word(newWord, originalStart, orientation);
                            wordsFound.add(newWordFound);
                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Orientation used in LetterExtender must only be 'H' or 'V'");
                    }
                }
            }
        }

        // if square has already been played, check if the tile in it can add to the word at all
        else if (nextLetters.keySet().contains(contents.toUpperCase())) {
            TrieNode newNode = nextLetters.get(contents.toUpperCase());
            String newWord = partialWord + contents;
            try {
                int nextLocation = Utils.nextLocation(location, orientation);
                extendAfter(newNode, nextLocation, orientation, originalStart, tray, newWord);
            }
            catch (IndexOutOfBoundsException e) {
                if (newNode.isTerminal()) {
                    Word newWordFound = new Word(newWord, originalStart, orientation);
                    wordsFound.add(newWordFound);
                }
            }
            catch (IllegalArgumentException e) {
                System.out.println("Orientation used in LetterExtender must only be 'H' or 'V'");
            }
        }
    }
}
