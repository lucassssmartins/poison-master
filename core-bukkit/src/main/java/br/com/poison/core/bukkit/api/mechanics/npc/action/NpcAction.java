package br.com.poison.core.bukkit.api.mechanics.npc.action;

import org.bukkit.entity.Player;

public interface NpcAction {
    void handleAction(Player player, Action action);
}
