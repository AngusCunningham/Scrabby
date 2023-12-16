package com.guscodes.scrabby.lexicon;

import java.io.InputStream;
import java.util.*;

public class WordHandler {
    private Map<String, String> definedDictionary = new HashMap<>();
    private Trie dictTrie;
    private Map<Character, Double> frequencyTable = new HashMap<>();

    public void addUserWord(String word) {
        definedDictionary.put(word, "USER ADDED WORD");
    }

    public void buildDefinedDictionary(String dictionaryTextFilePath) {
        InputStream inputStream = getClass().getResourceAsStream(dictionaryTextFilePath);
        Scanner scanner = new Scanner(inputStream);

        //read line by line
        try (scanner) {
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                int locationOfFirstWhitespace = 0;
                for (int index = 0; index < line.length(); index++) {
                    if (Character.isWhitespace(line.charAt(index))) {
                        locationOfFirstWhitespace = index;
                        break;
                    }
                }

                String word = line.substring(0, locationOfFirstWhitespace).stripTrailing().toUpperCase();
                String definition = line.substring(locationOfFirstWhitespace).stripLeading();

                //System.out.printf("Word: %s: %s\n", word, definition);

                // since one-letter words are of no use in scrabble, only add words longer than one letter
                if (word.length() > 1) {
                    definedDictionary.put(word, definition);
                }
            }
        }

        dictTrie = new Trie(definedDictionary.keySet());

        System.out.printf("%d words and their definitions read into the dictionary\n", definedDictionary.size());
    }

    public Map<String, String> getDefinedDictionary() {
        return this.definedDictionary;
    }

    public void buildLetterFrequencyTable(Collection<String> dictionary) {
        HashMap<Character, Double> letterFreqs = new HashMap<>();
        for (char letter : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
            letterFreqs.put(letter, 0.0);
        }

        // find how often each letter appears

        for (String word : dictionary) {
            List<String> wordLetters = Arrays.asList(word.split(""));
            for (char letter : letterFreqs.keySet()) {
                if (wordLetters.contains(String.valueOf(letter))) {
                    letterFreqs.put(letter, letterFreqs.get(letter) + 1);
                }
            }
        }

        int dictSize = dictionary.size();
        for (char letter :  letterFreqs.keySet()) {
            letterFreqs.put(letter, letterFreqs.get(letter) / dictSize);
        }
        letterFreqs.put('~', 1.0);

        this.frequencyTable = letterFreqs;
        System.out.printf("Letter frequencies analysed for %d words\n", dictionary.size());
        for (char letter : letterFreqs.keySet()) {
            System.out.println(letter + ": " + letterFreqs.get(letter));
        }
    }

    public TrieNode getTrieRoot() {
        return dictTrie.getRootTrieNode();
    }

    public Map<Character, Double> getFrequencyTable() {
        return this.frequencyTable;
    }
}
