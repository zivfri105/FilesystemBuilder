package com.axowattle.file_emulator;

import org.bukkit.Material;

public interface PlaceProfile {
    double position_value(Vector3Int position, PositionDecider.PathNode path_node);

    void on_path_enter(PositionDecider.PathNode new_node);
    void on_path_exit(PositionDecider.PathNode old_node);

    Material select_material(Vector3Int position);
}
