package com.axowattle.fileDeleter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private final long NANOSECONDS_PER_MILLISECONDS = 1_000_000;

    BlockPlacer placer;
    BlockNotifier notifier;

    @Override
    public void onEnable() {
        WorldData world_data = new WorldData(Bukkit.getWorld("world"), 50_000);
        placer = new BlockPlacer(world_data, 10_0000, 1 * NANOSECONDS_PER_MILLISECONDS);
        notifier = new BlockNotifier(world_data);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, placer, 1, 1);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, notifier, 1, 1);

        getCommand("gen-files").setExecutor(new GenerateTreeCommand(world_data, notifier));

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UnloadedBlocksPlacer(world_data), this);
    }

    @Override
    public void onDisable() {
        placer.revertBlocks();
    }
}
