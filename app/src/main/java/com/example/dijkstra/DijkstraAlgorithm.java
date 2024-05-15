package com.example.dijkstra;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.util.*;

public class DijkstraAlgorithm {
    private UndirectedGraph graph;
    private Context context;


    public DijkstraAlgorithm(UndirectedGraph graph, Context context) {
        this.graph = graph;
        this.context = context;
    }

    public List<String> findShortestPath(String startNode, String endNode) {
        // 출발 노드에서 각 노드까지의 최단 거리를 저장하는 맵
        Map<String, Double> distances = new HashMap<>();
        // 각 노드의 이전 노드를 저장하는 맵 (최단 경로 추적을 위해)
        Map<String, String> previousNodes = new HashMap<>();
        // 아직 최단 경로가 발견되지 않은 노드들의 집합
        Set<String> unvisitedNodes = new HashSet<>(graph.getAdjacencyList().keySet());

        // XML 파일에서 정의된 항목을 가져와서 중간 노드로 설정
        Set<String> middleNodes = getMiddleNodesFromXML();
        //Log.d("middle",middleNodes.toString());

        // 중간 노드를 unvisitedNodes에서 제거
        unvisitedNodes.removeAll(middleNodes);


        // 출발 노드와 도착 노드를 unvisitedNodes에 추가
       unvisitedNodes.add(startNode);
       unvisitedNodes.add(endNode);

        Log.d("unvisited",unvisitedNodes.toString());


        // 출발 노드의 최단 거리는 0으로 초기화
        distances.put(startNode, 0.0);

        while (!unvisitedNodes.isEmpty()) {
            // 현재까지 최단 거리 중 가장 짧은 거리를 가진 노드 선택
            String currentNode = null;
            for (String node : unvisitedNodes) {
                if (currentNode == null || distances.getOrDefault(node, Double.MAX_VALUE) < distances.getOrDefault(currentNode, Double.MAX_VALUE)) {
                    currentNode = node;
                }
            }

            if (currentNode == null) {
                break;
            }

            unvisitedNodes.remove(currentNode);

            // 현재 노드와 연결된 간선을 순회하며 최단 거리 갱신
            for (Edge edge : graph.getEdges(currentNode)) {
                String neighborNode = edge.endNode;
                double edgeWeight = edge.length;
                double totalDistance = distances.getOrDefault(currentNode, Double.MAX_VALUE) + edgeWeight;
                if (totalDistance < distances.getOrDefault(neighborNode, Double.MAX_VALUE)) {
                    distances.put(neighborNode, totalDistance);
                    previousNodes.put(neighborNode, currentNode);
                }
            }
        }

        // 최단 경로 추적
        List<String> shortestPath = new ArrayList<>();
        String currentNode = endNode;
        while (previousNodes.containsKey(currentNode)) {
            shortestPath.add(currentNode);
            currentNode = previousNodes.get(currentNode);
        }
        shortestPath.add(startNode);
        Collections.reverse(shortestPath);

        return shortestPath;

    }


    // XML 파일에서 정의된 항목을 가져와서 중간 노드로 설정
    private Set<String> getMiddleNodesFromXML() {
        Set<String> middleNodes = new HashSet<>();
        Resources res = context.getResources();
        String[] items = res.getStringArray(R.array.room_start); // spinner_items는 strings.xml 파일에 정의된 배열의 이름
        for (String item : items) {
            if (!item.startsWith("194")) {
                middleNodes.add(item);
            }
        }
        return middleNodes;
    }


    public java.util.List<String> GetEdgeName(java.util.List<String> shortestPath) {
        java.util.List<String> pairedNodes = new ArrayList<>();
        // shortestPath에서 두 개씩 짝지어 pairedNodes에 추가
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            pairedNodes.add(shortestPath.get(i) + "," + shortestPath.get(i + 1));
        }
        Log.d("노드짝 ","노드짝: "+pairedNodes);
        java.util.List<String> edgeNames = new ArrayList<>();
        // pairedNodes에서 각 노드 쌍에 대해 해당하는 간선 이름을 찾아 edgeNames에 추가
        try {
            java.util.List<java.util.List<String>> pathCSV = CSVReader.pathCSV(context, "path12345-좌표최종2.csv");
            for (String nodePair : pairedNodes) {
                for (int i = 0; i < pathCSV.size(); i++) {
                    String[] row = pathCSV.get(i).toArray(new String[0]);
                    if ((row[0] + "," + row[1]).equals(nodePair)||((row[1] + "," + row[0]).equals(nodePair))) {
                        edgeNames.add(row[2]);
                        break;  // 간선을 찾았으므로 더 이상 반복할 필요가 없음
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 예외 처리 로직 추가
        }
        return edgeNames;
    }




}
