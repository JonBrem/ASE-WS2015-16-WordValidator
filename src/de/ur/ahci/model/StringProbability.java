package de.ur.ahci.model;

/**
 * model class that contains a string and a double (the likelihood / value of the ring, compared to other strings that were possibly recognized in a video)
 */
public class StringProbability implements Comparable<StringProbability> {
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
