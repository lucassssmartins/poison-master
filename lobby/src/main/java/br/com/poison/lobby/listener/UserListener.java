package br.com.poison.lobby.listener;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.game.arena.Arena;
import br.com.poison.lobby.manager.GameManager;
import br.com.poison.lobby.manager.UserManager;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.lobby.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserListener implements Listener {

    private final UserManager userManager;
    private final GameManager gameManager;

    public UserListener() {
        userManager = Lobby.getUserManager();
        gameManager = Lobby.getGameManager();
    }

    @EventHandler
    public synchronized void route(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
            UUID id = event.getUniqueId();

            Profile profile = Core.getProfileData().read(id, true);

            if (profile == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED + " (Ex0005)");
                return;
            }

            try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
                String name = jedis.get(Constant.ROUTE_KEY + id);

                ArcadeCategory category = ArcadeCategory.HUB;

                if (name != null)
                    category = ArcadeCategory.fetch(name);

                Game game = gameManager.getGame(category);

                if (game == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNenhum jogo encontrado!");
                    return;
                }

                Arena arena = gameManager.getBestArena(game);

                if (arena == null) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNenhum sala encontrada!");
                    return;
                }

                new User(profile, game, arena, GameRouteContext.builder()
                        .arcade(game.getCategory())
                        .id(arena.getId())
                        .map(arena.getMap().getId())
                        .slot(arena.getSlot())
                        .input(InputMode.PLAYER)
                        .build());

                jedis.del(Constant.ROUTE_KEY + id);
            }
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user = userManager.read(player.getUniqueId());

        if (user != null)
            user.load();

        player.setWalkSpeed(0.25f);
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
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = Lobby.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        ItemStack hand = player.getItemInHand();

        if (hand == null || hand.getType().equals(Material.AIR)) return;

        if (hand.getType().equals(Material.INK_SACK)) {
            if (user.getProfile().hasCooldown("visible-players")) {
                player.sendMessage("§cAguarde " + TimeUtil.newFormatTime(user.getProfile().getCooldown("visible-players"))
                        + " para " + (user.isVisiblePlayers() ? "esconder" : "mostrar") + " os jogadores novamente.");
                return;
            }

            user.setVisiblePlayers(!user.isVisiblePlayers());

            player.setItemInHand(new Item(Material.INK_SACK, 1, user.isVisiblePlayers() ? 10 : 8)
                    .name("§fJogadores: " + (user.isVisiblePlayers() ? "§aON" : "§cOFF")));

            if (user.isVisiblePlayers())
                Bukkit.getOnlinePlayers().forEach(player::showPlayer);
            else
                Bukkit.getOnlinePlayers().forEach(player::hidePlayer);

            player.sendMessage(user.isVisiblePlayers()
                    ? "§aAgora você está vendo os jogadores."
                    : "§cAgora você não está mais vendo os jogadores.");

            user.getProfile().setCooldown("visible-players", TimeUnit.SECONDS.toMillis(8));
        }
    }

    protected void exit(Player player) {
        User user = userManager.read(player.getUniqueId());

        if (user != null) {
            user.getGame().leave(user);
            user.getArena().leaveMember(player);

            userManager.remove(player.getUniqueId());
        }
    }
}
