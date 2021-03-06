package de.ur.ase.join_filter;

import de.ur.ase.model.StringProbability;
import de.ur.ase.string_similarity.StringDistanceCalculator;

import java.util.*;

/**
 * The FilterWords class decides which words are too unlikely or too similar to other, more likely words to be sent
 * to the server (so as not to over-use the APIs we are using not-entirely-legally)
 */
public class FilterWords {

    /**
     * The entry point for this step. Removes some words from the set if they are too unlikely.
     *
     * @param sets
     * List of alternatives of possible recognitions.
     * @param totalNumRecognitions
     * How many words the detection program recognized across all frames, in total.
     * @return
     * The items in the List (the one passed as param is unaltered!) with, presumably, fewer Sets or fewer items in the
     * Sets.
     */
    public Set<Set<StringProbability>> filterWords(List<Set<StringProbability>> sets, int numFrames, int totalNumRecognitions, StringDistanceCalculator distanceCalculator) {
        Set<Set<StringProbability>> stringProbabilities = new HashSet<>();

        for(Set<StringProbability> set : sets) {
            Set<StringProbability> probabilities = getMostLikelyWords(set, totalNumRecognitions);
            stringProbabilities.add(probabilities);
        }

//        trimIfTooManyPossibilities(stringProbabilities, distanceCalculator);

        return stringProbabilities;
    }

    /**
     * Determines whether or not a StringProbability is very likely (and should definitely be sent to the server,
     * even if its alternatives are also very likely)
     *
     * @param totalNumRecognitions
     * How many words the detection program recognized across all frames, in total.
     * @param stringProbability
     * The StringProbabily in question
     * @return
     * whether or not the word is very likely
     */
    private boolean veryLikely(int totalNumRecognitions, StringProbability stringProbability) {
        return stringProbability.getProbability() >= totalNumRecognitions / 4;
    }

    /**
     * Determines whether or not a StringProbability is too unlikely to be sent to the server
     * (because any word that actually exists would have a higher value & Strings that are in multiple frames
     * should have a higher probability)
     *
     * @param totalNumRecognitions
     * How many words the detection program recognized across all frames, in total.
     * @param stringProbability
     * The StringProbabily in question
     * @return
     * whether or not the word is too unlikely to even need to be verified by the server.
     */
    private boolean tooUnlikely(int totalNumRecognitions, StringProbability stringProbability) {
        if(totalNumRecognitions <= 50) {
            return stringProbability.getProbability() < totalNumRecognitions / 20;
        }
        return stringProbability.getProbability() < totalNumRecognitions / 15;
    }

    /**
     * Gets the most likely words from the set and filters out the unlikely ones.
     *
     * @param wordAlternatives
     * Set of word alternatives (very similar Strings) for the text recognitions.
     * @param totalNumRecognitions
     * How many words the detection program recognized across all frames, in total.
     * @return
     * Set of StringProbabilities containing the likely words in the given Set.
     * May be empty, is never null.
     */
    private Set<StringProbability> getMostLikelyWords(Set<StringProbability> wordAlternatives, int totalNumRecognitions) {
        Set<StringProbability> likelyWords = new HashSet<>();

        for(StringProbability stringProbability : wordAlternatives) {
            if(tooUnlikely(totalNumRecognitions, stringProbability)) continue; // just based on looking at the data
            else if (tooShort(stringProbability)) continue; // simply unlikely for those to be relevant...

            boolean anotherIsFarMoreLikely = false;
            for(StringProbability otherProbability : wordAlternatives) {
                if(stringProbability == otherProbability) continue;

                if(!tooShort(otherProbability) && otherProbability.getProbability() >= 8 * stringProbability.getProbability()) {
                    anotherIsFarMoreLikely = true;
                }
            }
            if(!anotherIsFarMoreLikely || veryLikely(totalNumRecognitions, stringProbability)) {
                likelyWords.add(stringProbability);
            }

        }

        return likelyWords;
    }

    private boolean tooShort(StringProbability stringProbability) {
        return stringProbability.getString().length() <= 3;
    }

    private static int totalNumberOfAlternatives(Set<Set<StringProbability>> mostLikelyWords) {
        final int[] total = {0};
        mostLikelyWords.forEach(set -> {
            set.forEach(sp -> {
                total[0] += 1;
            });
        });
        return total[0];
    }

    // was an idea we had because some videos have waaaay too many tags. worked fine on one test video but we decided that was by chance & cut this method.
//    private void trimIfTooManyPossibilities(Set<Set<StringProbability>> stringProbabilities, StringDistanceCalculator distanceCalculator) {
//        if(stringProbabilities.size() > 15 || totalNumberOfAlternatives(stringProbabilities) > 50) {
//            List<Double> totals = new ArrayList<>();
//            for(Set<StringProbability> set : stringProbabilities) {
//                double total = 0;
//                for(StringProbability sp : set) {
//                    total += sp.probability;
//                }
//                totals.add(total);
//            }
//            if(totals.size() == 0) return;
//
//            Collections.sort(totals);
//            Collections.reverse(totals);
//
//            double threshold;
//            if(stringProbabilities.size() > 15) {
//                threshold = totals.get(15);
//            } else {
//                threshold = totals.get(totals.size() / 2);
//            }
//
//
//            Iterator<Set<StringProbability>> iterator = stringProbabilities.iterator();
//            while(iterator.hasNext()) {
//                Set<StringProbability> set = iterator.next();
//                double total = 0;
//                for(StringProbability sp : set) {
//                    total += sp.probability;
//                }
//                if(total < threshold) iterator.remove();
//            }
//
//        }
//    }

}
