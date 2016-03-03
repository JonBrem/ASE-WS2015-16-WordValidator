package de.ur.ase.join_filter;

import de.ur.ase.model.StringProbability;
import de.ur.ase.string_similarity.StringDistanceCalculator;

import java.util.*;

/**
 * The JoinWords class
 */
public class JoinWords {

    public List<Set<StringProbability>> joinWords(List<StringProbability> probabilities, StringDistanceCalculator similarityCalculator) {
        List<Set<StringProbability>> joins = new ArrayList<>();
        Map<StringProbability, Set<StringProbability>> setIndex = new HashMap<>();
        for(int i = 0; i < probabilities.size(); i++) {
            forEveryStringProbability(probabilities, similarityCalculator, joins, setIndex, i);
        }
        return joins;
    }

    /**
     * Decomposition method
     * @param probabilities
     * @param similarityCalculator
     * @param joins
     * @param setIndex
     * @param i
     */
    private void forEveryStringProbability(List<StringProbability> probabilities, StringDistanceCalculator similarityCalculator, List<Set<StringProbability>> joins, Map<StringProbability, Set<StringProbability>> setIndex, int i) {
        StringProbability current = probabilities.get(i);
        boolean partOfSet = false;

        for(int j = 0; j < i; j++) {
            partOfSet = forEveryOtherStringProbability(probabilities, similarityCalculator, setIndex, current, partOfSet, j);
        }

        if(!partOfSet) {
            Set<StringProbability> newSet = new HashSet<>();
            newSet.add(current);
            setIndex.put(current, newSet);
            joins.add(newSet);
        }
    }

    /**
     * Decomposition method
     *
     * @param probabilities
     * @param similarityCalculator
     * @param setIndex
     * @param current
     * @param partOfSet
     * @param j
     * @return
     */
    private boolean forEveryOtherStringProbability(List<StringProbability> probabilities, StringDistanceCalculator similarityCalculator, Map<StringProbability, Set<StringProbability>> setIndex, StringProbability current, boolean partOfSet, int j) {
        StringProbability other = probabilities.get(j);
        double similarity = getSimilarity(current.string, other.string, similarityCalculator);
        if(similarity <= 0.5) {
            setIndex.get(other).add(current);
            setIndex.put(current, setIndex.get(other));
            partOfSet = true;
        }
        if(isPartOf(current.string, other.string)) {
            setIndex.get(other).add(current);
        }
        return partOfSet;
    }


    private static double getSimilarity(String current, String other, StringDistanceCalculator similarityCalculator) {
        return similarityCalculator.getDistance(current, other);
    }

    private static boolean isPartOf(String w1, String w2) {
        return w2.contains(w1);
    }

}
