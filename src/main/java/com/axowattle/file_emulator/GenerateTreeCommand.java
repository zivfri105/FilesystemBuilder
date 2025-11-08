package com.axowattle.file_emulator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GenerateTreeCommand implements CommandExecutor {
    private final WorldData world_data;
    private final BlockNotifier notifier;

    public GenerateTreeCommand(WorldData world_data, BlockNotifier notifier) {
        this.world_data = world_data;
        this.notifier = notifier;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return false;
        }
        Player player = (Player) sender;

        PositionDecider decider = new PositionDecider(null, world_data, null, new Vector3Int(player.getLocation().toVector()));

        FileBuilder builder = new FileBuilder(decider, notifier);
        builder.start();



        return true;
    }
}
