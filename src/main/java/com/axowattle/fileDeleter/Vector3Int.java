package com.axowattle.fileDeleter;

import com.google.common.base.Objects;
import org.bukkit.util.Vector;

public class Vector3Int {
    public int x;
    public int y;
    public int z;

    public Vector3Int(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3Int(Vector vec){
        this.x = vec.getBlockX();
        this.y = vec.getBlockY();
        this.z = vec.getBlockZ();
    }

    public Vector3Int clone(){
        return new Vector3Int(x, y, z);
    }

    public Vector3Int add(int x, int y, int z){
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public int squared_distance(Vector3Int other){
        if (other == null) return 0;

        int dx = x - other.x;
        int dy = y - other.y;
        int dz = z - other.z;

        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Vector3Int other)) return false;
        return x == other.x && y == other.y && z == other.z;
    }
}
