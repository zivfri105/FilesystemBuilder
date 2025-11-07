package com.axowattle.fileDeleter;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionDecider {
    class Settings{
        int fetchBlocksFromParent = 4;
    }

    class PathNode{
        private final PathNode parent;
        private PathNode child;
        private final String name;
        private Vector3Int first_path_block;
        private PriorityQueue<Vector3Int> positions;
        private final Settings settings;

        public Material material;

        public PathNode(PathNode parent, String name, Settings settings){
            this.parent = parent;
            this.name = name;
            this.settings = settings;
            this.material = RandomBlocks.randomFullBlock(true);
            positions = new PriorityQueue<>(Comparator.comparingDouble(v -> v.squared_distance(this.first_path_block)));


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
    private final FileEnumerator enumerator;
    private Set<Vector3Int> unallowed_positions;

    private PathNode root_path;
    private PathNode current_path;
    private int path_depth;


    public PositionDecider(Settings settings, WorldData world_data, FileEnumerator enumerator, Vector3Int initial_position) {
        if (settings != null) this.settings = settings;
        else this.settings = new Settings();

        this.world_data = world_data;
        this.enumerator = enumerator;
        this.path_depth = 0;
        unallowed_positions = new HashSet<>();

        String cwd = System.getProperty("user.dir");
        String rootName = Paths.get(cwd).getRoot().toString();
        root_path = new PathNode(null, rootName, settings);
        current_path = root_path;
        path_depth = 0; // depth 0 at root node
        root_path.add_position(initial_position);
        root_path.first_path_block = initial_position;
        unallowed_positions.add(initial_position);
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
        world_data.add_place_block(placed_position, current_path.material, file.toString());

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
        if (unallowed_positions.contains(newPosition)) return;
        unallowed_positions.add(newPosition);
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
        current_path = new PathNode(current_path, name, settings);
    }

    private void remove_directory(){
        current_path.empty_node();
        current_path = current_path.parent;
    }
}
