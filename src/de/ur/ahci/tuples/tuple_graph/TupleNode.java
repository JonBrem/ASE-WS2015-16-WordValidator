package de.ur.ahci.tuples.tuple_graph;

import de.ur.ahci.tuples.Tuple;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TupleNode {

    private Tuple tuple;
    private Set<TupleLink> outLinks;
    private Set<TupleLink> inLinks;

    private boolean isStart, isEnd;

    public TupleNode(Tuple tuple) {
        this.tuple = tuple;
        this.outLinks = new HashSet<>();
        this.inLinks = new HashSet<>();
        this.isStart = false;
        this.isEnd = false;
    }

    public boolean hasLinks() {
        return outLinks != null && outLinks.size() > 0;
    }

    public void makeStart() {
        isStart = true;
        isEnd = false;
    }

    public void makeEnd() {
        isEnd = true;
        isStart = false;
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void addLinkTo(TupleNode node) {
        this.outLinks.add(new TupleLink(this, node));
        node.addLinkFrom(this);
    }

    public void addLinkFrom(TupleNode node) {
        this.inLinks.add(new TupleLink(node, this));
    }

    public boolean hasLinkTo(Tuple tuple) {
        for(TupleLink link : outLinks) {
            if(link.getEnd().equals(tuple)) return true;
        }

        return false;
    }

    public Set<TupleLink> getOutLinks() {
        return outLinks;
    }

    public Set<TupleLink> getInLinks() {
        return inLinks;
    }

    public Set<TupleNode> getOutLinkedNodes() {
        // fancy java 8 :)
        return outLinks.stream().map(TupleLink::getEnd).collect(Collectors.toSet());
    }

    public Set<TupleNode> getInLinkedNodes() {
        return inLinks.stream().map(TupleLink::getStart).collect(Collectors.toSet());
    }

    /**
     *
     * @return
     * <ul>
     *     <li>null if there are no nodes after this (isEnd is not necessarily set for this during graph construction)</li>
     *     <li>Set of end nodes, constructed recursively</li>
     * </ul>
     */
    public Set<TupleNode> findEndNodes() {
        if(isEnd || outLinks.size() == 0) return null;
        Set<TupleNode> endNodes = new HashSet<>();
        for(TupleLink link : outLinks) {
            Set<TupleNode> endNodesOfNode = link.getEnd().findEndNodes();
            if(endNodesOfNode != null) endNodes.addAll(link.getEnd().findEndNodes());
            else endNodes.add(link.getEnd());
        }
        return endNodes;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public void setTuple(Tuple tuple) {
        this.tuple = tuple;
    }

    public Set<String> buildStringsFromThisPoint() {
        if(this.isEnd) return new HashSet<>();

        String thisString = "";
        if(tuple != null && tuple.getString() != null) {
            thisString = Character.toString(tuple.getString().charAt(0));
        }

        Set<String> nextStrings = new HashSet<>();
        for(TupleLink link : outLinks) {
            Set<String> stringsFromTuple = link.getEnd().buildStringsFromThisPoint();

            if(stringsFromTuple.size() == 0) {
                if(tuple != null && tuple.getString() != null) {
                    nextStrings.add(tuple.getString());
                }
            } else {
                for(String s : stringsFromTuple) {
                    nextStrings.add(thisString + s);
                }
            }
        }

        return nextStrings;
    }

}
