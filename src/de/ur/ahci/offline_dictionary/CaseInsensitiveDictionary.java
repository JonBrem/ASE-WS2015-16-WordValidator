package de.ur.ahci.offline_dictionary;

import java.util.HashSet;
import java.util.Set;

/**
 * The CaseSensitiveDictionary contains a set of words/strings and can check whether or not a word/string is contained in it.
 */
public class CaseInsensitiveDictionary extends CaseSensitiveDictionary {

    private Set<String> words;

    /**
     * Instantiates a new (empty) dictionary.
     */
    public CaseInsensitiveDictionary() {
        words = new HashSet<>();
    }

    /**
     * adds a word to the dictionary.
     * Note: To add all lines from a file, use {@link #buildFromFile(String)}.
     * @param word the word you want to add
     */
    @Override
    public void addWord(String word) {
        words.add(word.toLowerCase());
    }

    /**
     * @param word any string
     * @return whether or not the dictionary contains the word (case-insensitive!)
     */
    public boolean contains(String word) {
        return words.contains(word.toLowerCase());
    }

}
