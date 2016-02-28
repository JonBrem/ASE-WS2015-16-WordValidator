package de.ur.ahci;

import de.ur.ahci.model.Frame;
import de.ur.ahci.model.StringProbability;
import de.ur.ahci.string_similarity.StringSimilarityCalculator;

import java.util.*;

public class WordValidator {

    private List<Frame> frameList;
    private Map<String, Map<String, Double>> similarityValues;
    private Map<String, StringProbability> probabilities;
    private StringSimilarityCalculator similarityCalculator;

    public WordValidator(List<Frame> frameList, StringSimilarityCalculator similarityCalculator) {
        this.frameList = frameList;
        this.similarityValues = new HashMap<>();
        this.probabilities = new HashMap<>();
        this.similarityCalculator = similarityCalculator;
    }

    public void run() {
        buildWordProbabilities();
    }

    private double getSimilarity(String word, String otherWord) {
        double similarity = -1;

        if(similarityValues.containsKey(word)) {
            Map<String, Double> map = similarityValues.get(word);
            if(map.containsKey(otherWord)) {
                similarity = map.get(otherWord);
            }
        }

        if(similarity == -1) {
            similarity = similarityCalculator.getSimilarity(word, otherWord);
        }

        putValueInSimilarityValues(word, otherWord, similarity);
        putValueInSimilarityValues(otherWord, word, similarity);

        return similarity;
    }

    private void putValueInSimilarityValues(String word, String otherWord, double similarity) {
        if(similarityValues.containsKey(word)) {
            similarityValues.get(word).put(otherWord, similarity);
        } else {
            Map<String, Double> map = new HashMap<>();
            map.put(otherWord, similarity);
            similarityValues.put(word, map);
        }
    }

    private void buildWordProbabilities() {
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

    private void compareWords(String word, String otherWord) {
        double similarity = getSimilarity(word, otherWord);
        if(similarity == 1) {
            boostWord(word, similarity);
        } else {
            boostWord(word, similarity);
            boostWord(otherWord, similarity);
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
