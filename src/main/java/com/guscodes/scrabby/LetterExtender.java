package com.guscodes.scrabby;

import java.util.*;

public class LetterExtender {
    Board board;
    Set<Word> wordsFound = new HashSet<>();
    TrieNode rootNode;
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
            //System.out.println(startSquare.getLocation() + " is empty");
            if (startNode.isTerminal()) {
                Word newWordFound = new Word();
                newWordFound.setWord(word);
                newWordFound.setOrientation(orientation);
                newWordFound.setLocations(Utils.letterLocations(word, originalStart, orientation));
                wordsFound.add(newWordFound);
            }

            for (String letter : tray) {
                //System.out.printf("At Node '%s', children are: \n", startNode.getLetter());
                //System.out.printf("Tray letter is: %s\n", letter);
                //System.out.println(children.keySet().toString());
                if (children.keySet().contains(letter.toUpperCase())) {
                    TrieNode newNode = children.get(letter.toUpperCase());
                    List<String> newTray = new ArrayList<>(tray);
                    newTray.remove(letter);
                    String newWord = word + letter;
                    try {
                        int nextLocation = Utils.nextLocation(startLocation, orientation);
                        extendAfter(newNode, nextLocation, orientation, originalStart, newTray, newWord);
                    }
                    catch (Exception e) {
                    }

                }
            }
        }

        else {
            if (children.keySet().contains(contents.toUpperCase())) {
                TrieNode newNode = children.get(contents.toUpperCase());
                String newWord = word + contents;
                try {
                    int nextLocation = Utils.nextLocation(startLocation, orientation);
                    extendAfter(newNode, nextLocation, orientation, originalStart, tray, newWord);
                }
                catch (Exception e) {
                }
            }
        }
    }
}
