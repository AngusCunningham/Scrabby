package com.guscodes.scrabby;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Word implements Comparable {

    private String word;
    private int score = 0;
    private int[] locations;
    private int rating;

    private char orientation;
    private List<String> lettersUsedFromTray;

    public Word(String word, int score, int[] locations) {
        this.locations = locations;
        this.score = score;
        this.word = word;
    }

    public Word(String word, int[] locations, char orientation) {
        this.word = word;
        this.locations = locations;
        this.orientation = orientation;
    }

    public Word(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return Objects.equals(word, word1.word) && Arrays.equals(locations, word1.locations);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(word);
        result = 31 * result + Arrays.hashCode(locations);
        return result;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setLocations(int[] locations) {
        this.locations = locations;
    }

    public String getWord() {
        return word;
    }

    public int getScore() {
        return score;
    }

    public int[] getLocations() {
        return locations;
    }

    public char getOrientation() {
        return orientation;
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public int compareTo(Object o) {
        Word otherWord = (Word) o;
        return this.getRating() - otherWord.getRating();
    }
}

