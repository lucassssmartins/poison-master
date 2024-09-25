package br.com.poison.core.bukkit.service.redis;

import br.com.poison.core.bukkit.event.list.member.league.MemberUpdateLeagueEvent;
import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanLeaveEvent;
import br.com.poison.core.bukkit.event.list.profile.field.ProfileFieldEvent;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import com.google.gson.JsonObject;
import br.com.poison.core.backend.database.redis.message.type.LeagueUpdateMessage;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.util.extra.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

public final class BukkitPubSubHandler extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        JsonObject json = Core.PARSER.parse(message).getAsJsonObject();

        if (channel.equalsIgnoreCase(Constant.REDIS_PROFILE_CHANNEL)) {
            if (!(json.has("id") && json.has("field") && json.has("value"))) return;

            Profile profile = Core.getProfileManager().read(UUID.fromString(json.get("id").getAsString()));
            if (profile == null) return;

            try {
                Field field = ReflectionUtil.getField(Profile.class, json.get("field").getAsString());

                Object old = field.get(profile), value = Core.GSON.fromJson(json.get("value"), field.getGenericType());

                field.set(profile, value);

                new ProfileFieldEvent(profile, field, old, value).call();
            } catch (Exception e) {
                profile.sendMessage("§cNão foi possível atualizar as suas informações!");

                Core.getLogger().log(Level.WARNING, "Não foi possível atualizar as informações de " + profile.getName(), e);
            }

            return;
        }

        if (channel.equalsIgnoreCase(Constant.REDIS_CLAN_DELETE_CHANNEL)) {
            Profile profile = Core.getProfileData().read(UUID.fromString(json.get("id").getAsString()), false);

            if (profile != null)
                new ProfileClanLeaveEvent(profile).call();
        }

        if (channel.equalsIgnoreCase(Constant.REDIS_LEAGUE_UPDATE_CHANNEL)) {
            LeagueUpdateMessage league = Core.GSON.fromJson(message, LeagueUpdateMessage.class);

            new MemberUpdateLeagueEvent(league.getMember(), league.getLeague()).call();
        }

        if (channel.equalsIgnoreCase(Constant.REDIS_TELEPORT_CHANNEL)) {
            if (!(json.has("sender") && json.has("target"))) return;

            Player sender = Bukkit.getPlayer(UUID.fromString(json.get("sender").getAsString())),
                    target = Bukkit.getPlayer(UUID.fromString(json.get("target").getAsString()));

            if (sender == null || target == null) return;

            sender.teleport(target);
            sender.sendMessage("§aVocê foi redirecionado até o jogador §e" + target.getName() + "§a!");
        }
    }
}
