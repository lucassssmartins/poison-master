package br.com.poison.core.arcade.room.map.synthetic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@AllArgsConstructor
public class SyntheticLocation {

    private double x, y, z;
    private float yaw, pitch;

    public Location getBukkitLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "SyntheticLocation{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                '}';
    }
}
