package com.axowattle.file_emulator;

import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class RandomBlocks {
    private static final Set<Material> ADMIN_ONLY = Set.of(
            Material.BARRIER,
            Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID,
            Material.JIGSAW,
            Material.LIGHT,
            Material.COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.DEBUG_STICK
    );

    /** All materials that have block entities (tile entities) and should be excluded. */
    private static final Set<Material> BLOCK_ENTITIES = Set.of(
            // Inventories
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.BARREL,
            Material.HOPPER,
            Material.DISPENSER,
            Material.DROPPER,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BREWING_STAND,
            Material.BEACON,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,

            // Redstone-related with state
            Material.COMPARATOR,
            Material.REPEATER,
            Material.DAYLIGHT_DETECTOR,
            Material.JUKEBOX,
            Material.NOTE_BLOCK,

            // Signs
            Material.OAK_SIGN,
            Material.SPRUCE_SIGN,
            Material.BIRCH_SIGN,
            Material.JUNGLE_SIGN,
            Material.ACACIA_SIGN,
            Material.DARK_OAK_SIGN,
            Material.MANGROVE_SIGN,
            Material.CHERRY_SIGN,
            Material.BAMBOO_SIGN,
            Material.CRIMSON_SIGN,
            Material.WARPED_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.MANGROVE_WALL_SIGN,
            Material.CHERRY_WALL_SIGN,
            Material.BAMBOO_WALL_SIGN,
            Material.CRIMSON_WALL_SIGN,
            Material.WARPED_WALL_SIGN,

            // Misc block entities
            Material.ENCHANTING_TABLE,
            Material.LECTERN,
            Material.SPAWNER,
            Material.PLAYER_HEAD,
            Material.VAULT,
            Material.CRAFTER,
            Material.TNT,
            Material.TRIAL_SPAWNER,
            Material.PLAYER_WALL_HEAD,
            Material.DRAGON_HEAD,
            Material.DRAGON_WALL_HEAD,
            Material.CREEPER_HEAD,
            Material.CREEPER_WALL_HEAD,
            Material.ZOMBIE_HEAD,
            Material.ZOMBIE_WALL_HEAD,
            Material.SKELETON_SKULL,
            Material.SKELETON_WALL_SKULL,
            Material.WITHER_SKELETON_SKULL,
            Material.WITHER_SKELETON_WALL_SKULL
    );

    // Precompute once
    private static final List<Material> FULL_BLOCKS_ALL = Collections.unmodifiableList(
            Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .filter(Material::isSolid)       // physical block
                    .filter(Material::isOccluding)   // visually full cube
                    .filter(m -> !ADMIN_ONLY.contains(m))
                    .filter(m -> !BLOCK_ENTITIES.contains(m))
                    .filter(m -> !m.name().contains("INFESTED"))
                    .collect(Collectors.toList())
    );

    private static final List<Material> FULL_BLOCKS_NO_GRAVITY = Collections.unmodifiableList(
            FULL_BLOCKS_ALL.stream()
                    .filter(m -> !m.hasGravity())
                    .collect(Collectors.toList())
    );

    /** Random full block (solid + occluding). Includes gravity blocks. */
    public static Material randomFullBlock() {
        return pick(FULL_BLOCKS_ALL);
    }

    /** Random full block, optionally excluding gravity blocks (sand/gravel/etc.). */
    public static Material randomFullBlock(boolean excludeGravity) {
        return pick(excludeGravity ? FULL_BLOCKS_NO_GRAVITY : FULL_BLOCKS_ALL);
    }

    private static Material pick(List<Material> pool) {
        if (pool.isEmpty()) throw new IllegalStateException("No eligible full blocks found");
        int i = ThreadLocalRandom.current().nextInt(pool.size());
        return pool.get(i);
    }
}
