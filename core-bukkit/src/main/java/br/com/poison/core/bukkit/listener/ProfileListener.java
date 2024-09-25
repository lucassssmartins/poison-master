package br.com.poison.core.bukkit.listener;

import br.com.poison.core.Constant;
import br.com.poison.core.backend.data.ProfileData;
import br.com.poison.core.backend.data.PunishmentData;
import br.com.poison.core.bukkit.manager.PermissionManager;
import br.com.poison.core.bukkit.manager.PlayerManager;
import br.com.poison.core.bukkit.manager.VanishManager;
import br.com.poison.core.bukkit.service.server.options.ServerOptions;
import br.com.poison.core.manager.ProfileManager;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.medal.Medal;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.resources.skin.Skin;
import br.com.poison.core.resources.skin.category.SkinCategory;
import br.com.poison.core.resources.skin.texture.SkinTexture;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.resources.punishment.Punishment;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ProfileListener implements Listener {

    private final ProfileData data;
    private final ProfileManager manager;

    private final PunishmentData punishmentData;

    public ProfileListener() {
        data = Core.getProfileData();
        manager = Core.getProfileManager();

        punishmentData = Core.getPunishmentData();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void punish(AsyncPlayerPreLoginEvent event) {
        Profile profile = Core.getProfileData().read(event.getUniqueId(), false);

        if (profile == null) return;

        Punishment ban = punishmentData.hasActiveBan(profile);

        if (ban != null) {
            if (ban.isValid() && (!ban.hasExpired() || ban.isPermanent())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, String.format(Constant.BAN_TEMPLATE_MESSAGE,
                        ban.isPermanent() ? "permanentemente" : "temporariamente",
                        ban.getReason().getInfo(),
                        (!ban.isPermanent() ? "§cExpira em: " + TimeUtil.formatTime(ban.getExpiresAt()) : "") + "\n",
                        ban.getCode()
                ));
                return;
            }

            punishmentData.delete(ban);
        }

        event.allow();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID id = event.getUniqueId();
        String name = event.getName();

        try {
            Profile profile = data.read(id, true);

            if (profile == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED + " (Ex0001)");
                return;
            }

            // Carregando a skin do perfíl.
            if (profile.hasSkin()) {
                Skin current = profile.getSkin(),
                        normal = Core.getSkinManager().fetch(profile.getId(), profile.getName(), SkinCategory.PROFILE);

                if (current.getCategory().equals(SkinCategory.PROFILE) && !current.equals(normal))
                    profile.setSkin(normal);

            } else {
                Skin skin = Core.getSkinManager().fetch(profile.getId(), profile.getName(), SkinCategory.PROFILE);

                if (skin == null || !profile.isAuthentic())
                    skin = new Skin("Cassio", SkinCategory.PROFILE, new SkinTexture("ewogICJ0aW1lc3RhbXAiIDogMTcwOTI2MDU1NTQ0NiwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=", "NxokmKscuDwbpN4PNWO1r3aPXp9IdG+gKQjkER+lrPZuK+55gBBYWCa0Yly5O7uJTY+0BiJLD9NPw8hPHvFbsS24oInimkUPsSWCkahvqZUohJAkNSGSYuNxB54KYRPP3p9tQtFnZ4Hr5Au9C7M/jQqwwL9EVJDXvd/wy1JsbwPZoqnwfv+xAbZnrQc8lsCEsPXydV/YJ/Wrvl5kkQTeJTD1SzHcavZmF1g8Iuhf/we9kKaOmHW/tfuw4kAXOrq1oms7taEO1ZqjriZ5z2qvYKwlHLfGqs9TAxGSJFZIbSDSnlrxrkFze/kMJrUfXSANxYBC+8p7eCm7fOHKZIPrL31yqYZOtzm7ncjjIIxL4cC5XbDzus5ReO4YXmnV480Owr9hYNZWYZPjwOFgl4Z+pbB8Y23BVSM7XW5kvHfsiIlXRYp1Ze6JJ1hX9vcU8gCZAEdhAzmU4KKM/bUgHRqQSPzZ+Jw0qjkaO+Ep3C+U5lNmqg3omlrYCaLxgfMobTqAkQ/PdUqDni3FDeJLW8qwsLjANKqLH0V8+No68YgvEUeTycCteMyVvpvGiNVeEA8qZkyEWfvdeKOR/NxPOxl8Fz0tEDI8TjP+stcgozvEqYGTDdLkgYn+bzcKMBUXVINRjBasBT/0vIpNQ9DEeTbb1fuGCsj3kSEwLGwnysf6QBE="));

                profile.setSkin(skin);
            }

            manager.save(profile);

            event.allow();
        } catch (Exception e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED + " (Ex0002)");

            Core.getLogger().log(Level.WARNING, "Não foi possível registrar a conta do jogador " + name, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Profile profile = manager.read(event.getPlayer().getUniqueId());

        if (profile == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED + " (Ex0003)");
            return;
        }

        event.allow();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        Profile profile = manager.read(player.getUniqueId());

        player.refresh();

        if (ServerOptions.DEFAULT_TAB_LIST)
            player.setPlayerListHeaderFooter(
                    // Header
                    TextComponent.fromLegacyText("\n" + Constant.SERVER_TITLE + "\n"),
                    // Footer
                    TextComponent.fromLegacyText("\n  §eDiscord: §f" + Constant.DISCORD + "  \n  §eLoja: §f" + Constant.STORE + "  \n")
            );

        if (profile.hasRank(RankCategory.TRIAL) && profile.getPreference().isAutoVanishMode()
                && !Core.getServerCategory().equals(ServerCategory.DUELS))
            VanishManager.vanish(profile);

        PermissionManager.loadPermissions(player);

        if (profile.hasSkin())
            PlayerManager.changePlayerSkin(event.getPlayer(), profile.getSkin(), false);

        ServerCategory server = Core.getServerCategory();

        if (!profile.inServer(server) && !server.isAllowArcade())
            profile.setServer(server);

        if (!profile.hasTag(profile.getTag()))
            profile.setTag(profile.getDefaultTag());

        profile.load();

        // Go Teleport
        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            String id = jedis.get(Constant.TELEPORT_KEY + profile.getId());

            if (id != null) {
                Player target = Bukkit.getPlayer(UUID.fromString(id));

                if (target != null) {
                    player.teleport(target);

                    player.sendMessage("§aVocê foi redirecionado para o §e" + target.getName() + "§a!");
                }
            }
        }

        System.out.println(profile.getName() + " entrou no servidor.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        onExit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);

        onExit(event.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        Profile profile = manager.read(player.getUniqueId());

        if (profile == null) return;

        String message = event.getMessage();

        if (profile.hasCooldown(Constant.CHAT_COOLDOWN_KEY)) {
            profile.sendMessage("§cAguarde " + TimeUtil.newFormatTime(profile.getCooldown(Constant.CHAT_COOLDOWN_KEY))
                    + " para conversar novamente.");
            return;
        }

        if (!profile.isStaffer() && !ServerOptions.CHAT_ENABLED) {
            profile.sendMessage("§cO chat do servidor está desativado.");
            return;
        }

        if (ServerOptions.DEFAULT_CHAT) {
            Tag tag = profile.getTag();
            Medal medal = profile.getMedal();

            TextComponent medalComponent = new TextComponent(!medal.equals(Medal.VOID) ? medal.getColoredSymbol() + " " : "");

            if (!medal.equals(Medal.VOID))
                medalComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                        "§7" + medal.getLore()
                )));

            TextComponent chat = new TextComponent(tag.getPrefix()
                    + (profile.isUsingFake() ? profile.getNickname() : profile.getName()) + ": ");

            TextComponent messageComponent = new TextComponent(
                    profile.hasRank(RankCategory.POISON) ? "§f" + Util.color(message) : "§7" + message);

            manager.documents(user -> player.inWorld(user.player().getWorld()))
                    .forEach(user -> user.sendMessage(medalComponent, chat, messageComponent));

            if (!profile.hasRank(RankCategory.POISON))
                profile.setCooldown(Constant.CHAT_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(4));

            System.out.println("[Chat] " + profile.getName() + ": " + message);
        }
    }

    protected void onExit(Player player) {
        Profile profile = manager.read(player.getUniqueId());

        if (profile != null) {
            profile.unload();

            VanishManager.removeVanish(profile);

            manager.remove(player.getUniqueId());
        }

        BukkitCore.getSidebarManager().delete(player);

        System.out.println(player.getName() + " saiu do servidor.");
    }
}
