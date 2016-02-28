package de.ur.ahci.string_similarity;

public class EqualitySimilarity implements StringSimilarityCalculator {
    @Override
    public double getSimilarity(String word1, String word2) {
        return word1.equals(word2)? 1 : 0;
    }
}
