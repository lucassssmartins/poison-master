package br.com.poison.core.bukkit.service.packet.hook;

import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.service.packet.NpcPacketListener;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class PacketHook {

    private final Plugin plugin;

    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public void load() {
        manager.addPacketListener(new NpcPacketListener(plugin, BukkitCore.getNpcManager()));
    }
}
