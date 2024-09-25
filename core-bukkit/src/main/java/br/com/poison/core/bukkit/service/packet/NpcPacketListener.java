package br.com.poison.core.bukkit.service.packet;

import br.com.poison.core.bukkit.manager.NpcManager;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.npc.Npc;
import br.com.poison.core.bukkit.api.mechanics.npc.action.Action;
import br.com.poison.core.bukkit.api.mechanics.npc.action.NpcAction;
import br.com.poison.core.bukkit.api.mechanics.npc.type.client.NpcClient;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NpcPacketListener extends PacketAdapter {

    private final NpcManager npcManager;

    public NpcPacketListener(Plugin plugin, NpcManager npcManager) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);

        this.npcManager = npcManager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        try {
            Player player = event.getPlayer();
            PacketContainer packet = event.getPacket();

            int entityId = packet.getIntegers().read(0);
            EntityUseAction action = packet.getEntityUseActions().read(0);

            if (action == EntityUseAction.ATTACK || action == EntityUseAction.INTERACT) {
                for (Npc npc : npcManager.getNPCs()) {
                    if (!npc.isSpawned() || npc.getEntityId() != entityId) continue;

                    if (npc instanceof NpcClient && !((NpcClient) npc).getReceiver().equals(player)) continue;

                    Location loc = player.getLocation();

                    if (loc.distanceSquared(npc.getLocation()) < 36.0) {
                        NpcAction npcAction = npc.getAction();

                        if (npcAction == null) return;

                        player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 2f, 1.2f);

                        Core.getMultiService().sync(() -> npcAction.handleAction(player, action.equals(EntityUseAction.ATTACK) ? Action.LEFT : Action.RIGHT));
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

}
