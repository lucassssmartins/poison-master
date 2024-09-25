package br.com.poison.arcade.pvp.listener;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.game.arena.Arena;
import br.com.poison.arcade.pvp.manager.GameManager;
import br.com.poison.arcade.pvp.manager.UserManager;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.event.list.member.league.MemberUpdateLeagueEvent;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.medal.Medal;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.resources.league.League;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.DateUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class UserListener implements Listener {

    private final UserManager userManager;
    private final GameManager gameManager;

    public UserListener() {
        userManager = PvP.getUserManager();
        gameManager = PvP.getGameManager();
    }

    @EventHandler
    public synchronized void route(AsyncPlayerPreLoginEvent event) {
        UUID id = event.getUniqueId();

        Profile profile = Core.getProfileManager().read(id);

        if (profile == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED);
            return;
        }

        PvPMember member = Core.getPvpData().fetch(id, true);

        if (member == null) {
            member = Core.getPvpData().input(profile);

            if (member == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED);
                return;
            }

            Core.getPvpData().persist(id);

            Core.getPvpManager().save(member);
        }

        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            String json = jedis.get(Constant.ROUTE_KEY + id);

            if (json == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNenhuma rota encontrada!");
                return;
            }

            GameRouteContext route = Core.GSON.fromJson(json, GameRouteContext.class);

            if (!route.isValid()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNenhuma rota encontrada!");
                return;
            }

            Game game = gameManager.getGame(route.getArcade());

            if (game == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNenhum modo de jogo encontrado!");
                return;
            }

            Arena arena = gameManager.getBestArena(game);

            if (arena == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNenhum sala encontrada!");
                return;
            }

            new User(profile, member, game, arena, GameRouteContext.builder()
                    .arcade(game.getCategory())
                    .id(arena.getId())
                    .map(arena.getMap().getId())
                    .input(InputMode.PLAYER)
                    .slot(arena.getSlot())
                    .build());

            jedis.del(Constant.ROUTE_KEY + id);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = userManager.read(player.getUniqueId());

        if (user == null) return;

        Profile profile = user.getProfile();

        user.load();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        exit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        exit(event.getPlayer());
    }

    @EventHandler
    public void league(MemberUpdateLeagueEvent event) {
        User user = userManager.read(event.getMember().getId());

        if (user != null)
            user.getGame().updateSidebar(user);
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        User user = PvP.getUserManager().read(event.getPlayer().getUniqueId());

        if (user == null) return;

        Profile profile = user.getProfile();
        PvPMember member = user.getMember();

        Tag tag = profile.getTag();
        Medal medal = profile.getMedal();

        League league = member.getLeague();

        String message = event.getMessage();

        TextComponent medalComponent = new TextComponent(!medal.equals(Medal.VOID) ? medal.getColoredSymbol() + " " : "");

        if (!medal.equals(Medal.VOID))
            medalComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                    "§7" + medal.getLore()
            )));

        TextComponent leagueComponent = new TextComponent(league.getSymbolPrefix() + " ");

        TextComponent chat = new TextComponent(tag.getPrefix() + (profile.isUsingFake() ? profile.getNickname() : profile.getName()) + ": ");

        TextComponent messageComponent = new TextComponent(profile.isVIP() ? "§f" + Util.color(message) : "§7" + message);

        messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText("§fEnviado em: §7§o" + DateUtil.getDate(System.currentTimeMillis()))));

        Core.getProfileManager().documents()
                .forEach(target -> target.sendMessage(medalComponent, leagueComponent, chat, messageComponent));

        System.out.println("[" + user.getGame().getName() + "/" + user.getArena().getDiscriminator() + "] " + profile.getName() + ": " + message);
    }

    protected void exit(Player player) {
        User user = userManager.read(player.getUniqueId());

        if (user != null) {
            user.getGame().removePlayer(player.getUniqueId());
            user.getArena().leaveMember(player);

            userManager.remove(player.getUniqueId());
        }

        Core.getPvpData().cache(player.getUniqueId());
    }
}
