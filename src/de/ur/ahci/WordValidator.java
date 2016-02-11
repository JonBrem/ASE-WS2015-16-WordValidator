package de.ur.ahci;

import de.ur.ahci.build_probabilities.NGramProbability;
import de.ur.ahci.model.Frame;
import de.ur.ahci.string_similarity.EqualitySimilarity;

import java.util.*;

public class WordValidator {

    private List<Frame> frameList;
    private Map<String, Map<String, Double>> similarityValues;
    private Map<String, StringProbability> probabilities;
    private StringSimilarityCalculator similarityCalculator;
    private NGramProbability nGramProbability;

    public WordValidator(List<Frame> frameList) {
        this(frameList, new EqualitySimilarity(), null);
    }

    public WordValidator(List<Frame> frameList, StringSimilarityCalculator similarityCalculator) {
        this(frameList, similarityCalculator, null);
    }

    public WordValidator(List<Frame> frameList, NGramProbability nGramProbability) {
        this(frameList, new EqualitySimilarity(), nGramProbability);
    }

    public WordValidator(List<Frame> frameList, StringSimilarityCalculator similarityCalculator, NGramProbability nGramProbability) {
        this.frameList = frameList;
        this.similarityValues = new HashMap<>();
        this.probabilities = new HashMap<>();
        this.similarityCalculator = similarityCalculator;
        this.nGramProbability = nGramProbability;
    }

    public void run() {
        buildWordProbabilities();

        if(nGramProbability != null) {
            applyNGramProbabilities();
        }

        dumpWordProbabilities();
    }

    private void applyNGramProbabilities() {
        for(String key : probabilities.keySet()) {
            StringProbability value = probabilities.get(key);
            String s = value.string;

            double nGramLikelihoods = 1;
            for(int i = 0; i < s.length() - 2; i++) {
                double nGramLikelihood = nGramProbability.getProbability(s.substring(i, i + 3));
                if(nGramLikelihood == 0) nGramLikelihood = 1 / (double) nGramProbability.getTotal();
                nGramLikelihoods *= nGramLikelihood;
            }
            value.probability *= nGramLikelihoods;
        }
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

    private void dumpWordProbabilities() {
        List<StringProbability> stringProbabilities = new ArrayList<>(probabilities.values());
        Collections.sort(stringProbabilities);
        Collections.reverse(stringProbabilities);
        for(StringProbability sp : stringProbabilities) {
            System.out.println(sp.string + "\t" + sp.probability);
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

    private class StringProbability implements Comparable<StringProbability> {
        public String string;
        public double probability;

        public StringProbability(String string, double probability) {
            this.string = string;
            this.probability = probability;
        }

        @Override
        public int compareTo(StringProbability stringProbability) {
            return Double.compare(probability, stringProbability.probability);
        }
    }


}
