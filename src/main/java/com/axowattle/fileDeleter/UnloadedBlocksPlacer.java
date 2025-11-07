package com.axowattle.fileDeleter;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class UnloadedBlocksPlacer implements Listener {
    private final WorldData world_data;


    public UnloadedBlocksPlacer(WorldData world_data) {
        this.world_data = world_data;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.getWorld() != world_data.world) return;

        Queue<PlaceData> place_queue = world_data.get_chunk_data(e.getChunk());
        if (place_queue == null) return;

        while (!place_queue.isEmpty()){
            PlaceData data = place_queue.poll();
            Block block = world_data.world.getBlockAt(data.position.getBlockX(), data.position.getBlockY(), data.position.getBlockZ());
            block.setType(data.blockData, false);
            world_data.all_positons.put(block, data.text);
        }
    }
}
