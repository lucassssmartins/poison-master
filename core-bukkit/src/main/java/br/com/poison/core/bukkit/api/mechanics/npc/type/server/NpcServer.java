package br.com.poison.core.bukkit.api.mechanics.npc.type.server;

import br.com.poison.core.bukkit.api.mechanics.npc.Npc;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NpcServer extends Npc {
    
    public NpcServer(Location location) {
        this(location, null);
    }

    public NpcServer(Location location, Property textures) {
        super(location);

        if (textures != null) {
            GameProfile skinProfile = new GameProfile(getUuid(), "NPC");

            skinProfile.getProperties().clear();
            skinProfile.getProperties().put("textures", textures);

            Property skinTextures = skinProfile.getProperties().get("textures").stream().findFirst().orElse(null);

            if (skinTextures != null) {
                getProfile().getProperties().clear();

                getProfile().getProperties().put("textures", skinTextures);
            }
        }
    }

    @Override
    public void display() {
        super.display();

        Bukkit.getOnlinePlayers().forEach(this::spawnTo);
    }

    @Override
    public void destroy() {
        super.destroy();

        Bukkit.getOnlinePlayers().forEach(this::despawnTo);
    }
}
