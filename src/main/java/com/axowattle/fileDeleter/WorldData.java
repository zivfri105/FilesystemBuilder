package com.axowattle.fileDeleter;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class WorldData {
    public WorldData(World world, int blocks_capacity) {
        unloaded_blocks = new HashMap<>();
        all_positons = new HashMap<>();
        place_queue = new LinkedBlockingQueue<>(blocks_capacity);
        this.world = world;
    }
    public final World world;
    public final Map<Chunk, Queue<PlaceData>> unloaded_blocks;
    public final Map<Block, String> all_positons;
    public final BlockingQueue<PlaceData> place_queue;


    public void add_block(Vector position, Material block, String text){
        PlaceData data = new PlaceData();
        data.position = position;
        data.blockData = block;
        data.text = text;
        try {
            place_queue.put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
