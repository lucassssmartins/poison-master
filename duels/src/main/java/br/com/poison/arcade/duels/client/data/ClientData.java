package br.com.poison.arcade.duels.client.data;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.stats.ClientStats;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ClientData {

    private final ClientStats stats;

    private Kit kit = null;

    private UUID lastHit = null;

    private int kills = 0, points = 0, placedBlocks = 0;

    private final long joinedAt = System.currentTimeMillis();

    public String getKitName() {
        return kit != null ? kit.getName() : "Nenhum";
    }

    public boolean isUsingKit(Kit kit) {
        return this.kit != null && this.kit.getName().equalsIgnoreCase(kit.getName());
    }

    public void setPlayer() {
        stats.setType(ClientStatsType.PLAYER);
        stats.setDuration(ClientStatsType.PLAYER.getDuration());
    }

    public boolean isStats(ClientStatsType type) {
        return stats.getType().equals(type);
    }

    public boolean hasLastHit() {
        return lastHit != null && getLastHitPlayer() != null;
    }

    public Player getLastHitPlayer() {
        return Bukkit.getPlayer(lastHit);
    }

    public Client getLastHitClient() {
        return Duels.getClientManager().read(lastHit);
    }
}
