package de.ur.ase.model;

/**
 * model class that contains a string and a double (the likelihood / value of the ring, compared to other strings that were possibly recognized in a video)
 */
public class StringProbability implements Comparable<StringProbability> {
    private String string;
    private double probability;

    /**
     * model class that contains a string and a double (the likelihood / value of the ring, compared to other strings that were possibly recognized in a video)
     */
    public StringProbability(String string, double probability) {
        this.string = string;
        this.probability = probability;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public int compareTo(StringProbability stringProbability) {
        return Double.compare(probability, stringProbability.probability);
    }
}
