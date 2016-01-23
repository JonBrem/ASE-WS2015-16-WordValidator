package de.ur.ahci.tuples.tuple_graph;

import de.ur.ahci.similar_characters.Replacements;
import de.ur.ahci.similar_characters.SimilarString;
import de.ur.ahci.tuples.Tuple;

import java.util.*;

public class TupleGraphUtil {

    public static Map<Tuple, TupleNode> tupleNodeMap;

    static {
        tupleNodeMap = new HashMap<>();
    }

    public static TupleGraph constructGraph(String word, int tupleLength) {
        TupleNode start = new TupleNode(null);
        TupleNode end = new TupleNode(null);

        start.makeStart();
        end.makeEnd();

        if (word.length() < tupleLength) {
            constructGraphForShortString(word, start, end);
        } else {
            Set<TupleNode> currentNodes = new HashSet<>();
            currentNodes.add(start);

            currentNodes = constructGraphForRegularWord(word, tupleLength, start, currentNodes);

            for (TupleNode node : currentNodes) {
                node.addLinkTo(end);
            }
        }

        return new TupleGraph(start);
    }

    private static void constructGraphForShortString(String word, TupleNode start, TupleNode end) {
        TupleNode wordTuple = new TupleNode(new Tuple(word));
        start.addLinkTo(wordTuple);
        wordTuple.addLinkTo(end);
    }

    private static Set<TupleNode> constructGraphForRegularWord(String word, int tupleLength, TupleNode start, Set<TupleNode> currentNodes) {
        for (int i = 0; i < word.length() - tupleLength + 1; i++) {
            if (i == 0) {
                firstExpansion(word.substring(0, tupleLength), start, tupleLength);
                currentNodes = start.findEndNodes();
                if(fixForShortTuples(tupleLength, currentNodes)) {
                    currentNodes = start.findEndNodes();
                }
            } else {
                char currentChar = word.charAt(i + tupleLength - 1);
                regularExpansion(word, tupleLength, currentNodes, currentChar);
                currentNodes = start.findEndNodes();
                if(fixForShortTuples(tupleLength, currentNodes)) {
                    currentNodes = start.findEndNodes();
                }
            }
        }

        postFixForShortTuples(start, tupleLength);

        return currentNodes;
    }

    private static void postFixForShortTuples(TupleNode start, int tupleLength) {
        // @todo: without any expansions: if a tuple (towards the end of the graph) has fewer characters than expected -> combine with parent without any expansions
    }

    private static boolean fixForShortTuples(int tupleLength, Set<TupleNode> currentNodes) {
        boolean hasChangedAnything = false;
        for(TupleNode node : currentNodes) {
            String val = node.getTuple().getString();
            if(val.length() <= tupleLength - 1) {
                boolean success = tryToBacktrace(val, node, tupleLength);
                if(success) hasChangedAnything = true;

                if(success) {
                    Set<TupleLink> linksToNode = node.getInLinks();
                    for(TupleLink linkToNode : linksToNode) {
                        Set<TupleLink> toRemove = new HashSet<>();
                        for(TupleLink linkFromParent : linkToNode.getStart().getOutLinks()) {
                            if(linkFromParent.getEnd() == node) {
                                toRemove.add(linkFromParent);
                            }
                        }
                        linkToNode.getStart().getOutLinks().removeAll(toRemove);
                    }
                }
            }
        }
        return hasChangedAnything;
    }

    private static boolean tryToBacktrace(String val, TupleNode node, int tupleLength) {
        Set<TupleNode> linksToNode = node.getInLinkedNodes();

        boolean hadSuccess = false;
        for(TupleNode parentNode : linksToNode) {
            if(!parentNode.isStart()) {
                String nodeVal = parentNode.getTuple().getString();

                String newVal = nodeVal.charAt(0) + val;
                if(newVal.length() == tupleLength) {
                    for(TupleNode parentOfParent : parentNode.getInLinkedNodes()) {
                        parentOfParent.addLinkTo(new TupleNode(new Tuple(newVal)));
                    }
                    hadSuccess = true;
                } else {
                    hadSuccess = tryToBacktrace(newVal, parentNode, tupleLength);
                }
            }
        }

        return hadSuccess;
    }

    private static void firstExpansion(String wordPart, TupleNode start, int tupleLength) {
        Set<TupleNode> expandedNodes = getExpandedNodes(wordPart, tupleLength);
        for(TupleNode tmpNode : expandedNodes) {
            start.addLinkTo(tmpNode);
        }
    }

    private static void regularExpansion(String word, int tupleLength, Set<TupleNode> currentNodes, char currentChar) {
        Set<String> currentNodeEndings = getCurrentNodeEndings(currentNodes, tupleLength);
        for (String currentNodeEnding : currentNodeEndings) {
            if(currentNodeEnding.length() < tupleLength - 1) continue;
            String nextString = currentNodeEnding + currentChar;

            Set<TupleNode> expandedNodes = getExpandedNodes(nextString, tupleLength);

            for (TupleNode currentNode : currentNodes) {
                if (tupleEndsWith(currentNode, currentNodeEnding)) {
                    for (TupleNode nextNode : expandedNodes) {
                        currentNode.addLinkTo(nextNode);
                    }
                }
            }
        }

        for (TupleNode currentNode : currentNodes) {
            String val = currentNode.getTuple().getString();
            if(val.length() < tupleLength) {
                currentNode.getTuple().setString(val + currentChar);
            }
        }
    }

    private static Set<String> getCurrentNodeEndings(Set<TupleNode> currentNodes, int tupleLength) {
        Set<String> currentNodeEndings = new HashSet<>();

        for(TupleNode tuple : currentNodes) {
            String tupleContent = tuple.getTuple().getString();
            if(tupleContent.length() == tupleLength) {
                currentNodeEndings.add(tupleContent.substring(1));
            } else if (tupleContent.length() < tupleLength) {
                currentNodeEndings.add(tupleContent);
            }
        }
        return currentNodeEndings;
    }


    private static boolean tupleEndsWith(TupleNode currentNode, String currentNodeEnding) {
        return currentNode.getTuple().getString().endsWith(currentNodeEnding) && !currentNode.getTuple().getString().equals(currentNodeEnding);
    }

    private static Set<TupleNode> getExpandedNodes(String nextString, int tupleLength) {
        Set<TupleNode> nextNodes = new HashSet<>();
        nextNodes.add(new TupleNode(new Tuple(nextString)));

        List<SimilarString> similarStrings = Replacements.getReplacedStrings(nextString);

        for(SimilarString similarString : similarStrings) {
            String val = similarString.getString();
            if(val.length() <= tupleLength) {
                nextNodes.add(new TupleNode(new Tuple(val)));
            } else if (val.length() > tupleLength) {
                TupleNode current, first;
                first = current = new TupleNode(new Tuple(val.substring(0, tupleLength)));

                for(int i = 1; i < val.length() - tupleLength + 1; i++) {
                    TupleNode next = new TupleNode(new Tuple(val.substring(i, i + tupleLength)));
                    current.addLinkTo(next);
                    current = next;
                }
                nextNodes.add(first);
            }
        }

        return nextNodes;
    }
}