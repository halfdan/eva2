package eva2.optimization.strategies;

import java.util.*;

/**
 * This is a small implementation of an undirected graph which is able
 * to compute the connected components.
 * This class is used to represent the NicheGraph for the ANPSO algorithm.
 */
public class NicheGraph implements java.io.Serializable {

    /**
     * *******************************************************************************************************************
     * members
     */
    // key is vertex, value is set of adjacent vertices
    protected TreeMap<String, TreeSet<String>> graphTable;

    // set of visited vertices in bfs
    protected HashSet<String> set;

    /**
     * *******************************************************************************************************************
     * ctor
     */
    public NicheGraph() {
        graphTable = new TreeMap<>();
        set = new HashSet<>();
    }


    public NicheGraph(NicheGraph o) {
        this.graphTable = (TreeMap<String, TreeSet<String>>) o.graphTable.clone();
        this.set = (HashSet<String>) o.set.clone();
    }

    @Override
    public Object clone() {
        return new NicheGraph(this);
    }
/**********************************************************************************************************************
 * vertices and edges
 */
    /**
     * adds a new vertex without neighbors
     *
     * @param v
     */
    public void addVertex(String v) {
        if (!containsVertex(v)) {
            graphTable.put(v, new TreeSet<>());
        }
    }

    /**
     * adds an edge to the graph (previously adds the vertices if necessary)
     *
     * @param v1
     * @param v2
     */
    public void addEdge(String v1, String v2) {
        if (!containsVertex(v1)) {
            addVertex(v1);
        }
        if (!containsVertex(v2)) {
            addVertex(v2);
        }
        // mutually add the vertices as neighbors
        graphTable.get(v1).add(v2);
        graphTable.get(v2).add(v1);
    }

    public boolean containsVertex(String v) {
        return graphTable.containsKey(v);
    }

    // return iterator over all vertices in graph
    public Iterable<String> getVertexIterator() {
        return graphTable.keySet();
    }

    // return an iterator over the neighbors of vertex v
    public Iterable<String> getNeighborIterator(String v) {
        return graphTable.get(v);
    }

    /**
     * *******************************************************************************************************************
     * BFS, getConnectedComponents
     */
    // run BFS from given root vertex r 
    public void runBFS(String r) {
        set = new HashSet<>();

        // put root on the queue
        java.util.Queue<String> q = new LinkedList<>();
        q.offer(r);
        set.add(r);

        // remove next vertex and put all neighbors on queue (if not visited)...
        while (!q.isEmpty()) {
            String v = q.poll();
            for (String w : getNeighborIterator(v)) {
                if (!set.contains(w)) {
                    q.offer(w);
                    set.add(w);
                }
            }
        }
    }

    /**
     * @return connected components of the graph
     */
    public List<Set<String>> getConnectedComponents() {
        ArrayList<Set<String>> l = new ArrayList<>();

        for (String v : getVertexIterator()) {
            if (!isComponent(v, l)) {
                // use current vertex to start a bfs
                runBFS(v);
                // add visited vertices to the set of connected components
                l.add(set);
            }
        }
        return l;
    }

    private boolean isComponent(String v, ArrayList<Set<String>> l) {
        for (Set<String> set : l) {
            if (set.contains(v)) {
                return true;
            }
        }
        return false;
    }

}
