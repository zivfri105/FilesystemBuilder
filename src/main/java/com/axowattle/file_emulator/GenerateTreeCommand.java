package com.axowattle.file_emulator;

import com.axowattle.file_emulator.profiles.RandomSphereProfile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class GenerateTreeCommand implements CommandExecutor {
    private final WorldData world_data;
    private final BlockNotifier notifier;

    public GenerateTreeCommand(WorldData world_data, BlockNotifier notifier) {
        this.world_data = world_data;
        this.notifier = notifier;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (!(sender instanceof Player player)){
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return false;
        }

        PositionDecider decider = new PositionDecider(null, new RandomSphereProfile(), world_data, new Vector3Int(player.getLocation().toVector()));

        FileBuilder builder = new FileBuilder(decider, notifier);
        builder.start();



        return true;
    }
}
