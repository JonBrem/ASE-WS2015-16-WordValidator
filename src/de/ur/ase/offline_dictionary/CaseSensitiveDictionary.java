package de.ur.ase.offline_dictionary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * The CaseSensitiveDictionary contains a set of words/strings and can check whether or not a word/string is contained in it.
 */
public class CaseSensitiveDictionary implements OfflineDictionary {

    private Set<String> words;

    /**
     * Instantiates a new (empty) dictionary.
     */
    public CaseSensitiveDictionary() {
        words = new HashSet<>();
    }

    /**
     * @param file Every line from this file will be added to the dictionary.
     */
    public void buildFromFile(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line;
            while((line = r.readLine()) != null) {
                addWord(line);
            }
            r.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a word to the dictionary.
     * Note: To add all lines from a file, use {@link #buildFromFile(String)}.
     * @param word the word you want to add
     */
    public void addWord(String word) {
        words.add(word);
    }

    /**
     * @param word any string
     * @return whether or not the dictionary contains the word (case-sensitive!)
     */
    public boolean contains(String word) {
        return words.contains(word);
    }

}
