package de.ur.ase.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The recognition data for one video frame.
 * <br>
 * Model class, contains mostly getters and setters.
 */
public class Frame {

    private int number;
    private List<String> words;

    /**
     * The recognition data for one video frame.
     * <br>
     * Model class, contains mostly getters and setters.
     */
    public Frame() {
        words = new ArrayList<>();
    }

    /**
     * @return
     * The frame number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number
     * Sets the frame number to that number.
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return
     * The List of Words in the Frame
     */
    public List<String> getWords() {
        return words;
    }

    /**
     * @param word
     * Adds the word to the List of words in this frame
     */
    public void addWord(String word) {
        this.words.add(word);
    }
}
