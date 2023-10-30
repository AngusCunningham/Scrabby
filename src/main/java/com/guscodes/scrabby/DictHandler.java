package com.guscodes.scrabby;

import java.io.InputStream;
import java.util.*;

public class DictHandler {
    private Set<String> dictionary = buildDictionary();
    private Trie dictTrie = new Trie(dictionary);
    public Set<String> getDictionary() {
        return this.dictionary;
    }
    public Set<String> searchTrie(String prefix) {
        return dictTrie.search(prefix);
    }

    private Set<String> buildDictionary() {
        Set<String> scrabbleWords = new HashSet<>();

        InputStream inputStream = getClass().getResourceAsStream("dict.txt");
        Scanner scanner = new Scanner(inputStream);

        //read line by line
        try (scanner) {
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                // since one-letter words are of no use in scrabble, only add words longer than one letter
                if (line.length() > 1) {
                    scrabbleWords.add(line);
                }
            }
        }
        System.out.printf("%d words read into scrabble dictionary\n", scrabbleWords.size());
        letterFrequencyInDictionary(scrabbleWords);
        return scrabbleWords;
    }

    public HashMap<Character, Integer> letterFrequencyInDictionary(Collection<String> dictionary) {
        HashMap<Character, Integer> letterFreqs = new HashMap<>();
        for (char letter : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
            letterFreqs.put(letter, 0);
        }

        for (String word : dictionary) {
            for (char letter : word.toCharArray()) {
                int oldValue = letterFreqs.get(letter);
                letterFreqs.put(letter, oldValue + 1);
            }
        }
        for (char letter : letterFreqs.keySet()) {
            //System.out.printf("%c : %d\n", letter, letterFreqs.get(letter));
        }
        return letterFreqs;
    }

    public TrieNode getTrieRoot() {
        return dictTrie.getRootTrieNode();
    }
}
