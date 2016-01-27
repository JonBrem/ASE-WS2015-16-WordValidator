package de.ur.ahci.build_probabilities;

import java.util.*;

public class NGramProbability {

    private int nGramLength;
    private Map<String, Integer> numOccurences;
    private long total;

    public NGramProbability(int nGramLength) {
        this.nGramLength = nGramLength;
        this.numOccurences = new HashMap<>();
        total = 0;
    }

    public void readWords(String... words) {
        for(String word : words) {
            parseWord(word);
        }
    }

    public void readWords(Collection<String> words) {
        for(String word : words) {
            parseWord(word);
        }
    }

    private void parseWord(String word) {
        word = word.toLowerCase();
        if(word.length() >= nGramLength) {
            for(int i = 0; i < word.length() - nGramLength + 1; i++) {
                String nGram = word.substring(i, i + nGramLength);

                if(!numOccurences.containsKey(nGram)) {
                    numOccurences.put(nGram, 1);
                } else {
                    numOccurences.put(nGram, numOccurences.get(nGram) + 1);
                }
                total++;
            }
        }
    }

    public double getProbability(String ngram) {
        if(numOccurences.containsKey(ngram)) {
            return numOccurences.get(ngram) / (double) total;
        } else {
            return 0;
        }
    }

    public double averageProbability() {
        return numOccurences.keySet().size() / (double) total;
    }
}
