package de.ur.ase.offline_dictionary;

import de.ur.ase.model.StringProbability;
import de.ur.ase.WordValidator;

import java.util.Collection;

/**
 * Boosts StringProbabilities using an CaseSensitiveDictionary.
 */
public class OfflineDictionaryBoost {

    public static double BOOST_WORD_FOUND = 10.0;
    public static double BOOST_WORD_NOT_FOUND = .9;

    private OfflineDictionary dictionary;

    public OfflineDictionaryBoost(OfflineDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Applies the boosts (specified by the constants {@link #BOOST_WORD_FOUND} and {@link #BOOST_WORD_NOT_FOUND} to
     * every StringProbability in the Collection.
     * @param stringProbabilities
     * Collection of StringProbabilities
     */
    public void boostAll(Collection<StringProbability> stringProbabilities) {
        stringProbabilities.forEach(this::boost);
    }

    /**
     * Applies the boosts (specified by the constants {@link #BOOST_WORD_FOUND} and {@link #BOOST_WORD_NOT_FOUND} to
     * the StringProbability
     * @param stringProbability
     * A StringProbability, likely created by the {@link WordValidator}
     */
    public void boost(StringProbability stringProbability) {
        if(dictionary.contains(stringProbability.getString())) {
            stringProbability.setProbability(stringProbability.getProbability() * BOOST_WORD_FOUND);
        } else {
            stringProbability.setProbability(stringProbability.getProbability() * BOOST_WORD_NOT_FOUND);
        }
    }

}
