package ad2.ss16.pa;

import java.util.*;

/**
 * Klasse zum Berechnen eines k-MST mittels Branch-and-Bound. Hier sollen Sie
 * Ihre L&ouml;sung implementieren.
 */
@SuppressWarnings(value = "unchecked")
public class KMST extends AbstractKMST {

    private Set<Edge> usedEdges;
    private Set<Integer> usedNodes;
    private Set<Edge> edges;
    private Set<Edge> mstEdges;
    private PriorityQueue<Edge> queue;
    private int k;
    private int upperBound;
    private LinkedList<Edge>[] incidenceEdges;
    private int currentWeight;
    private int[] minNodes;
    private boolean solutionFound;
    private PriorityQueue<Edge> edgesQueue;
    private int recursionLimit;
    private int numNodes;

    /**
     * Der Konstruktor. Hier ist die richtige Stelle f&uuml;r die
     * Initialisierung Ihrer Datenstrukturen.
     *
     * @param numNodes Die Anzahl der Knoten
     * @param numEdges Die Anzahl der Kanten
     * @param edges    Die Menge der Kanten
     * @param k        Die Anzahl der Knoten, die Ihr MST haben soll
     */
    public KMST(Integer numNodes, Integer numEdges, HashSet<Edge> edges, int k) {
        this.edges = edges;
        this.numNodes = numNodes;
        this.mstEdges = new HashSet<>();
        this.usedNodes = new HashSet<>();
        this.k = k;
        this.upperBound = 0;
        this.currentWeight = 0;
        this.incidenceEdges = new LinkedList[numNodes];
        this.queue = new PriorityQueue<>(numEdges);
        this.minNodes = new int[k];
        this.solutionFound = false;
        this.edgesQueue = new PriorityQueue<>();
        for (Edge edge : edges) {
            queue.add(edge);
        }
        initAdjacentList();


    }

    private void initAdjacentList() {
        PriorityQueue<Edge> edgesQueue = new PriorityQueue<>(queue);
        for (Edge e : edges) {
            if (incidenceEdges[e.node1] == null) {
                incidenceEdges[e.node1] = new LinkedList<>();
            }
            if (incidenceEdges[e.node2] == null) {
                incidenceEdges[e.node2] = new LinkedList<>();
            }
            incidenceEdges[e.node1].add(e);
            incidenceEdges[e.node2].add(e);
        }
        minNodes[0] = 0;

        for (int i = 1; i < k; i++) {
            Edge minEdge = edgesQueue.poll();
            minNodes[i] = minNodes[i - 1] + minEdge.weight;
        }
    }
    private PriorityQueue<Edge> calculateIncidenceEdges(Set<Integer> usedNodes) {
        PriorityQueue<Edge> edges = new PriorityQueue<>();
        Iterator<Integer> itUsedNodes = usedNodes.iterator();
        while (itUsedNodes.hasNext()) {
            LinkedList<Edge> list = incidenceEdges[itUsedNodes.next()];
            for (int i = 0; i < list.size(); i++) {
                Edge adjEdge = list.get(i);
                if (!edges.contains(adjEdge)
                        && (!usedNodes.contains(adjEdge.node1) || !usedNodes.contains(adjEdge.node2))) {
                    edges.add(adjEdge);
                }
            }
        }
        return edges;
    }

    private void calculateUpperBound() {
        upperBound = Integer.MAX_VALUE;
        PriorityQueue<Edge> clonequeue = new PriorityQueue<>(queue);
        usedNodes = new HashSet<>();

        while (!clonequeue.isEmpty()) {
            Edge e = clonequeue.poll();
            Prim(e);
        }

    }

    private void Prim(Edge e) {
        if (usedNodes.size() < k) {
            mstEdges.add(e);
            currentWeight += e.weight;
            usedNodes.add(e.node1);
            usedNodes.add(e.node2);
            if (usedNodes.size() < k) {
                PriorityQueue<Edge> incidenceEdges = calculateIncidenceEdges(usedNodes);
                Iterator<Edge> iterator = incidenceEdges.iterator();
                while (iterator.hasNext()) {
                    Edge nextEdge = incidenceEdges.poll();
                    Prim(nextEdge);
                    return;
                }
            }


        } else if (usedNodes.size() == k && currentWeight < upperBound) {
            upperBound = currentWeight;
            setSolution(upperBound, mstEdges);
            System.out.println("upperBound: " + getSolution().getUpperBound());
            usedNodes = new HashSet<>();
            currentWeight = 0;
            mstEdges = new HashSet<>();
        } else {
            usedNodes = new HashSet<>();
            currentWeight = 0;
            mstEdges = new HashSet<>();
        }
    }


