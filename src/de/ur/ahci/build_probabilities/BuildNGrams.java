package de.ur.ahci.build_probabilities;

import java.util.*;

public class BuildNGrams {

    private int nGramLength;
    private Map<String, Integer> numOccurences;

    public BuildNGrams(int nGramLength) {
        this.nGramLength = nGramLength;
        this.numOccurences = new HashMap<>();
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
        if(word.length() >= nGramLength) {
            for(int i = 0; i < word.length() - nGramLength + 1; i++) {
                String nGram = word.substring(i, i + nGramLength);

                if(!numOccurences.containsKey(nGram)) {
                    numOccurences.put(nGram, 1);
                } else {
                    numOccurences.put(nGram, numOccurences.get(nGram) + 1);
                }
            }
        }
    }

    public void debugDump() {
        List<NGramProbability> nGrams = new ArrayList<>();
        long total = 0;
        for(String key : numOccurences.keySet()) {
            NGramProbability p = new NGramProbability(key, numOccurences.get(key));
            total += numOccurences.get(key);
            nGrams.add(p);
        }

        Collections.sort(nGrams);
        for(NGramProbability p : nGrams) {
            System.out.println(p.getnGram() + "\t" + p.getNumOccurences() + "\t" + p.getProbability(total) * 100.0 + "%");
        }
    }

}
