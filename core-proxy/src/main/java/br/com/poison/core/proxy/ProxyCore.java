package br.com.poison.core.proxy;

import br.com.poison.core.Constant;
import br.com.poison.core.backend.database.redis.channel.RedisPubSub;
import br.com.poison.core.proxy.command.structure.ProxyCommandLoader;
import br.com.poison.core.proxy.listener.loader.ListenerLoader;
import br.com.poison.core.proxy.message.AutoMessage;
import br.com.poison.core.proxy.redis.ProxyPubSubHandler;
import br.com.poison.core.proxy.server.ProxyServer;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProxyCore extends Plugin {

    @Override
    public void onLoad() {
        Core.setServerCategory(ServerCategory.PROXY);

        Core.init(new ProxyMultiService(this), getLogger(), new ProxyServer());
    }

    @Override
    public void onEnable() {
        CompletableFuture.runAsync(() -> new RedisPubSub(new ProxyPubSubHandler(), Constant.REDIS_CHANNELS).registerChannels());

        new ProxyCommandLoader(this).register("br.com.poison.core.proxy.command.list");
        new ListenerLoader(this).register("br.com.poison.core.proxy");

        Core.getServerManager().init(2023);

        getProxy().getScheduler().schedule(this, () -> {
            Core.getServerManager().update(getProxy().getOnlineCount());

            Core.getInvitationManager().checkInvitations();

            // Atualizando contas dos jogadores
            for (Profile profile : Core.getProfileManager().documents()) {
                if (profile == null) continue;

                profile.checkAll();
            }
        }, 0, 1, TimeUnit.SECONDS);

        new AutoMessage(this).run();

        Core.getLogger().info("Proxy iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        Core.closeup();

        getProxy().getPlayers().forEach(player -> player.disconnect(TextComponent.fromLegacyText("§cO servidor está reiniciando, tente entrar novamente mais tarde!")));
    }
}
