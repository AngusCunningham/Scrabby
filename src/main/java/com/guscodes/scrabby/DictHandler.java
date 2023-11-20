package com.guscodes.scrabby;

import java.io.InputStream;
import java.util.*;

public class DictHandler {
    private Set<String> dictionary = buildDictionary();
    private Trie dictTrie = new Trie(dictionary);
    private HashMap<Character, Float> letterFreqs = letterFrequencyInDictionary(dictionary);

    public Set<String> getDictionary() {
        return this.dictionary;
    }
    public Set<String> searchTrie(String prefix) {
        return dictTrie.search(prefix);
    }

    public HashMap<Character, Float> getLetterFreqs() {
        return this.letterFreqs;
    }

    private Set<String> buildDictionary() {
        Set<String> scrabbleWords = new HashSet<>();

        InputStream inputStream = getClass().getResourceAsStream("Dict.txt");
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
        return scrabbleWords;
    }

    public HashMap<Character, Float> letterFrequencyInDictionary(Collection<String> dictionary) {
        HashMap<Character, Integer> letterFreqs = new HashMap<>();
        HashMap<Character, Float> fractionalLetterFreqs = new HashMap<>();
        for (char letter : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
            letterFreqs.put(letter, 0);
        }

        // find how often each letter appears
        for (String word : dictionary) {
            for (char letter : word.toCharArray()) {
                int oldValue = letterFreqs.get(letter);
                letterFreqs.put(letter, oldValue + 1);
            }
        }

        // find the total number of letter appearances
        int totalAppearances = 0;
        for (char letter : letterFreqs.keySet()) {
            totalAppearances += letterFreqs.get(letter);
        }

        // write letter frequency as a percentage
        for (char letter : letterFreqs.keySet()) {
            float fractionOfLettersInAllWords = (float) letterFreqs.get(letter) / totalAppearances;
            fractionalLetterFreqs.put(letter, fractionOfLettersInAllWords);
            System.out.printf("%c : %f\n", letter, fractionOfLettersInAllWords);
        }

        return fractionalLetterFreqs;
    }

    public TrieNode getTrieRoot() {
        return dictTrie.getRootTrieNode();
    }
}
