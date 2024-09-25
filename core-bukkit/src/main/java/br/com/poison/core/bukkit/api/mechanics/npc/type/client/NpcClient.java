package br.com.poison.core.bukkit.api.mechanics.npc.type.client;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.npc.Npc;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public class NpcClient extends Npc {

    private final Player receiver;

    public NpcClient(Player receiver, Location location) {
        this(receiver, location, null);
    }

    public NpcClient(Player receiver, Location location, Property textures) {
        super(location);

        this.receiver = receiver;

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

        Core.getMultiService().syncLater(() -> spawnTo(receiver), 2L);
    }

    @Override
    public void destroy() {
        super.destroy();

        despawnTo(receiver);
    }
}
