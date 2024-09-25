package br.com.poison.core.bukkit.manager;

import br.com.poison.core.bukkit.api.mechanics.hologram.type.client.HologramClient;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.hologram.Hologram;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.preference.Preference;
import br.com.poison.core.util.extra.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class HologramManager {

    protected final double range = NumberConversions.square(Bukkit.getViewDistance() << 4);

    private final Map<Integer, Hologram> holograms = new ConcurrentHashMap<>();

    public boolean canSpawn(Hologram hologram, Location location) {
        Location hologramLocation = hologram.getLocation();

        return location.getWorld().equals(hologramLocation.getWorld())
                && location.distanceSquared(hologramLocation) <= range;
    }

    public HologramServer getServer(String tag) {
        return (HologramServer) getHolograms().values().stream()
                .filter(hologram -> hologram.getClass().isAssignableFrom(HologramServer.class) && hologram.getTag().equalsIgnoreCase(tag))
                .findFirst()
                .orElse(null);
    }

    public List<HologramServer> getServers(String tag) {
        List<Hologram> holograms = getHolograms().values().stream()
                .filter(hologram -> hologram.getClass().isAssignableFrom(HologramServer.class) && hologram.getTag().equalsIgnoreCase(tag))
                .collect(Collectors.toList());

        List<HologramServer> servers = new ArrayList<>();

        holograms.forEach(hologram -> servers.add((HologramServer) hologram));

        return servers;
    }

    public HologramClient getClient(String tag) {
        return (HologramClient) getHolograms().values().stream()
                .filter(hologram -> hologram.getClass().isAssignableFrom(HologramClient.class) && hologram.getTag().equalsIgnoreCase(tag))
                .findFirst()
                .orElse(null);
    }

    public void removeClient(HologramClient client) {
        Profile profile = Core.getProfileData().read(client.getReceiver().getUniqueId(), false);

        if (profile == null) return;

        client.despawnTo(client.getReceiver());

        holograms.remove(client.getId());
    }

    public HologramClient spawnClient(Player receiver, String tag, Location location) {
        int id = Integer.parseInt(StringUtils.generateNumberCode(5));

        HologramClient client = new HologramClient(receiver, tag, location);

        client.setId(id);

        holograms.put(id, client);

        return client;
    }

    public HologramClient spawnClient(Player receiver, String tag, Location location, long expiresAt) {
        int id = Integer.parseInt(StringUtils.generateNumberCode(5));

        HologramClient client = new HologramClient(receiver, tag, location, expiresAt);

        client.setId(id);

        holograms.put(id, client);

        return client;
    }

    public HologramServer spawnServer(String tag, Location location) {
        int id = Integer.parseInt(StringUtils.generateNumberCode(5));

        HologramServer server = new HologramServer(tag, location);

        server.setId(id);

        holograms.put(id, server);

        return server;
    }

    public HologramServer spawnServer(String tag, Location location, long expiresAt) {
        int id = Integer.parseInt(StringUtils.generateNumberCode(5));

        HologramServer server = new HologramServer(tag, location, expiresAt);

        server.setId(id);

        holograms.put(id, server);

        return server;
    }

}
