package com.axowattle.fileDeleter;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class BlockPlacer implements Runnable{


    private final WorldData world_data;
    private final int maxPlacePerTick;
    private final long budgetNanos;

    public BlockPlacer(WorldData world_data, int maxPlacePerTick, long budgetNanos) {
        this.maxPlacePerTick = maxPlacePerTick;
        this.world_data = world_data;
        this.budgetNanos = budgetNanos;
    }

    @Override
    public void run() {
        final long start = System.nanoTime();

        for (int blocksPlaced = 0; (!world_data.place_queue.isEmpty() || !world_data.high_priority_place_queue.isEmpty()) && blocksPlaced < maxPlacePerTick && (System.nanoTime() - start) < budgetNanos; blocksPlaced++){
            try {
                PlaceData data;
                if (world_data.high_priority_place_queue.isEmpty()) data = world_data.place_queue.take();
                else data = world_data.high_priority_place_queue.take();
                Block block = world_data.world.getBlockAt(data.position.x, data.position.y, data.position.z);
                Chunk chunk = block.getChunk();
                if (!chunk.isLoaded()) {
                    world_data.add_unloaded_block(chunk, data);
                    continue;
                }
                block.setType(data.blockData, false);
                if (data.path != null)
                    world_data.all_positons.add_path(new Vector3Int(block.getLocation().toVector()) ,data.path);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void revertBlocks(){
        for(Vector3Int position : world_data.all_positons.keySet()){
            world_data.world.getBlockAt(position.x, position.y, position.z).setType(Material.AIR);
        }
    }
}
