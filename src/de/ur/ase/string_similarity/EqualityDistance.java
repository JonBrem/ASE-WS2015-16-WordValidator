package de.ur.ase.string_similarity;

public class EqualityDistance implements StringDistanceCalculator {
    @Override
    public double getDistance(String word1, String word2) {
        return word1.equals(word2)? 1 : 0;
    }
}
