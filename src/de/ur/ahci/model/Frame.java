package de.ur.ahci.model;

import java.util.ArrayList;
import java.util.List;

public class Frame {

    private int number;
    private List<String> words;

    public Frame() {
        words = new ArrayList<>();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<String> getWords() {
        return words;
    }

    public void addWord(String word) {
        this.words.add(word);
    }
}
