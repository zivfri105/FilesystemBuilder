package com.axowattle.file_emulator;

import org.bukkit.Material;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionDecider {
    class Settings{
        int fetchBlocksFromParent = 4;
        int maxSize = 5_000;
    }

    public class PathNode{
        private final PathNode parent;
        private PathNode child;
        private final String name;
        private Vector3Int first_path_block;
        private UniquePriorityQueue<Vector3Int> positions;
        private Settings settings;


        public PathNode(PathNode parent, PlaceProfile profile, String name, Settings settings){
            this.parent = parent;
            this.name = name;
            this.settings = settings;
            if (settings == null) this.settings = new Settings();
            positions = new UniquePriorityQueue<>(Comparator.comparingDouble(v -> profile.position_value(v, this)));


            if (parent == null) return;
            parent.child = this;

            this.first_path_block = parent.fetch_position();
            positions.add(this.first_path_block);
            for (int i = 0; i < settings.fetchBlocksFromParent - 1 && !parent.positions.isEmpty(); i++){
                positions.add(parent.fetch_position());
            }
        }

        public void add_position(Vector3Int position){
            this.positions.add(position);
            while (this.positions.size() > settings.maxSize){
                positions.poll();
            }
        }

        public Vector3Int fetch_position(){
            if (!this.positions.isEmpty())
                return this.positions.poll();

            if (parent == null)
                throw new RuntimeException("Positions are empty this should never happen");

            return parent.fetch_position();
        }

        public void empty_node(){
            if (parent == null) return;
            while (!this.positions.isEmpty()){
                parent.positions.add(this.positions.poll());
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
            for (Vector3Int v : positions) {
                string.append("(");
                string.append(v);
                string.append(")");
            }
            string.append("}");
            return string.toString();
        }
    }

    private final Settings settings;
    private final WorldData world_data;

    private PathNode root_path;
    private PathNode current_path;
    private PlaceProfile profile;



    public PositionDecider(Settings settings, PlaceProfile profile, WorldData world_data, Vector3Int initial_position) {
        if (settings != null) this.settings = settings;
        else this.settings = new Settings();

        this.profile = profile;

        this.world_data = world_data;

        String cwd = System.getProperty("user.dir");
        String rootName = Paths.get(cwd).getRoot().toString();
        root_path = new PathNode(null, profile, rootName, settings);
        current_path = root_path;
        profile.on_path_enter(root_path);
        root_path.add_position(initial_position);
        root_path.first_path_block = initial_position;
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
        current_path =new PathNode(current_path, profile, name, settings);
        profile.on_path_enter(current_path);
    }

    private void remove_directory(){
        profile.on_path_exit(current_path);
        current_path.empty_node();
        current_path = current_path.parent;
    }
}
