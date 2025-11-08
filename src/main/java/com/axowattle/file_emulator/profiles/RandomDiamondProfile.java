package com.axowattle.file_emulator.profiles;

import com.axowattle.file_emulator.PlaceProfile;
import com.axowattle.file_emulator.PositionDecider;
import com.axowattle.file_emulator.Vector3Int;
import org.bukkit.Material;

import java.util.Stack;

public class RandomDiamondProfile implements PlaceProfile {
    private final Stack<Material> blocks_stack;

    public RandomDiamondProfile(){
        blocks_stack = new Stack<>();
    }

    @Override
    public double position_value(Vector3Int position, PositionDecider.PathNode path_node) {
        return Math.abs(position.x - path_node.get_first_path_block().x) + Math.abs(position.y - path_node.get_first_path_block().y) + Math.abs(position.z - path_node.get_first_path_block().z);
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
