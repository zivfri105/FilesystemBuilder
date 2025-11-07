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
    class PlaceData{
        public Vector position;
        public Material blockData;
        public String text;
    }

    private final World world;
    private final BlockingQueue<PlaceData> queue;
    private final int maxPlacePerTick;
    private final HashMap<Block, String> all_positons;

    public BlockPlacer(World world, int queueCapacity, int maxPlacePerTick) {
        this.world = world;
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.maxPlacePerTick = maxPlacePerTick;
        this.all_positons = new HashMap<>();
    }

    @Override
    public void run() {
        for (int blocksPlaced = 0; !queue.isEmpty() && blocksPlaced < maxPlacePerTick; blocksPlaced++){
            try {
                PlaceData data = queue.take();
                Block block = world.getBlockAt(data.position.getBlockX(), data.position.getBlockY(), data.position.getBlockZ());
                Chunk chunk = block.getChunk();
                if (!chunk.isLoaded()) queue.put(data);
                block.setType(data.blockData, false);
                all_positons.put(block, data.text);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void revertBlocks(){
        for(Block block : all_positons.keySet()){
            block.setType(Material.AIR);
        }
    }

    public void add_block(Vector position, Material block, String text){
        PlaceData data = new PlaceData();
        data.position = position;
        data.blockData = block;
        data.text = text;
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String get_block_text(Block block){
        if(all_positons.containsKey(block))
            return all_positons.get(block);
        return null;
    }

    public int get_waiting_length(){
        return queue.size();
    }
}
