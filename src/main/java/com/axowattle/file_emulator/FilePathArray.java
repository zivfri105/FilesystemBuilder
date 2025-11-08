package com.axowattle.file_emulator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FilePathArray {
    class Node{
        public Node(String name, Node parent){
            this.name = name;
            this.parent = parent;
            this.children = new HashMap<>();
        }

        public String name;
        public Node parent;
        public Map<String, Node> children;
    }

    private Map<Vector3Int ,Node> indexes;
    private Node root_node;

    public FilePathArray(){
        this.indexes = new HashMap<>();
        root_node = new Node("/", null);
    }

    public int add_path(Vector3Int position, Path path) {
        Node current_node = root_node;
        for (int i = 0; i < path.getNameCount(); i++) {
            String segment = path.getName(i).toString();
            current_node.children.putIfAbsent(segment, new Node(segment, current_node));
            current_node = current_node.children.get(segment);
        }
        indexes.put(position ,current_node);
        return indexes.size() - 1;
    }

    public Path get(Vector3Int position) {
        if (!indexes.containsKey(position)) return null;

        Node node = indexes.get(position);
        Deque<String> segments = new ArrayDeque<>();

        // Walk upward to root, collecting names
        while (node != null && node.parent != null) { // skip root's "/"
            segments.addFirst(node.name);
            node = node.parent;
        }

        // Build a Path from the collected segments
        return Paths.get("/", segments.toArray(new String[0]));
    }

    public void clear(){
        root_node = new Node("/", null);
        indexes.clear();
    }

    public Set<Vector3Int> keySet(){
        return indexes.keySet();
    }
}
