package com.axowattle.file_emulator;

import com.axowattle.file_emulator.profiles.RandomCubeProfile;
import com.axowattle.file_emulator.profiles.RandomDiamondProfile;
import com.axowattle.file_emulator.profiles.RandomPerlinNoiseProfile;
import com.axowattle.file_emulator.profiles.RandomSphereProfile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class GenerateTreeCommand implements CommandExecutor, TabCompleter {
    private final WorldData world_data;
    private final BlockNotifier notifier;

    private Map<String, PlaceProfile> profiles;

    public GenerateTreeCommand(WorldData world_data, BlockNotifier notifier) {
        this.world_data = world_data;
        this.notifier = notifier;

        profiles = new HashMap<>();
        profiles.put("spheres", new RandomSphereProfile());
        profiles.put("diamonds", new RandomDiamondProfile());
        profiles.put("cubes", new RandomCubeProfile());
        profiles.put("perlin", new RandomPerlinNoiseProfile());

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] arguments) {
        if (!(sender instanceof Player player)){
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return false;
        }
        if (arguments.length != 1){
            player.sendMessage(ChatColor.RED + "You need to enter the profile");
            return true;
        }

        if (!profiles.containsKey(arguments[0])){
            player.sendMessage(ChatColor.RED + "Could not find profile named " + arguments[0]);
            return true;
        }

        PositionDecider decider = new PositionDecider(null, profiles.get(arguments[0]), world_data, new Vector3Int(player.getLocation().toVector()));

        FileBuilder builder = new FileBuilder(decider, notifier);
        builder.start();

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>(profiles.keySet()) ;
    }
}
