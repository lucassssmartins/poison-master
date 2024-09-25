package br.com.poison.core.bukkit.manager;

import br.com.poison.core.bukkit.api.mechanics.npc.Npc;
import br.com.poison.core.bukkit.api.mechanics.npc.type.client.NpcClient;
import br.com.poison.core.bukkit.api.mechanics.npc.type.server.NpcServer;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NpcManager {

    protected static double cosFOV = Math.cos(Math.toRadians(60));
    protected static double bukkitRange = NumberConversions.square(Bukkit.getViewDistance() << 4);

    private static Field entityCountField;

    static {
        try {
            entityCountField = Entity.class.getDeclaredField("entityCount");
            entityCountField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Map<Integer, Npc> npcs = new ConcurrentHashMap<>();

    private boolean inViewOf(Location l, Player player) {
        Vector dir = l.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getEyeLocation().getDirection()) >= cosFOV;
    }

    public boolean canSpawn(Player player, Location loc) {
        return canSpawn(player.getLocation(), loc) && inViewOf(loc, player);
    }

    public boolean canSpawn(Location a, Location b) {
        return a.getWorld().equals(b.getWorld()) && a.distanceSquared(b) <= bukkitRange;
    }

    public Npc getNPC(int id) {
        return npcs.get(id);
    }

    public void add(Npc npc) {
        npcs.put(npc.getEntityId(), npc);
    }

    public void remove(Npc npc) {
        npcs.remove(npc.getEntityId());
    }

    public Collection<Npc> getNPCs() {
        return npcs.values();
    }

    public NpcServer spawnServer(Location location) {
        return new NpcServer(location);
    }

    public NpcServer spawnServer(Location location, Property textures) {
        return new NpcServer(location, textures);
    }

    public NpcClient spawnClient(Player receiver, Location location) {
        return new NpcClient(receiver, location);
    }

    public NpcClient spawnClient(Player receiver, Location location, Property textures) {
        return new NpcClient(receiver, location, textures);
    }

    public synchronized int nextEntityId() {
        try {
            int currentId = entityCountField.getInt(null);
            entityCountField.set(null, currentId + 1);
            return currentId;
        } catch (Exception e) {
            return -1;
        }
    }
}