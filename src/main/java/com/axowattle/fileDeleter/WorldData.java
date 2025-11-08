package com.axowattle.fileDeleter;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class WorldData {
    public WorldData(World world, int blocks_capacity) {
        unloaded_blocks = new HashMap<>();
        all_positons = new FilePathArray();
        place_queue = new LinkedBlockingQueue<>(blocks_capacity);
        high_priority_place_queue = new LinkedBlockingQueue<>();
        commited_positions = new HashSet<>();
        this.world = world;
    }
    public final World world;
    public final Map<Long, Queue<PlaceData>> unloaded_blocks;
    public final FilePathArray all_positons;
    public final BlockingQueue<PlaceData> place_queue;
    public final BlockingQueue<PlaceData> high_priority_place_queue;
    public final Set<Vector3Int> commited_positions;


    public void add_place_block(Vector3Int position, Material block, Path path){
        PlaceData data = new PlaceData();
        data.position = position;
        data.blockData = block;
        commited_positions.add(data.position);
        data.path = path;
        try {
            place_queue.put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void add_unloaded_block(Chunk chunk, PlaceData placeData){
        long chunk_key = get_chunk_key(chunk.getX(), chunk.getZ());
        if (!unloaded_blocks.containsKey(chunk_key)){
            unloaded_blocks.put(chunk_key, new LinkedTransferQueue<>());
        }
        unloaded_blocks.get(chunk_key).add(placeData);

    }

    public Queue<PlaceData> get_chunk_data(Chunk chunk){
        long chunk_key = get_chunk_key(chunk.getX(), chunk.getZ());
        if (!unloaded_blocks.containsKey(chunk_key))
            return null;

        return unloaded_blocks.get(chunk_key);
    }

    public void revert_blocks() throws InterruptedException {
        unloaded_blocks.clear();
        place_queue.clear();
        commited_positions.clear();

        for (Vector3Int position : all_positons.keySet()){
            PlaceData data = new PlaceData();
            data.position = position;
            data.blockData = Material.AIR;
            data.path = null;
            high_priority_place_queue.put(data);
        }


        all_positons.clear();
    }

    private static long get_chunk_key(int cx, int cz) {
        return (((long) cx) << 32) | (cz & 0xffffffffL);
    }
}
