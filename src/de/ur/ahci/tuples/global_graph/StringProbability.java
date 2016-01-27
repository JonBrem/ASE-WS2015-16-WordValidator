package de.ur.ahci.tuples.global_graph;

/**
 * Created by jonbr on 27.01.2016.
 */
public class StringProbability implements Comparable<StringProbability> {

    private String string;
    private double probability;

    public StringProbability(String string, double probability) {
        this.setString(string);
        this.setProbability(probability);
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public int compareTo(StringProbability another) {
        int probCompare = Double.compare(probability, another.getProbability());
        if(probCompare != 0) return probCompare;

        else return string.compareTo(another.getString());
    }
}
