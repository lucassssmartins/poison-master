package br.com.poison.core.bukkit.api.mechanics.hologram;

import br.com.poison.core.bukkit.api.mechanics.hologram.row.Row;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
@ToString
public abstract class Hologram {

    private int id;

    private final String tag;

    private final List<Row> lines = new ArrayList<>();

    private final Set<Player> viewers = new HashSet<>();

    private Location location;

    public Hologram(String tag, Location location) {
        this(tag, location, -1L);
    }

    public Hologram(String tag, Location location, long expiresAt) {
        this.tag = tag;
        this.location = location;
    }

    public abstract void setText(List<String> lines);

    public abstract void updateRows();

    public World getWorld() {
        return location.getWorld();
    }

    public void teleport(Location location) {
        setLocation(location);

        if (lines.isEmpty()) return;

        location = location.clone();

        for (Row row : lines) {
            row.teleport(location);

            location = location.clone().subtract(0, 0.25, 0);
        }
    }

    public void setText(int index, String text) {
        Row row = lines.get(index);

        row.setText(text);
        viewers.forEach(row::update);
    }

    public void spawnTo(Player player) {
        if (!viewers.contains(player)) {
            lines.forEach(line -> line.spawn(player));

            viewers.add(player);
        }
    }

    public void despawnTo(Player player) {
        if (viewers.contains(player)) {
            lines.forEach(line -> line.despawn(player));

            viewers.remove(player);
        }
    }
}
