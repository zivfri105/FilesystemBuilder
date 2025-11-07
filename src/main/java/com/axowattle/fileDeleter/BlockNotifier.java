package com.axowattle.fileDeleter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class BlockNotifier implements Runnable{
    private final BlockPlacer placer;
    public FileEnumerator current_enumerator;

    public BlockNotifier(BlockPlacer placer) {
        this.placer = placer;
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()){
            Block block = getTargetBlock(player, 5);
            String text = placer.get_block_text(block);
            if (text == null){
                text = "Blocks Waiting: " + placer.get_waiting_length();
                if (current_enumerator != null){
                    text += ", Files Waiting: " + current_enumerator.queuedCount();
                }
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));

        }
    }

    public static Block getTargetBlock(Player player, double maxDistance) {
        RayTraceResult result = player.rayTraceBlocks(maxDistance);
        if (result == null) return null;
        return result.getHitBlock();
    }
}
