package br.com.poison.core.arcade.room.map.rollback;

import br.com.poison.core.util.extra.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Getter
@Setter
public class RollbackBlock {

    private final Location location;

    private Pattern pattern;
    private RollbackType type;

    public enum RollbackType {REMOVE_BLOCK, PLACE_BLOCK}

    public RollbackBlock(Location location, RollbackType type) {
        this.location = location;
        this.type = type;

        Block block = location.getWorld().getBlockAt(location);

        this.pattern = new Pattern(block.getType(), block.getData(), block.isBlockPowered());
    }

    @Override
    public String toString() {
        return "RollbackBlock{" +
                "location=" + location.toString() +
                ", pattern=" + pattern +
                ", type=" + type +
                '}';
    }

    public boolean hasType(RollbackType type) {
        return this.type.equals(type);
    }
}
