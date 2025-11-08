package com.axowattle.file_emulator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionDecider {
    public static class Settings{
        int fetchBlocksFromParent = 4;
        int maxSize = 5_000;
    }

    private static class PriorityVector{
        public Vector3Int vec;
        public double priority;

        public PriorityVector(Vector3Int vec, PathNode node){
            priority = node.profile.position_value(vec, node);
            this.vec = vec;
        }
    }

    public static class PathNode{
        private final PathNode parent;
        private PathNode child;
        private final String name;
        private Vector3Int first_path_block;
        private final UniquePriorityQueue<PriorityVector> positions;
        private Settings settings;
        private final PlaceProfile profile;


        public PathNode(PathNode parent, PlaceProfile profile, String name, Settings settings){
            this.profile = profile;
            this.parent = parent;
            this.name = name;
            this.settings = settings;
            if (settings == null) this.settings = new Settings();
            positions = new UniquePriorityQueue<>(Comparator.comparingDouble(v -> v.priority));


            if (parent == null) return;
            parent.child = this;

            this.first_path_block = parent.fetch_position();
            positions.add(new PriorityVector(this.first_path_block, this));
            for (int i = 0; i < this.settings.fetchBlocksFromParent - 1 && !parent.positions.isEmpty(); i++){
                positions.add(new PriorityVector(parent.fetch_position(), this));
            }
        }

        public void add_position(Vector3Int position){
            this.positions.add(new PriorityVector(position, this));
            while (this.positions.size() > settings.maxSize){
                positions.poll();
            }
        }

        public Vector3Int fetch_position(){
            if (!this.positions.isEmpty())
                return this.positions.poll().vec;

            if (parent == null)
                throw new RuntimeException("Positions are empty this should never happen");

            return parent.fetch_position();
        }

        public void empty_node(){
            if (parent == null) return;
            while (!this.positions.isEmpty()){
                Vector3Int data = this.positions.poll().vec;
                parent.positions.add(new PriorityVector(data, this));
            }
        }

        public Vector3Int get_first_path_block() {
            return first_path_block;
        }

        @Override
        public String toString() {
            StringBuilder string = new StringBuilder();
            string.append(name);
            string.append(" Blocks{ ");
            for (PriorityVector v : positions) {
                string.append("(");
                string.append(v.vec);
                string.append(")");
            }
            string.append("}");
            return string.toString();
        }
    }

    private final Settings settings;
    private final WorldData world_data;

    private final PathNode root_path;
    private PathNode current_path;
    private final PlaceProfile profile;



    public PositionDecider(Settings settings, PlaceProfile profile, WorldData world_data, Vector3Int initial_position) {
        if (settings != null) this.settings = settings;
        else this.settings = new Settings();

        this.profile = profile;

        this.world_data = world_data;

        String cwd = System.getProperty("user.dir");
        String rootName = Paths.get(cwd).getRoot().toString();
        root_path = new PathNode(null, profile, rootName, settings);
        root_path.first_path_block = initial_position;
        current_path = root_path;
        profile.on_path_enter(root_path);
        root_path.add_position(initial_position);
    }

    public void match_directories(Path path) {
        if (path == null) return;

        PathNode rtl_node = root_path.child;
        int path_index = 0;
        while (path_index < path.getNameCount() && rtl_node != null && sameName(rtl_node.name, path.getName(path_index).toString())){
            path_index++;
            rtl_node = rtl_node.child;
        }
        if (rtl_node == null) rtl_node = current_path;
        else rtl_node = rtl_node.parent;

        while (current_path != rtl_node){
            remove_directory();
        }

        while (path_index < path.getNameCount()){
            add_directory(path.getName(path_index).toString());
            path_index++;
        }
    }

    public void add_file(Path file){
        match_directories(file.getParent());

        Vector3Int placed_position = current_path.fetch_position();
        while (world_data.commited_positions.contains(placed_position)){
            placed_position = current_path.fetch_position();
        }
        world_data.add_place_block(placed_position, profile.select_material(placed_position), file);

        add_optional_block(placed_position, 1, 0, 0);
        add_optional_block(placed_position, -1, 0, 0);
        add_optional_block(placed_position, 0, 1, 0);
        add_optional_block(placed_position, 0, -1, 0);
        add_optional_block(placed_position, 0, 0, 1);
        add_optional_block(placed_position, 0, 0, -1);
    }

    public void add_optional_block(Vector3Int position, int x, int y, int z){
        Vector3Int newPosition = position.clone().add(x, y, z);
        if (newPosition.y < -64 || newPosition.y > 319)
            return;
        if (world_data.commited_positions.contains(newPosition)) return;
        current_path.add_position(newPosition);
    }

// ===== helpers =====

    private static boolean windows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
    private static boolean sameName(String a, String b) {
        if (a == null) return b == null;
        if (b == null) return false;
        return windows() ? a.equalsIgnoreCase(b) : a.equals(b);
    }

    private void add_directory(String name){
        current_path = new PathNode(current_path, profile, name, settings);
        profile.on_path_enter(current_path);
    }

    private void remove_directory(){
        profile.on_path_exit(current_path);
        current_path.empty_node();
        current_path = current_path.parent;
    }
}
