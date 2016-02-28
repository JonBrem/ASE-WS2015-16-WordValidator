package de.ur.ahci.offline_dictionary;

/**
 * The OfflineDictionary contains a set of words/strings and can check whether or not a word/string is contained in it.
 */
public interface OfflineDictionary {

    /**
     * @param word any string
     * @return whether or not the dictionary contains the word
     */
    public boolean contains(String word);

}
