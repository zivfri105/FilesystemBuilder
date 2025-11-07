package com.axowattle.fileDeleter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    BlockPlacer placer;
    BlockNotifier notifier;

    @Override
    public void onEnable() {
        placer = new BlockPlacer(Bukkit.getWorld("world"), 50_000, 5_000);
        notifier = new BlockNotifier(placer);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, placer, 1, 1);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, notifier, 1, 1);

        getCommand("gen-files").setExecutor(new GenerateTreeCommand(placer, notifier));
    }

    @Override
    public void onDisable() {
        placer.revertBlocks();
    }
}
