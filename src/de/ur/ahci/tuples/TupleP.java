package de.ur.ahci.tuples;

public class TupleP {

    private Tuple tuple;
    private float probability;

    public TupleP(Tuple tuple) {
        this.tuple = tuple;
    }

    public TupleP(Tuple tuple, float probability) {
        this.probability = probability;
        this.tuple = tuple;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }
}
