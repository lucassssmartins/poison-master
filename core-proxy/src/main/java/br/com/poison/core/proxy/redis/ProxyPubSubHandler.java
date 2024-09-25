package br.com.poison.core.proxy.redis;

import br.com.poison.core.Constant;
import br.com.poison.core.server.Server;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.extra.ReflectionUtil;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.proxy.event.list.profile.field.ProfileFieldChangeEvent;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

public final class ProxyPubSubHandler extends JedisPubSub {

    @SuppressWarnings("deprecation")
    @Override
    public void onMessage(String channel, String message) {
        JsonObject json = Core.PARSER.parse(message).getAsJsonObject();

        if (json == null) return;

        if (channel.equalsIgnoreCase(Constant.REDIS_PROFILE_CHANNEL)) {
            if (!(json.has("id") && json.has("field") && json.has("value"))) return;

            Profile profile = Core.getProfileManager().read(UUID.fromString(json.get("id").getAsString()));
            if (profile == null) return;

            try {
                Field field = ReflectionUtil.getField(Profile.class, json.get("field").getAsString());

                Object old = field.get(profile), value = Core.GSON.fromJson(json.get("value"), field.getGenericType());

                field.set(profile, value);

                new ProfileFieldChangeEvent(profile, field, old, value).call();
            } catch (Exception e) {
                profile.sendMessage("§cNão foi possível atualizar as suas informações!");

                Core.getLogger().log(Level.WARNING, "Não foi possível atualizar as informações de " + profile.getName(), e);
            }

            return;
        }

        if (channel.equalsIgnoreCase(Constant.REDIS_SERVER_SENDER_CHANNEL)) {
            if (!(json.has("id") && json.has("category"))) return;

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(json.get("id").getAsString()));
            if (player == null) return;

            Server server = Core.getServerManager().getServer(ServerCategory.fetch(json.get("category").getAsString()));

            if (server == null || server.getServerInfo() == null) {
                player.sendMessage(TextComponent.fromLegacyText("§cNenhum servidor encontrado!"));
                return;
            }

            player.connect(server.getServerInfo());
        }
    }
}
