package de.ur.ase.join_filter;

import de.ur.ase.model.StringProbability;
import de.ur.ase.string_similarity.StringDistanceCalculator;

import java.util.*;

/**
 * The JoinWords routine determines which words are similar or parts of other words
 * and most likely belong to / refer to the same "actual" word in a video.
 */
public class JoinWords {

    /**
     * The JoinWords routine determines which words are similar or parts of other words
     * and most likely belong to / refer to the same "actual" word in a video.
     *
     * @param probabilities
     * List of words/probabilities up to this step.
     * @param distanceCalculator
     * String distance calculator, e.g. {@link de.ur.ase.string_similarity.NeedlemanWunschDistance}
     * @return
     * A List of Sets; the sets contain word alternatives (similar strings)
     *
     */
    public List<Set<StringProbability>> joinWords(List<StringProbability> probabilities, StringDistanceCalculator distanceCalculator) {
        List<Set<StringProbability>> joins = new ArrayList<>();
        Map<StringProbability, Set<StringProbability>> setIndex = new HashMap<>();
        for(int i = 0; i < probabilities.size(); i++) {
            forEveryStringProbability(probabilities, distanceCalculator, joins, setIndex, i);
        }
        return joins;
    }

    /**
     * Decomposition method that gets called for every string probability in the list (hence the name).
     *
     * @param probabilities
     * List of original String Probabilities (before this step)
     * @param distanceCalculator
     * String distance calculator, e.g. {@link de.ur.ase.string_similarity.NeedlemanWunschDistance}
     * @param joins
     * The Sets of Word Alternatives will be added to this list ("output" param)
     * @param setIndex
     * "index" (as in a "dictionary") of existing Word Alternative Sets.
     * @param i
     * Current List index
     */
    private void forEveryStringProbability(List<StringProbability> probabilities, StringDistanceCalculator distanceCalculator, List<Set<StringProbability>> joins, Map<StringProbability, Set<StringProbability>> setIndex, int i) {
        StringProbability current = probabilities.get(i);
        boolean partOfSet = false;

        for(int j = 0; j < i; j++) {
            partOfSet = forEveryOtherStringProbability(probabilities, distanceCalculator, setIndex, current, partOfSet, j);
        }

        if(!partOfSet) {
            Set<StringProbability> newSet = new HashSet<>();
            newSet.add(current);
            setIndex.put(current, newSet);
            joins.add(newSet);
        }
    }

    /**
     * Decomposition method used to compare a StringProbability to all of the others (hence the name)
     *
     * @param probabilities
     * List of original String Probabilities (before this step)
     * @param distanceCalculator
     * String distance calculator, e.g. {@link de.ur.ase.string_similarity.NeedlemanWunschDistance}
     * @param setIndex
     * "index" (as in a "dictionary") of existing Word Alternative Sets.
     * @param current
     * not the "other", but the current StringProbability
     * @param partOfSet
     * whether or not the current item is already part of a Set.
     * @param j
     * List index of "other" item
     * @return
     * Returns the original value of the "partOfSet" parameter if nothing changed or "true" if "current" is part
     * of the set it was compared to in this method. Is <em>never</em> explicitly set to false; can only return false
     * if it was already false!!
     */
    private boolean forEveryOtherStringProbability(List<StringProbability> probabilities, StringDistanceCalculator distanceCalculator, Map<StringProbability, Set<StringProbability>> setIndex, StringProbability current, boolean partOfSet, int j) {
        StringProbability other = probabilities.get(j);
        double similarity = getSimilarity(current.getString(), other.getString(), distanceCalculator);
        if(similarity <= 0.5) {
            setIndex.get(other).add(current);
            setIndex.put(current, setIndex.get(other));
            partOfSet = true;
        }
        if(isPartOf(current.getString(), other.getString())) {
            setIndex.get(other).add(current);
        }
        return partOfSet;
    }

    /**
     * Calculates the Similarity between two Strings with a given StringDistanceCalculator
     * @param s1
     * One of the two Strings
     * @param s2
     * The other String
     * @param distanceCalculator
     * String distance calculator, e.g. {@link de.ur.ase.string_similarity.NeedlemanWunschDistance}
     * @return
     * The similarity value calculated by the StringDistanceCalculator
     */
    private static double getSimilarity(String s1, String s2, StringDistanceCalculator distanceCalculator) {
        return distanceCalculator.getDistance(s1, s2);
    }

    /**
     * Whether or not w2 contains w1
     * @param w1
     * some String
     * @param w2
     * some other String
     * @return
     * Whether or not w2 contains w1
     */
    private static boolean isPartOf(String w1, String w2) {
        return w2.contains(w1);
    }

}
