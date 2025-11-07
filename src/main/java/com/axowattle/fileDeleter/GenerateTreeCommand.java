package com.axowattle.fileDeleter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.nio.file.Paths;
import java.util.List;

public class GenerateTreeCommand implements CommandExecutor {
    private final BlockPlacer placer;
    private final BlockNotifier notifier;

    public GenerateTreeCommand(BlockPlacer placer, BlockNotifier notifier) {
        this.placer = placer;
        this.notifier = notifier;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return false;
        }
        Player player = (Player) sender;

        PositionDecider decider = new PositionDecider(null, placer, null, player.getLocation().toVector());

        FileBuilder builder = new FileBuilder(decider, notifier);
        builder.start();



        return true;
    }
}
