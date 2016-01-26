package de.ur.ahci.tuples.tuple_graph;

import de.ur.ahci.tuples.Tuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public void addLinkTo(TupleNode node, float probability) {
        this.outLinks.add(new TupleLink(this, node, probability));
        node.addLinkFrom(this, probability);
    }

    public void addLinkFrom(TupleNode node, float probability) {
        this.inLinks.add(new TupleLink(node, this, probability));
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

    public Set<List<TupleNode>> backTrack(int steps) {
        Set<List<TupleNode>> paths = new HashSet<>();
        if(isStart || steps == 0) return paths;

        boolean onlyParentIsStart = true;
        for(TupleLink linkToParent : inLinks) if(!linkToParent.getStart().isStart()) onlyParentIsStart = false;

        if(steps == 1 || onlyParentIsStart) {
            List<TupleNode> thisList = new ArrayList<>();
            thisList.add(this);
            Set<List<TupleNode>> thisSet = new HashSet<>();
            thisSet.add(thisList);
            return thisSet;
        }

        for(TupleLink linkToParent : inLinks) {
            TupleNode parent = linkToParent.getStart();
            Set<List<TupleNode>> pathsFromParent = parent.backTrack(steps - 1);
            for(List<TupleNode> listFromParent : pathsFromParent) {
                listFromParent.add(0, this);
            }
            paths.addAll(pathsFromParent);
        }

        return paths;
    }
}
