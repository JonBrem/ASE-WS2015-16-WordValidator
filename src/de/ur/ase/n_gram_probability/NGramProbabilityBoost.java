package de.ur.ase.n_gram_probability;

import de.ur.ase.model.StringProbability;

import java.util.Collection;

/**
 * The NGramProbabilityBoost changes the probability values of StringProbabilities based on the likelihood
 * of the character chains of their strings.
 */
public class NGramProbabilityBoost {

    /** if a character chain is less likely than {@link #SMALL_PROBABILITY} of the average probability, it is considered small */
    public static final double SMALL_PROBABILITY = 0.05;
    /** if a character chain is more likely than {@link #LARGE_PROBABILITY} of the average probability, it is considered large */
    public static final double LARGE_PROBABILITY = 20;
    /** if a character chain is less likely than {@link #SMALL_PROBABILITY} of the average probability, its StringProbabilities probability will be multiplied by this */
    public static final double SMALL_PROBABILITY_BOOST = 0.7;
    /** if a character chain is more likely than {@link #LARGE_PROBABILITY} of the average probability, its StringProbabilities probability will be multiplied by this */
    public static final double LARGE_PROBABILITY_BOOST = 1.05;

    private NGramProbability nGramProbability;

    /**
     * The NGramProbabilityBoost changes the probability values of StringProbabilities based on the likelihood
     * of the character chains of their strings.
     */
    public NGramProbabilityBoost(NGramProbability probability) {
        this.nGramProbability = probability;
    }

    /**
     * Applies boosts based on the likelihood of character chains.
     * @param words
     * List of String Probabilities to boost
     */
    public void boostAll(Collection<StringProbability> words) {
        words.forEach(this::boostValue);
    }

    /**
     * Applies boost based on the likelihood of the character chains in the StringProbabilities word
     * @param word
     * any StringProbability
     */
    public void boostValue(StringProbability word) {
        double nGramAverage = nGramProbability.averageProbability() / nGramProbability.getTotal();
        for(int i = 0; i < word.getString().length() - 2; i++) {
            double nGramLikelihood = nGramProbability.getProbability(word.getString().substring(i, i + 3).toLowerCase());
            if(nGramLikelihood == 0) nGramLikelihood = 1 / (double) nGramProbability.getTotal();

            if(nGramLikelihood <= SMALL_PROBABILITY * nGramAverage) {
                word.setProbability(word.getProbability() * SMALL_PROBABILITY_BOOST);
            } else if (nGramLikelihood >= LARGE_PROBABILITY * nGramAverage) {
                word.setProbability(word.getProbability() * LARGE_PROBABILITY_BOOST);
            }
        }
    }

}
