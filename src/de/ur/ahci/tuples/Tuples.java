package de.ur.ahci.tuples;

import de.ur.ahci.tuples.tuple_graph.TupleNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Access tuples this way.
 */
public class Tuples {

    private static Map<Tuple, TupleNode> tuples;

    static {
        tuples = new HashMap<>();
    }

}
