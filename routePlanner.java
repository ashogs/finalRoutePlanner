
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Scanner;


public class routePlanner {
    ArrayList<String> visited;
    ArrayList<String> unVisited;
    HashMap<String, HashMap <Integer, String>> routeTable;

    routePlanner(Graph graph){
        int sizeOfGraph = graph.nodeList.size();
        this.visited = new ArrayList <String>();
        this.unVisited = new ArrayList <String>(sizeOfGraph);
        this.routeTable = new HashMap<String, HashMap <Integer, String>>();
        for (Node node : graph.nodeList){
            this.unVisited.add(node.name);
            HashMap <Integer, String> neighbour = new HashMap <Integer, String>();
            neighbour.put(999999, "");
            this.routeTable.put(node.name, neighbour);
        }
    }

    public static void main(String[] args) {
        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter starting point");
        String startPoint = myObj.nextLine();
        System.out.println("Enter end point");
        String endPoint = myObj.nextLine();
        Graph graph = new Graph();
        graph.populateGraphFromFile();
        routePlanner route = new routePlanner(graph);
        System.out.println(route.calculateDistanceFromStartingPoint(startPoint, endPoint, graph));
    }

    Integer calculateDistanceFromStartingPoint(String fromPlace, String toPlace, Graph graph){
        this.setValueInTable(fromPlace, fromPlace, 0);
        String next = fromPlace;
        String previous = fromPlace;
        Integer distance = 0;
        while(!this.unVisited.isEmpty()){
            int previousValue = getValueFromTable(next);
            this.calculateDistanceNearestNeighbour(next, graph, previousValue);
            this.unVisited.remove(next);
            this.visited.add(next);
            previous = next;
            next = this.findNextNode(previous);
        }


        for(String name : this.routeTable.keySet()){
            if (name.equals(toPlace)){
                HashMap<Integer, String> values = this.routeTable.get(name);
                for (Integer value : values.keySet()){
                    distance = value;
                }
            }
        }
        return distance;
    }

    String findNextNode(String previous){
        int size = this.unVisited.size();
        Integer min = 9999999;
        ArrayList <Integer> valueList = new ArrayList <Integer>();
        String nextNode = "";
        HashMap <Integer, String> temp = new HashMap <Integer, String> ();
        for (String nodeName : this.unVisited){
            Integer value = this.getValueFromTable(nodeName);
            valueList.add(value.intValue());
        }
        for (Integer i : valueList){
            if(i < min && i >= 0){
                min = i;
            }
        }
        for(HashMap <Integer, String> valueSet : this.routeTable.values()){
            for(Integer i : valueSet.keySet()){
                if(i == min){
                    temp.put(min, valueSet.get(i));
                }
            }
        }
        for(String keys : this.routeTable.keySet()){
            if(this.routeTable.get(keys).equals(temp)&&(!this.visited.contains(keys))){
                nextNode = keys;
            }
        }
        return nextNode;
    }

    void calculateDistanceNearestNeighbour(String nameOfNode, Graph graph, Integer previousPath){
        for(Node node : graph.nodeList){
            if(node.name.equals(nameOfNode)&&(!this.visited.contains(node.name))){
                for (HashMap<String, Integer> neighbour : node.neighbours){
                    boolean mustUpdate = false;
                    Integer newValue = -1;
                    String nameOfNeighbour = "";
                    Integer updateValue = -1;
                    for (String name : neighbour.keySet()){
                        nameOfNeighbour = name;
                    }
                    for (Integer value : neighbour.values()) {
                        newValue = value;
                        if((value + previousPath) < getValueFromTable(nameOfNeighbour)){
                            mustUpdate = true;
                            updateValue = previousPath + value;
                        }
                    }
                    if (mustUpdate == true){
                        for (String visitedPlace : neighbour.keySet()){
                            this.setValueInTable(visitedPlace, nameOfNode, updateValue);
                        }   
                    }
                }
            }
        }
    }

    void setValueInTable(String actualNode, String visitedNode, Integer value){
        HashMap <Integer, String> newValue = new HashMap <Integer, String>();
        newValue.put(value, visitedNode);
        this.routeTable.replace(actualNode, newValue);
    }
    
    Integer getValueFromTable(String place){
        Integer value = 9999999;
        HashMap <Integer, String> placeValue = this.routeTable.get(place);
        for (Integer i : placeValue.keySet()) {
            value = i;
        }
        return value;
    } 
}

class Graph {
    ArrayList <Node> nodeList;

    Graph(){
        this.nodeList = new ArrayList <Node>();
    };

    Graph(ArrayList <Node> nodeList){
        this.nodeList = nodeList;
    }

    void populateGraphFromFile(){
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("ruter.txt"));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                String place = values[0];
                String neighbourPlace = values[1];
                Integer distance = Integer.valueOf(values[2]);

                createNodeAndLinkNeighbour(place, neighbourPlace, distance, this);
                createNodeAndLinkNeighbour(neighbourPlace, place, distance, this);
    
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean doesNodeExist(String value, Graph graph){
        for (Node node : graph.nodeList){
            if(node.name.equals(value)){
                return true;
            }
        }
        return false;
    }
    

    void addNewNode(String name, String neighbour, Integer value){
        HashMap<String, Integer> neighbourWithValue = new HashMap<String, Integer>();
        neighbourWithValue.put(neighbour, value);
        ArrayList<HashMap<String, Integer>> neighbours = new ArrayList<HashMap<String, Integer>>();
        neighbours.add(neighbourWithValue);
        this.nodeList.add(new Node(name, neighbours));
    }

    void updateNodeWithNeighbour(String nodeName, String neighbourName, Integer value, Graph graph){
        HashMap<String, Integer> neighbourWithValue = new HashMap<String, Integer>();
        neighbourWithValue.put(neighbourName, value);
        ArrayList<HashMap<String, Integer>> neighbours = new ArrayList<HashMap<String, Integer>>();
        for (Node node : graph.nodeList){
            if(node.name.equals(nodeName)){
                node.neighbours.add(neighbourWithValue);
            }
        }

    }

    boolean isNeighbourDefined(String place, String neighbourPlace, Graph graph){
        for (Node node : graph.nodeList){
            if (node.name.equals(place)){
                for(HashMap<String, Integer> neighbour : node.neighbours){
                    if(neighbour.containsKey(neighbourPlace)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void createNodeAndLinkNeighbour(String place, String neighbourPlace, Integer value, Graph graph){
        if (!doesNodeExist(place, this)){
            addNewNode(place, neighbourPlace, value);
        }
        else{
            if(!isNeighbourDefined(place, neighbourPlace, this)){
            updateNodeWithNeighbour(place, neighbourPlace ,value, this);
            }
        }
    }
}

class Node{
    String name;
    ArrayList<HashMap<String, Integer>> neighbours;
     
    Node(String name, ArrayList<HashMap<String, Integer>> neighbours) 
    { 
        this.name = name; 
        this.neighbours = neighbours; 
    }

} 