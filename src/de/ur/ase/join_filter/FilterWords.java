package de.ur.ase.join_filter;

import de.ur.ase.model.StringProbability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class FilterWords {

    public Set<Set<StringProbability>> filterWords(List<Set<StringProbability>> sets, int numFrames, int totalNumRecognitions) {
        Set<Set<StringProbability>> stringProbabilities = new HashSet<>();

        for(Set<StringProbability> set : sets) {
            Set<StringProbability> probabilities = getMostLikelyWords(set, totalNumRecognitions);
            stringProbabilities.add(probabilities);
        }

        return stringProbabilities;
    }

    private boolean veryLikely(int totalNumRecognitions, StringProbability stringProbability) {
        return stringProbability.probability >= totalNumRecognitions / 4;
    }

    private boolean tooUnlikely(int totalNumRecognitions, StringProbability stringProbability) {
        if(totalNumRecognitions <= 50) {
            return stringProbability.probability < totalNumRecognitions / 20;
        }
        return stringProbability.probability < totalNumRecognitions / 15;
    }

    private Set<StringProbability> getMostLikelyWords(Set<StringProbability> set, int totalNumRecognitions) {
        Set<StringProbability> likelyWords = new HashSet<>();

        for(StringProbability stringProbability : set) {
            if(tooUnlikely(totalNumRecognitions, stringProbability)) continue; // just based on looking at the data
            else if (stringProbability.string.length() <= 3) continue; // simply unlikely for those to be relevant...

            boolean anotherIsFarMoreLikely = false;
            for(StringProbability otherProbability : set) {
                if(stringProbability == otherProbability) continue;
                if(otherProbability.probability >= 1.5 * stringProbability.probability) {
                    anotherIsFarMoreLikely = true;
                }
            }
            if(!anotherIsFarMoreLikely || veryLikely(totalNumRecognitions, stringProbability)) {
                likelyWords.add(stringProbability);
            }

        }

        return likelyWords;
    }

}
