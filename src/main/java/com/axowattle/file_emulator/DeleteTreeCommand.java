package com.axowattle.file_emulator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteTreeCommand implements CommandExecutor {
    private final WorldData worldData;

    public DeleteTreeCommand(WorldData worldData) {
        this.worldData = worldData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(!(sender instanceof Player player)) return false;

        try {
            worldData.revert_blocks();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
