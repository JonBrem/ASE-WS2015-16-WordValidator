package de.ur.ahci.string_similarity;

/**
 * Calculates String distances (i.e., how similar two Strings are, 0 being a perfect match).
 */
public interface StringSimilarityCalculator {

    double getSimilarity(String word1, String word2);

}
