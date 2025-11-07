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

    public BlockPlacer(WorldData world_data, int maxPlacePerTick) {
        this.maxPlacePerTick = maxPlacePerTick;
        this.world_data = world_data;
    }

    @Override
    public void run() {
        for (int blocksPlaced = 0; !world_data.place_queue.isEmpty() && blocksPlaced < maxPlacePerTick; blocksPlaced++){
            try {
                PlaceData data = world_data.place_queue.take();
                Block block = world_data.world.getBlockAt(data.position.getBlockX(), data.position.getBlockY(), data.position.getBlockZ());
                Chunk chunk = block.getChunk();
                if (!chunk.isLoaded()) {
                    world_data.add_unloaded_block(chunk, data);
                    continue;
                }
                block.setType(data.blockData, false);
                world_data.all_positons.put(block, data.text);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void revertBlocks(){
        for(Block block : world_data.all_positons.keySet()){
            block.setType(Material.AIR);
        }
    }

    public String get_block_text(Block block){
        if(world_data.all_positons.containsKey(block))
            return world_data.all_positons.get(block);
        return null;
    }
}
