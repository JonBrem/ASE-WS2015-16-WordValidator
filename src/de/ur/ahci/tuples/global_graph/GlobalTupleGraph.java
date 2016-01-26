package de.ur.ahci.tuples.global_graph;

import de.ur.ahci.build_probabilities.NGramProbability;
import de.ur.ahci.tuples.Tuple;
import de.ur.ahci.tuples.tuple_graph.TupleLink;
import de.ur.ahci.tuples.tuple_graph.TupleNode;

import java.util.*;

public class GlobalTupleGraph {

    public static TupleNode globalGraphStart;
    public static Set<Path> paths;

    static {
        globalGraphStart = new TupleNode(null);
        globalGraphStart.makeStart();
        paths = new HashSet<>();
    }

    public static void add(TupleNode startOfGraph) {
        Set<TupleNode> exploredNodes = new HashSet<>();
        exploredNodes.add(startOfGraph);

        Set<TupleLink> graphStartPoints = startOfGraph.getOutLinks();

        int backTrackSteps = 0;

        while (true) {
            backTrackSteps++;
            Set<TupleLink> newGraphStartPoints = new HashSet<>();

            for (TupleLink link : graphStartPoints) {
                TupleNode node = link.getEnd();
                if (allInLinksExplored(node, exploredNodes)) {
                    Set<List<TupleNode>> backTracks = node.backTrack(backTrackSteps);
                    boostLists(backTracks);
                    newGraphStartPoints.addAll(node.getOutLinks());
                    exploredNodes.add(node);
                }
            }

            graphStartPoints = newGraphStartPoints;

            boolean containsOnlyEnd = true;
            for(TupleLink link: graphStartPoints) {
                if(!link.getEnd().isEnd()) containsOnlyEnd = false;
            }
            if (containsOnlyEnd) break;
        }
    }

    private static void boostLists(Set<List<TupleNode>> backTracks) {
        for(List<TupleNode> nodeList : backTracks) {
            Collections.reverse(nodeList); // was created by backtracking, so it needs to be reversed.

            Set<TupleNode> subPathStarts = new HashSet<>();

            float factor = 1f;
            int initialSize = nodeList.size();

            while(nodeList.size() > 1) {
                Path listAsPath = new Path(nodeList, factor);

                boolean alreadyExists = false;
                for (Path p : paths) {
                    TupleNode firstNodeInPath = p.getPath().get(0);
                    boolean cont = false;
                    for(TupleNode subPathStart : subPathStarts) {
                        if(subPathStart == firstNodeInPath) {
                            cont = true;
                            break;
                        }
                    }

                    if(cont) {
                        continue;
                    }

                    if (listAsPath.compareTo(p)) {
                       boostPath(p, factor);
                       alreadyExists = true;
                    }
                }

                if(!alreadyExists && nodeList.size() == initialSize) {
                    createPath(listAsPath, subPathStarts, factor);
                }

                List<TupleNode> copy = new ArrayList<>();
                for(int i = 1; i < nodeList.size(); i++) {
                    copy.add(nodeList.get(i));
                }
                nodeList = copy;

                factor = nodeList.size() / (float) initialSize;
            }
        }
    }

    private static void boostPath(Path p, float factor) {
        p.addEntryProbability(factor);
    }

    private static void createPath(Path listAsPath, Set<TupleNode> subPathStarts, float factor) {
        paths.add(listAsPath);
        globalGraphStart.addLinkTo(listAsPath.getPath().get(0), factor);

        // create paths from start
        List<TupleNode> pathAsList = new ArrayList<>();
        for (int i = 1; i < pathAsList.size() - 1; i++) {
            List<TupleNode> subList = new ArrayList<>();
            for(int j = i; j < pathAsList.size(); j++) {
                subList.add(pathAsList.get(j));
            }

            if(subList.size() > 0) {
                Path p = new Path(subList, factor * subList.size() / (float) pathAsList.size());
                paths.add(p);
            }
        }
    }

    private static boolean allInLinksExplored(TupleNode node, Set<TupleNode> exploredNodes) {
        for(TupleNode parent : node.getInLinkedNodes()) {
            if(!exploredNodes.contains(parent)) return false;
        }

        return true;
    }

    public static void dump(NGramProbability nGramProbability) {
        List<Path> pathsAsList = new ArrayList<>(paths);
        for(Path p : pathsAsList) p.calculateAbsoluteProbability(nGramProbability);
        Collections.sort(pathsAsList, (o1, o2) -> Float.compare(o1.absoluteProbability, o2.absoluteProbability));
        Collections.reverse(pathsAsList);

        pathsAsList.forEach(p -> System.out.println(p.absoluteProbability + ":\t" + p.toString()));

    }

    private static class Path {
        private List<TupleNode> path;
        private float entryProbability;
        private float absoluteProbability;

        public Path(List<TupleNode> path) {
            this(path, 1f);
        }

        public Path(List<TupleNode> path, float entryProbability) {
            this.path = path;
            this.entryProbability = entryProbability;
        }

        public boolean compareTo(Path another) {
            List<TupleNode> anotherPath = another.getPath();

            if (path.size() != anotherPath.size()) return false;
            for (int i = 0; i < path.size(); i++) {
                Tuple t1 = path.get(i).getTuple();
                Tuple t2 = anotherPath.get(i).getTuple();

                if ((t1 == null && t2 != null) || (t1 != null && t2 == null) || (t1 == null && t2 == null)) return false;
                if (!t1.getString().equals(t2.getString())) return false;
            }

            return true;
        }

        public List<TupleNode> getPath() {
            return this.path;
        }

        public void setPath(List<TupleNode> path) {
            this.path = path;
        }

        public float getEntryProbability() {
            return this.entryProbability;
        }

        public void addEntryProbability(float probability) {
            this.entryProbability += probability;
        }

        private TupleLink getLinkAlongPath(int index) {
            TupleNode start = path.get(index);
            TupleNode end = path.get(index + 1);

            for(TupleLink l : start.getOutLinks()) {
                if(l.getEnd() == end) return l;
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();

            for(int i = 0; i < path.size(); i++) {
                Tuple t = path.get(i).getTuple();
                if(t == null) {
                    s.append("null");
                } else {
                    s.append(t.getString());
                }

                if(i != path.size() - 1) s.append("\t").append(getLinkAlongPath(i).getProbabiltiy()).append(": ");
            }

            return s.toString();
        }

        public void calculateAbsoluteProbability(NGramProbability nGramProbability) {
            float absoluteProbability = entryProbability;

            for(int i = 0; i < path.size(); i++) {
                Tuple t = path.get(i).getTuple();

                if(t != null) {
                    float p = nGramProbability.getProbability(t.getString());

                    absoluteProbability *= p + 0.5;
                }

                if(i != path.size() - 1){
                    float transitionProbability = getLinkAlongPath(i).getProbabiltiy();
                    absoluteProbability *= transitionProbability;
                }
            }

            absoluteProbability *= path.size();
        }
    }
}
