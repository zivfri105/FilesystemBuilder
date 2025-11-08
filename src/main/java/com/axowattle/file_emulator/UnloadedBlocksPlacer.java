package com.axowattle.file_emulator;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Queue;

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
            Block block = world_data.world.getBlockAt(data.position.x, data.position.y, data.position.z);
            block.setType(data.blockData, false);
            if (data.path != null)
                world_data.all_positons.add_path(new Vector3Int(block.getLocation().toVector()), data.path);
        }
    }
}
