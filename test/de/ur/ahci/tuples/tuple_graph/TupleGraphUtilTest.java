package de.ur.ahci.tuples.tuple_graph;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class TupleGraphUtilTest {

    @Test
    public void testGraph() {

        TupleGraph graph = TupleGraphUtil.constructGraph("FAMILIENFREUNDLICH", 3);
        TupleNode first = graph.start;
        first.buildStringsFromThisPoint().forEach(System.out::println);

    }

}
