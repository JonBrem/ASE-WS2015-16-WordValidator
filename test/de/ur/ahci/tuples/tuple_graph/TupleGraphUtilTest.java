package de.ur.ahci.tuples.tuple_graph;

import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TupleGraphUtilTest {

    @Test
    public void testGraph() {

        TupleGraph graph = TupleGraphUtil.constructGraph("FAMILIE", 3);
        TupleNode first = graph.start;
        first.buildStringsFromThisPoint().forEach(System.out::println);

//        printAll(first, "\t");

    }

    private void printAll(TupleNode first, String s) {
        for(TupleNode node : first.getOutLinkedNodes()) {
            if(node.getTuple() == null || node.isEnd()) return;
            else {
                System.out.println(s + node.getTuple().getString());
                printAll(node, s + "\t");
            }
        }
    }

    @Test
    public void testBackTrack() {
        TupleGraph graph = TupleGraphUtil.constructGraph("FAMILIE", 3);
        TupleNode first = graph.start;

        // find last node before end (in any path)
        TupleNode current = first;
        while (!current.isEnd()) {
            for (TupleLink link : current.getOutLinks()) {
                current = link.getEnd();
                break;
            }
        }

        if(!current.isStart()) {
            for (TupleLink link: current.getInLinks()) {
                current = link.getStart();
                break;
            }
        }

        // back track from that node
        Set<List<TupleNode>> backTracked = current.backTrack(3);

        for(List<TupleNode> tupleNodeList : backTracked) {
            for(TupleNode node : tupleNodeList) {
                if(node.getTuple() != null) {
                    System.out.print(node.getTuple().getString() + " ");
                }
            }
            System.out.println();
        }


    }

}
