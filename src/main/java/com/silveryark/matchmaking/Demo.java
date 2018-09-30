package com.silveryark.matchmaking;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.EdmondsMaximumCardinalityMatching;
import org.jgrapht.alg.matching.GreedyWeightedMatching;
import org.jgrapht.graph.AsWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashMap;
import java.util.Map;

public class Demo {


    public static void main(String[] args) {
        Graph<String, DefaultWeightedEdge> stringGraph = createStringGraph();

        // note undirected edges are printed as: {<v1>,<v2>}
        System.out.println(stringGraph.toString());

        EdmondsMaximumCardinalityMatching greedyWeightedMatching = new EdmondsMaximumCardinalityMatching(stringGraph,
                new GreedyWeightedMatching(stringGraph, true));
        MatchingAlgorithm.Matching matching =
                greedyWeightedMatching.getMatching();
        System.out.println(matching);

    }

    private static Graph<String, DefaultWeightedEdge> createStringGraph() {
        Graph<String, DefaultWeightedEdge> g = new SimpleGraph<>(DefaultWeightedEdge.class);

        String v1 = "Player1";
        String v2 = "Player2";
        String v3 = "Player3";
        String v4 = "Player4";
        String v5 = "Player5";

        // add the vertices
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);

        Map<DefaultWeightedEdge, Double> weightedMap = new HashMap<>();
        // add edges to create a circuit
        weightedMap.put(g.addEdge(v1, v2), (double) 2);
        weightedMap.put(g.addEdge(v2, v3), (double) 1);
        weightedMap.put(g.addEdge(v3, v4), (double) 0);
        weightedMap.put(g.addEdge(v4, v1), (double) -1);

        return new AsWeightedGraph<>(g, weightedMap);
    }

}
