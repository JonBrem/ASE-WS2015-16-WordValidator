package de.ur.ase.string_similarity;

/**
 * Calculates String distances (i.e., how similar two Strings are, 0 being a perfect match).
 */
public interface StringDistanceCalculator {

    double getDistance(String word1, String word2);

}
