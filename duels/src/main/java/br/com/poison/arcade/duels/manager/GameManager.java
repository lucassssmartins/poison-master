package br.com.poison.arcade.duels.manager;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.arena.Arena;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.arcade.duels.game.Game;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.util.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class GameManager {

    private final Duels duels;

    private final Set<Game> games;
    private final List<Class<?>> gamesList;

    public GameManager(Duels duels) {
        this.duels = duels;

        this.games = new HashSet<>();
        this.gamesList = ClassLoader.getClassesForPackage(duels, "br.com.poison.arcade.duels.game.list");
    }

    public void handle() {
        SlimeWorldAPI.removeWorldsFromLoader(Constant.SLIME_HIKARI_SQL_LOADER);

        FileConfiguration config = duels.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Games");

        for (String name : section.getKeys(false)) {
            try {
                String directory = section.getString(name + ".directory");

                int min_rooms = section.getInt(name + ".min_arenas"), max_rooms = section.getInt(name + ".max_arenas");

                Game game = (Game) getGameClass(name)
                        .getConstructor(Duels.class, Integer.class, Integer.class, String.class)
                        .newInstance(duels, min_rooms, max_rooms, directory);

                game.clearPlayers();

                game.load();

                Bukkit.getPluginManager().registerEvents(game, duels);

                getGames().add(game);
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível carregar o jogo " + name, e);

                Bukkit.shutdown();
            }
        }
    }

    public void unload() {
        SlimeWorldAPI.removeWorldsFromLoader(Constant.SLIME_HIKARI_SQL_LOADER);

        games.forEach(Game::unload);
        games.clear();
    }

    public void sendArena(Client client, Arena arena, ClientStatsType type) {
        Player player = client.getProfile().player();

        if (!arena.getGame().isActive() && !client.getProfile().isStaffer()) {
            player.sendMessage("§cO modo de jogo solicitado não está ativo.");
            return;
        }

        GameRouteContext route = GameRouteContext.builder()
                .arcade(arena.getGame().getCategory())
                .id(arena.getId())
                .map(arena.getMap().getId())
                .slot(arena.getSlot())
                .input(type.equals(ClientStatsType.VANISH)
                        ? InputMode.VANISHER
                        : type.equals(ClientStatsType.SPECTATOR) ? InputMode.SPECTATOR
                        : InputMode.PLAYER)
                .build();

        client.getArena().leave(player);

        client.setRoute(route);

        client.setStats(type);
        client.setArena(arena);

        arena.join(player);
    }

    public void sendArena(Client client, ArcadeCategory arcade) {
        Player player = client.getProfile().player();

        Game game = getGame(arcade);

        if (game == null) {
            player.sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        if (!game.isActive() && !client.getProfile().isStaffer()) {
            player.sendMessage("§cO modo de jogo solicitado não está ativo.");
            return;
        }

        Arena current = client.getArena();

        Arena search = (Arena) game.getArenas().stream()
                .filter(room -> room.getId() != current.getId()
                        && room.getSlot() == current.getSlot() && room.isAvailable())
                .findFirst().orElse(null);

        if (search == null) {
            player.sendMessage("§cNenhuma partida foi encontrada.");
            return;
        }

        current.leave(player);

        client.setArena(search);
        client.setRoute(GameRouteContext.builder()
                .arcade(arcade)
                .slot(search.getSlot())
                .id(search.getId())
                .map(search.getMap().getId())
                .input(client.getRoute().getInput())
                .build());

        player.sendMessage("§aEnviando para " + search.getDiscriminator() + "...");

        search.join(player);
    }

    public Arena findBestArena(Game game, GameRouteContext route, UUID id) {
        return getArenas(game).stream()
                .filter(arena -> {
                    /* TODO: Executando busca pela melhor arena disponível. */

                    if (!game.isActive())
                        return false;

                    if (arena.getSlot() != route.getSlot())
                        return false;

                    if (route.getInput().equals(InputMode.PLAYER) && !arena.getPlayers().isEmpty() && arena.isAvailable())
                        return true;

                    if (arena.getId() == route.getId() &&
                            (route.getInput().equals(InputMode.VANISHER) || route.getInput().equals(InputMode.SPECTATOR)))
                        return true;

                    if (route.hasLink() && arena.isAvailable()) {
                        arena.getReservations().addAll(route.getLink());
                        return true;
                    }

                    if (id != null && arena.isReserved() && arena.hasReservation(id) && arena.isNotFull())
                        return true;

                    return arena.isAvailable();
                })
                .findFirst()
                .orElse(null);
    }

    public List<Arena> getArenas() {
        return games.stream()
                .flatMap(game -> game.getArenas().stream())
                .filter(room -> room instanceof Arena)
                .map(room -> (Arena) room)
                .collect(Collectors.toList());
    }

    public List<Arena> getArenas(Game game) {
        return game.getArenas().stream()
                .filter(room -> room instanceof Arena)
                .map(room -> (Arena) room)
                .collect(Collectors.toList());
    }

    public Game getGame(ArcadeCategory category) {
        return games.stream().filter(game -> game.getCategory().equals(category)).findFirst().orElse(null);
    }

    public Game getGame(String name) {
        return games.stream().filter(game -> game.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Class<?> getGameClass(String name) {
        return gamesList.stream().filter(game -> game.getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public int getPlayers(ArcadeCategory category) {
        Game game = getGame(category);

        return game != null ? game.getPlayers() : 0;
    }
}
