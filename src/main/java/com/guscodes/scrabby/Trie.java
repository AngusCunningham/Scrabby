package com.guscodes.scrabby;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trie {
    private TrieNode root;
    private Set<String> output;

    public Trie(Set<String> wordsToAdd) {
        if (root == null) {
            root = new TrieNode("");
        }
        int count = 0;
        for (String word : wordsToAdd) {
            insert(word);
            count += 1;
        }
        System.out.printf("%d words added to trie\n", count);
    }

    public TrieNode getRootTrieNode() {
        return this.root;
    }

    public void insert(String word) {
        TrieNode currentTrieNode = root;
        String[] letters = word.split("");

        for (String letter : letters) {
            HashMap <String, TrieNode> children = currentTrieNode.getChildren();

            if (children.containsKey(letter)) {
                currentTrieNode = children.get(letter);
            }
            else {
                currentTrieNode.addChild(letter, new TrieNode(letter));
                currentTrieNode = children.get(letter);
            }
        }
        currentTrieNode.setTerminal();
    }

    public Set<String> search(String prefix) {
        System.out.println("Searching trie for: " + prefix);
        this.output = new HashSet<>();
        TrieNode currentTrieNode = root;
        String[] letters = prefix.split("");
        for (String letter : letters) {
            if (currentTrieNode.getChildren().keySet().contains(letter)) {
                currentTrieNode = currentTrieNode.getChildren().get(letter);
            }
            else {
                System.out.println("Search completed, no matching items found");
                return new HashSet<String>();
            }
        }
        depthFirstSearch(currentTrieNode, prefix.substring(0, prefix.length() - 1));
        System.out.printf("Search completed, %d matching items found\n", this.output.size());
        return this.output;
    }

    private void depthFirstSearch(TrieNode startTrieNode, String prefix) {
        if (startTrieNode.isTerminal()) {
            this.output.add(prefix + startTrieNode.getLetter());
        }

        for (TrieNode TrieNode : startTrieNode.getChildren().values()) {
            depthFirstSearch(TrieNode, prefix + startTrieNode.getLetter());
        }
    }
}
