package com.axowattle.file_emulator.profiles;

import com.axowattle.file_emulator.PlaceProfile;
import com.axowattle.file_emulator.PositionDecider;
import com.axowattle.file_emulator.Vector3Int;
import org.bukkit.Material;

import java.util.Stack;

public class RandomPerlinNoiseProfile implements PlaceProfile {
    private final Stack<Material> blocks_stack;
    private final NoiseGenerator noise;
    public RandomPerlinNoiseProfile(){
        noise = new NoiseGenerator();
        blocks_stack = new Stack<>();
    }

    @Override
    public double position_value(Vector3Int position, PositionDecider.PathNode path_node) {
        return Math.pow(noise.noise(position.x, position.y, position.z) * 100, 2) + position.squared_distance(path_node.get_first_path_block());
    }

    @Override
    public void on_path_enter(PositionDecider.PathNode new_node) {
        blocks_stack.push(RandomBlocks.randomFullBlock(true));
    }

    @Override
    public void on_path_exit(PositionDecider.PathNode old_node) {
        blocks_stack.pop();
    }

    @Override
    public Material select_material(Vector3Int position) {
        return blocks_stack.peek();
    }
}
