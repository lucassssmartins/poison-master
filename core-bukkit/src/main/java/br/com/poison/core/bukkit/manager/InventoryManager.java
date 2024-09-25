package br.com.poison.core.bukkit.manager;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class InventoryManager {

    private final Map<UUID, Inventory> map = new HashMap<>();

    public Inventory fetch(UUID uuid) {
        return map.get(uuid);
    }

    public boolean exists(UUID uuid) {
        return map.containsKey(uuid);
    }

    public void save(Player player, Inventory inventory) {
        map.put(player.getUniqueId(), inventory);
    }

    public void delete(UUID uuid) {
        map.remove(uuid);
    }
}
