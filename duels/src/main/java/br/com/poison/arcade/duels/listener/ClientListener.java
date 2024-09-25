package br.com.poison.arcade.duels.listener;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.arena.Arena;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.ClientData;
import br.com.poison.arcade.duels.client.data.stats.ClientStats;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.arcade.duels.game.Game;
import br.com.poison.arcade.duels.manager.ClientManager;
import br.com.poison.arcade.duels.manager.GameManager;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.logging.Level;

public class ClientListener implements Listener {

    private final ClientManager clientManager;
    private final GameManager gameManager;

    public ClientListener() {
        this.clientManager = Duels.getClientManager();
        this.gameManager = Duels.getGameManager();
    }

    @EventHandler
    public void onSearchRoute(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        Profile profile = Core.getProfileManager().read(uuid);

        if (profile == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Constant.PROFILE_CREATE_FAILED);
            return;
        }

        /* Carregando os dados do Membro */
        DuelMember member = Core.getDuelData().fetch(uuid, true);

        if (member == null) {
            member = Core.getDuelData().input(profile);

            if (member == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Não foi possível carregar o seu perfíl! (Ex0001)");
                return;
            }
        }

        Core.getDuelData().persist(uuid);
        Core.getDuelManager().save(member);
        /* Dados carregados com sucesso */

        /* Buscando a rota atual do jogador */
        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            String data = jedis.get(Constant.ROUTE_KEY + uuid);

            if (data == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNão foi possível encontrar a sua rota!");
                return;
            }

            // Pegando a rota de jogo
            GameRouteContext context = Core.GSON.fromJson(data, GameRouteContext.class);

            if (context == null || context.getArcade() == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNão foi possível encontrar o modo de jogo!");
                return;
            }

            // Buscando o jogo solicitado
            Game game = gameManager.getGame(context.getArcade());

            if (game == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cO modo de jogo solicitado não foi encontrado.");
                return;
            }

            // Buscando arena para o jogo
            Arena arena = gameManager.findBestArena(game, context, uuid);

            if (arena == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cNão foi possível encontrar uma partida disponível!");
                return;
            }

            GameRouteContext route = GameRouteContext.builder()
                    .arcade(context.getArcade())
                    .id(arena.getId())
                    .map(arena.getMap().getId())
                    .slot(arena.getSlot())
                    .input(context.getInput())
                    .build();

            if (context.hasLink())
                route.setLink(context.getLink());

            InputMode input = route.getInput();

            ClientStatsType type = (input == null) ? ClientStatsType.PLAYER :
                    (input.equals(InputMode.VANISHER) ? ClientStatsType.VANISH :
                            input.equals(InputMode.SPECTATOR) ? ClientStatsType.SPECTATOR :
                                    ClientStatsType.PLAYER);

            Client client = new Client(profile, member, new ClientData(new ClientStats(type)));

            client.setRoute(route);
            client.setValidRoute(route.isValid());

            client.setArena(arena);

            clientManager.save(client);

            Core.getLogger().info("[" + game.getName() + "/" + arena.getId() + "] " + profile.getName() + " entrou na partida." +
                    " (" + (arena.getPlayers().size() + 1) + "/" + arena.getMaxPlayers() + ")");
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao solicitar a rota de \"" + uuid + "\"", e);
        }
    }

    @EventHandler
    public void onClientLogin(PlayerLoginEvent event) {
        if (event.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
            Player player = event.getPlayer();

            Client client = clientManager.read(player.getUniqueId());

            if (client == null || !client.isValidRoute())
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cNão foi possível carregar o seu perfil! (Ex0002)");
        } else
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cA sua conexão não foi bem sucedida.");
    }

    @EventHandler
    public void onClientJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Client client = clientManager.read(player.getUniqueId());

        if (client != null) {
            client.load(player);

            Game game = client.getGame();

            if (game != null && game.isRequiredCreateArenas())
                Core.getLogger().info("[SlimeWorld] Gerando mais 3 salas para " + game.getName() + "...");
        }
    }

    @EventHandler
    public void onClientQuit(PlayerQuitEvent event) {
        handleExit(event.getPlayer());
    }

    @EventHandler
    public void onClientKick(PlayerKickEvent event) {
        handleExit(event.getPlayer());
    }

    protected void handleExit(Player player) {
        Client client = clientManager.read(player.getUniqueId());

        if (client != null) {
            client.getArena().leave(player);

            clientManager.remove(player.getUniqueId());
        }

        Core.getDuelData().cache(player.getUniqueId());
    }
}
