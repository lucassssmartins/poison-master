package br.com.poison.core.bukkit;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.backend.database.redis.channel.RedisPubSub;
import br.com.poison.core.bukkit.command.structure.BukkitCommandLoader;
import br.com.poison.core.bukkit.listener.loader.ListenerLoader;
import br.com.poison.core.bukkit.manager.*;
import br.com.poison.core.bukkit.service.BukkitMultiService;
import br.com.poison.core.bukkit.service.packet.hook.PacketHook;
import br.com.poison.core.bukkit.service.redis.BukkitPubSubHandler;
import br.com.poison.core.bukkit.service.server.BukkitServer;
import br.com.poison.core.util.bukkit.BukkitUtil;
import br.com.poison.core.bukkit.api.user.cooldown.CooldownManager;
import br.com.poison.core.bukkit.api.user.permission.regex.RegexPermissions;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.viaversion.viaversion.api.Via;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class BukkitCore extends JavaPlugin {

    @Getter
    protected static ProtocolManager protocolManager;

    @Getter
    protected static InventoryManager inventoryManager;

    @Getter
    protected static SidebarManager sidebarManager;

    @Getter
    protected static NpcManager npcManager;

    @Getter
    protected static HologramManager hologramManager;

    @Getter
    protected static CooldownManager cooldownManager;

    protected RegexPermissions regexPermissions;

    @Override
    public void onLoad() {
        // Registrando Gerenciadores
        protocolManager = ProtocolLibrary.getProtocolManager();

        npcManager = new NpcManager();
        hologramManager = new HologramManager();

        cooldownManager = new CooldownManager();

        inventoryManager = new InventoryManager();
        sidebarManager = new SidebarManager();

        Core.init(new BukkitMultiService(this), getLogger(), new BukkitServer());
    }

    @Override
    public void onEnable() {
        Via.getConfig().setCheckForUpdates(false);

        // Registrando canais do Redis
        CompletableFuture.runAsync(() ->
                new RedisPubSub(new BukkitPubSubHandler(), Constant.REDIS_CHANNELS).registerChannels());

        // Iniciando servidor no Redis
        Core.getServerManager().init(Bukkit.getMaxPlayers());

        // Desativando comandos do Minecraft
        Core.getMultiService().syncLater(() -> {
            BukkitUtil.removeCommand(this, BukkitConstant.COMMANDS_TO_DISABLE);
        }, 3L);

        // Registrando comandos e Eventos
        new BukkitCommandLoader(this).register("br.com.poison.core.bukkit.command.list");
        new ListenerLoader(this).register("br.com.poison.core.bukkit");

        // Permissions
        regexPermissions = new RegexPermissions();

        // Registrando tasks
        TaskManager.registerTasks(this, "br.com.poison.core.bukkit.service.task.list");

        // Iniciando Packets Customizados (NPC, Holograma, Command, etc...)
        new PacketHook(this).load();

        Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(Entity::remove));

        Core.getLogger().info("Core iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        TaskManager.unloadTasks();

        Core.closeup();

        regexPermissions.onDisable();

        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("§cA sala está reiniciando, tente entrar novamente mais tarde!"));
    }
}
