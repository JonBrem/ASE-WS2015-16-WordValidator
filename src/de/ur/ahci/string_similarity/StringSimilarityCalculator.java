package de.ur.ahci.string_similarity;

/**
 * Calculates the reverse of any kind of String distance (i.e., how similar two Strings are, 1 being a perfect match).
 */
public interface StringSimilarityCalculator {

    double getSimilarity(String word1, String word2);

}
