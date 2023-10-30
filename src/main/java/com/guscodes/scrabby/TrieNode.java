package com.guscodes.scrabby;

import java.util.HashMap;

public class TrieNode {
    private String letter;
    private boolean terminal = false;
    private HashMap<String, TrieNode> children = new HashMap<>();
    public TrieNode(String letter) {
        this.letter = letter;
    }

    public HashMap<String, TrieNode> getChildren() {
        return children;
    }

    public void addChild(String letter, TrieNode childNode) {
        children.put(letter, childNode);
    }

    public String getLetter() {
        return letter;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(){
        terminal = true;
    }
}
