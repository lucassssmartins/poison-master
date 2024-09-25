package br.com.poison.core.bukkit.api.mechanics.hologram.row;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.hologram.Hologram;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@Getter
@ToString
public class Row {

    private final Hologram hologram;
    private final EntityArmorStand stand;

    private Location location;

    public Row(Hologram hologram, Location location, String text) {
        this.hologram = hologram;
        this.stand = handleStand(location, text);

        this.location = location;
    }

    protected EntityArmorStand handleStand(Location location, String text) {
        CraftWorld world = (CraftWorld) location.getWorld();

        if (world == null) {
            Core.getLogger().info("Mundo não encontrado!");
            return null;
        }

        EntityArmorStand stand = new EntityArmorStand(world.getHandle());

        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        stand.setCustomName(text);
        stand.setCustomNameVisible(!text.isEmpty());

        stand.setGravity(false);
        stand.setInvisible(true);

        stand.n(false); // Marker

        return stand;
    }

    public void teleport(Location location) {
        this.location = location;

        stand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /* Methods Start */

    public String getText() {
        return stand.getCustomName();
    }

    public void setText(String text) {
        stand.setCustomName(text);
    }

    public int getId() {
        return stand.getId();
    }

    /* Methods End */

    /* Entity Methods */

    public void spawn(Player player) {
        PacketPlayOutSpawnEntityLiving living = new PacketPlayOutSpawnEntityLiving(stand);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(living);
    }

    public void despawn(Player player) {
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(this.getId());

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);
    }

    public void respawn(Player player) {
        despawn(player);

        spawn(player);
    }

    public void update(Player player) {
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(this.getId(), stand.getDataWatcher(), true);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metadata);
    }
}
