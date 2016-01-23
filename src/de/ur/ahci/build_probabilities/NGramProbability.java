package de.ur.ahci.build_probabilities;

public class NGramProbability implements Comparable<NGramProbability> {

    private String nGram;
    private int numOccurences;


    public NGramProbability(String nGram, int numOccurences) {
        this.nGram = nGram;
        this.numOccurences = numOccurences;
    }

    public float getProbability(long total) {
        return numOccurences / (float) total;
    }

    public int getNumOccurences() {
        return numOccurences;
    }

    public void setNumOccurences(int numOccurences) {
        this.numOccurences = numOccurences;
    }

    public String getnGram() {
        return nGram;
    }

    public void setnGram(String nGram) {
        this.nGram = nGram;
    }

    @Override
    public int compareTo(NGramProbability o) {
        return - Integer.compare(numOccurences, o.getNumOccurences());
    }
}
