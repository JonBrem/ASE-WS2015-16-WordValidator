package de.ur.ase;

import de.ur.ase.model.Frame;
import de.ur.ase.model.StringProbability;
import de.ur.ase.string_similarity.StringDistanceCalculator;

import java.util.*;

public class WordValidator {

    private List<Frame> frameList;
    private Map<String, Map<String, Double>> distanceValues;
    private Map<String, StringProbability> probabilities;
    private StringDistanceCalculator distanceCalculator;

    public WordValidator(List<Frame> frameList, StringDistanceCalculator distanceCalculator) {
        this.frameList = frameList;
        this.distanceValues = new HashMap<>();
        this.probabilities = new HashMap<>();
        this.distanceCalculator = distanceCalculator;
    }

    public void run() {
        for(int frameIndex = 0; frameIndex < frameList.size(); frameIndex++) {
            Frame frame = frameList.get(frameIndex);
            List<String> wordsInFrame = frame.getWords();

            for(int wordIndex = 0; wordIndex < wordsInFrame.size(); wordIndex++) {
                String word = wordsInFrame.get(wordIndex);

                for(int otherFrameIndex = frameIndex; otherFrameIndex < frameList.size(); otherFrameIndex++) {
                    for(String otherWord : frameList.get(otherFrameIndex).getWords()) {
                        int indexOfOtherWordInOriginalList = wordsInFrame.indexOf(otherWord);
                        if(indexOfOtherWordInOriginalList == -1 || indexOfOtherWordInOriginalList == wordIndex) {
                            compareWords(word, otherWord);
                        }
                    }
                }
            }
        }
    }

    private double getDistance(String word, String otherWord) {
        double distance = -1;

        if(distanceValues.containsKey(word)) {
            Map<String, Double> map = distanceValues.get(word);
            if(map.containsKey(otherWord)) {
                distance = map.get(otherWord);
            }
        }

        if(distance == -1) {
            distance = distanceCalculator.getDistance(word, otherWord);
        }

        putValueInDistanceValues(word, otherWord, distance);
        putValueInDistanceValues(otherWord, word, distance);

        return distance;
    }

    private void putValueInDistanceValues(String word, String otherWord, double distance) {
        if(distanceValues.containsKey(word)) {
            distanceValues.get(word).put(otherWord, distance);
        } else {
            Map<String, Double> map = new HashMap<>();
            map.put(otherWord, distance);
            distanceValues.put(word, map);
        }
    }

    private void compareWords(String word, String otherWord) {
        double distance = getDistance(word, otherWord);
        if(distance == 0) {
            boostWord(word, 1);
        } else {
            if(distance <= 1) {
                boostWord(word, 1 - distance);
                boostWord(otherWord, 1 - distance);
            }
        }
    }

    private void boostWord(String word, double boost) {
        if(!probabilities.containsKey(word)) {
            probabilities.put(word, new StringProbability(word, boost));
        } else {
            probabilities.get(word).probability += boost;
        }
    }


    public List<StringProbability> getStringProbabilities() {
        return new ArrayList<>(probabilities.values());
    }
}
