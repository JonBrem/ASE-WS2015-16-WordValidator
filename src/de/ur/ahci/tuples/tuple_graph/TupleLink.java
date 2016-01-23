package de.ur.ahci.tuples.tuple_graph;

public class TupleLink {

    private TupleNode start;
    private TupleNode end;

    private float probabiltiy;

    public TupleLink(TupleNode start, TupleNode end) {
        this.start = start;
        this.end = end;

        probabiltiy = 0;
    }

    public TupleNode getStart() {
        return start;
    }

    public void setStart(TupleNode start) {
        this.start = start;
    }

    public TupleNode getEnd() {
        return end;
    }

    public void setEnd(TupleNode end) {
        this.end = end;
    }

    public float getProbabiltiy() {
        return probabiltiy;
    }

    public void setProbabiltiy(float probabiltiy) {
        this.probabiltiy = probabiltiy;
    }
}
