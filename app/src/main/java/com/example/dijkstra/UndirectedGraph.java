package com.example.dijkstra;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

class UndirectedGraph {
    private Map<String, List<Edge>> adjacencyList;

    UndirectedGraph() {
        this.adjacencyList = new HashMap<>();
    }

    void addEdge(String startNode, String endNode, String edgeName, double length) {
        addEdgeToMap(startNode, endNode, edgeName, length);
        addEdgeToMap(endNode, startNode, edgeName, length);
    }

    private void addEdgeToMap(String startNode, String endNode, String edgeName, double length) {
        List<Edge> edges = adjacencyList.getOrDefault(startNode, new ArrayList<>());
        edges.add(new Edge(startNode, endNode, edgeName, length));
        adjacencyList.put(startNode, edges);
    }

    List<Edge> getEdges(String node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    public String getGraphInfo() {
        StringBuilder builder = new StringBuilder();
        for (String node : adjacencyList.keySet()) {
            List<Edge> edges = adjacencyList.get(node);
            for (Edge edge : edges) {
                builder.append("StartNode: ").append(node)
                        .append(", EndNode: ").append(edge.endNode)
                        //.append(" - ").append(edge.endNode)
                        .append(", StringLineName: ").append(edge.edgeName)
                        .append(", Length: ").append(edge.length)
                        .append("\n");
            }
        }
        return builder.toString();
    }

    int getVertexCount() {
        return adjacencyList.size();
    }

    // 추가된 메서드: 간선 개수 반환, 나중에 삭제
    int getEdgeCount() {
        int count = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        // 무방향 그래프이므로 간선의 개수를 2로 나눕니다.
        return count / 2;
    }

    // 추가된 메서드: 그래프의 간선 정보를 가져오는 메서드
    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    // 노드와 해당 노드까지의 거리를 저장하는 클래스
    private static class NodeDistancePair {
        String node;
        double distance;

        NodeDistancePair(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }

}


class Edge {
    String startNode;
    String endNode;
    String edgeName;
    double length;

    public String getEdgeName() {
        return edgeName;
    }

    Edge(String startNode, String endNode, String edgeName, double length) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.edgeName = edgeName;
        this.length = length;
    }
}