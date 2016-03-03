package de.ur.ase.n_gram_probability;

import de.ur.ase.model.StringProbability;

import java.util.Collection;

/**
 * The NGramProbabilityBoost
 */
public class NGramProbabilityBoost {

    /*
     * This is just one way this can work. We could implement multiple behaviours.
     *
     *
     */


    public static final double SMALL_PROBABILITY = 0.05;
    public static final double LARGE_PROBABILTIY = 20;
    public static final double SMALL_PROBABILITY_BOOST = 0.7;
    public static final double LARGE_PROBABILITY_BOOST = 1.05;

    private NGramProbability nGramProbability;

    public NGramProbabilityBoost(NGramProbability probability) {
        this.nGramProbability = probability;
    }

    public void boostAll(Collection<StringProbability> words) {
        for(StringProbability word : words) boostValue(word);
    }

    public void boostValue(StringProbability word) {
        double nGramAverage = nGramProbability.averageProbability() / nGramProbability.getTotal();
        for(int i = 0; i < word.string.length() - 2; i++) {
            double nGramLikelihood = nGramProbability.getProbability(word.string.substring(i, i + 3).toLowerCase());
            if(nGramLikelihood == 0) nGramLikelihood = 1 / (double) nGramProbability.getTotal();

            if(nGramLikelihood <= SMALL_PROBABILITY * nGramAverage) {
                word.probability *= SMALL_PROBABILITY_BOOST;
            } else if (nGramLikelihood >= LARGE_PROBABILTIY * nGramAverage) {
                word.probability *= LARGE_PROBABILITY_BOOST;
            }
        }
    }

}
