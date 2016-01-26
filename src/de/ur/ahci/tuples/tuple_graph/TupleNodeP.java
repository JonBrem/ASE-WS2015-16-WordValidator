package de.ur.ahci.tuples.tuple_graph;

/**
 * Helper class that stores both a tuple node and a probability.
 */
public class TupleNodeP {

    private TupleNode node;
    private float probability;

    public TupleNodeP(TupleNode node, float probability) {
        this.node = node;
        this.probability = probability;
    }

    public TupleNode getNode() {
        return node;
    }

    public void setNode(TupleNode node) {
        this.node = node;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }
}