    private void doMst(int node, PriorityQueue<Edge> edgesQueue, int numEdges) {
        int numOfEdgesLeft = (k - 1) - usedEdges.size();
        int minWeight = minNodes[numOfEdgesLeft - 1];
        int estimateWeight;
        recursionLimit++;

        LinkedList<Edge> list = incidenceEdges[node];
        for (Edge edge : list) {
            estimateWeight = currentWeight + edge.weight + minWeight;
            if ((!usedNodes.contains(edge.node1) || !usedNodes.contains(edge.node2))
                    && estimateWeight < upperBound
                    && !edgesQueue.contains(edge)) {
                edgesQueue.add(edge);
            }
        }

        PriorityQueue<Edge> priorityQueueClone;

        while (!edgesQueue.isEmpty() && recursionLimit < numNodes) {

            Edge incidenceEdge = edgesQueue.poll();

            if (!usedNodes.contains(incidenceEdge.node1) || !usedNodes.contains(incidenceEdge.node2)) {

                numOfEdgesLeft = (k - 1) - usedEdges.size();
                if (usedEdges.size() < k - 1) {
                    estimateWeight = currentWeight + incidenceEdge.weight + minNodes[numOfEdgesLeft - 1];
                } else {
                    estimateWeight = currentWeight + incidenceEdge.weight + minNodes[numOfEdgesLeft];
                }

                if (estimateWeight < upperBound) {
                    currentWeight += incidenceEdge.weight;
                    solutionFound = false;
                    int neighborNode;
                    if (usedNodes.contains(incidenceEdge.node1)) {
                        neighborNode = incidenceEdge.node2;
                        node = incidenceEdge.node1;
                    } else {
                        neighborNode = incidenceEdge.node1;
                        node = incidenceEdge.node2;
                    }

                    if (usedNodes.isEmpty()) {
                        usedNodes.add(neighborNode);
                        usedNodes.add(node);
                    } else {
                        usedNodes.add(neighborNode);
                    }

                    usedEdges.add(incidenceEdge);

                    if (usedNodes.size() == k && usedEdges.size() == k - 1) {
                        upperBound = currentWeight;
                        setSolution(upperBound, usedEdges);
                        solutionFound = true;
                        return;
                    } else {
                        node = neighborNode;
                        priorityQueueClone = new PriorityQueue<>(edgesQueue);
                        numEdges++;
                        doMst(node, edgesQueue, numEdges);

                        if (usedNodes.size() == 1) {
                            currentWeight = 0;
                            usedEdges = new HashSet<>();
                            usedNodes = new HashSet<>();
                        }
                        edgesQueue = new PriorityQueue<>();
                        edgesQueue.addAll(priorityQueueClone);
                    }
                    if (!solutionFound) {
                        usedNodes.remove(neighborNode);
                        usedEdges.remove(incidenceEdge);
                        numEdges--;
                        currentWeight -= incidenceEdge.weight;
                    }
                }
            }
        }
    }

    /**
     * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
     * Verf&uuml;gung gestellt um einen g&uuml;ltigen k-MST zu finden.
     * <p>
     * <p>
     * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
     * ein.
     * </p>
     */
    @Override
    public void run() {

        calculateUpperBound();

        while (!queue.isEmpty()) {
            Edge e = queue.poll();

            usedEdges = new HashSet<>();
            usedNodes = new HashSet<>();

            int minWeight = minNodes[k - 2];
            int estimateWeight = e.weight + minWeight;

            if (estimateWeight < upperBound) {
                currentWeight = 0;
                solutionFound = false;
                recursionLimit = 0;
                doMst(e.node1, edgesQueue, 0);
            }

        }
    }
}
