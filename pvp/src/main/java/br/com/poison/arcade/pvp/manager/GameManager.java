package br.com.poison.arcade.pvp.manager;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.game.arena.Arena;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.util.loader.ClassLoader;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class GameManager {

    private final PvP pvp;

    private final Set<Game> games;
    private final List<Class<?>> gamesList;

    public GameManager(PvP pvp) {
        this.pvp = pvp;

        this.games = new HashSet<>();
        this.gamesList = ClassLoader.getClassesForPackage(pvp, "br.com.poison.arcade.pvp.game.list");
    }

    public void load() {
        SlimeWorldAPI.removeWorldsFromLoader(Constant.SLIME_HIKARI_SQL_LOADER);

        FileConfiguration fileConfiguration = pvp.getConfig();
        ConfigurationSection section = fileConfiguration.getConfigurationSection("modes");

        for (String name : section.getKeys(false)) {
            try {
                int min_rooms = section.getInt(name + ".min"), max_rooms = section.getInt(name + ".max");

                String directory = section.getString(name + ".directory");

                Game game = (Game) fetch(name)
                        .getConstructor(PvP.class, Integer.class, Integer.class, String.class)
                        .newInstance(pvp, min_rooms, max_rooms, directory);

                game.load();

                getGames().add(game);
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível carregar o " + name, e);

                Bukkit.shutdown();
            }
        }
    }

    public void unload() {
        SlimeWorldAPI.removeWorldsFromLoader(Constant.SLIME_HIKARI_SQL_LOADER);

        games.forEach(Game::unload);
        games.clear();
    }

    public Arena getBestArena(Game game) {
        Arena arena = (Arena) game.getArenas().stream()
                .filter(room -> room.hasCondition(RoomCondition.PLAYING))
                .findFirst()
                .orElse(null);

        if (arena == null) {
            Core.getLogger().info("ARENA NOT FOUND!");
            return null;
        }

        return arena;
    }

    public void redirect(User user, Game game) {
        if (game == null) {
            user.getProfile().sendMessage("§cNenhuma sala encontrada!");
            return;
        }

        redirect(user, getBestArena(game));
    }

    public void redirect(User user, ArcadeCategory category) {
        redirect(user, getGame(category));
    }

    public void redirect(User user, Arena arena) {
        Profile profile = user.getProfile();

        if (arena == null) {
            profile.sendMessage("§cNenhuma sala encontrada!");
            return;
        }

        if (user.getArena().equals(arena)) {
            profile.sendMessage("§cVocê já está conectado na sala!");
            return;
        }

        Game game = (Game) arena.getGame();

        if (game == null) {
            profile.sendMessage("§cNenhum modo de jogo encontrado!");
            return;
        }

        Player player = profile.player();

        user.getArena().leaveMember(player);
        user.getGame().removePlayer(player.getUniqueId());

        user.setGame(game);
        user.setArena(arena);

        user.setRoute(GameRouteContext.builder()
                .arcade(game.getCategory())
                .id(arena.getId())
                .map(arena.getMap().getId())
                .input(InputMode.PLAYER)
                .slot(arena.getSlot())
                .build());

        arena.join(player);
    }


    public Class<?> fetch(String name) {
        return gamesList.stream().filter(game -> game.getSimpleName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Game getGame(String name) {
        return games.stream().filter(game -> game.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Game getGame(ArcadeCategory category) {
        return games.stream().filter(game -> game.getCategory().equals(category)).findFirst().orElse(null);
    }

    public int getPlayers(ArcadeCategory category) {
        Game game = getGame(category);

        return game != null ? game.getPlayers() : 0;
    }
}
