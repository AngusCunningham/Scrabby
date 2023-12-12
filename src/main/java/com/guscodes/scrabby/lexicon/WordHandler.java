package com.guscodes.scrabby.lexicon;

import java.io.InputStream;
import java.util.*;

public class WordHandler {
    private Map<String, String> definedDictionary = new HashMap<>();
    private Trie dictTrie;

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
