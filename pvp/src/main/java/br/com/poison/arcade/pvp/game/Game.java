package br.com.poison.arcade.pvp.game;

import br.com.poison.arcade.pvp.PvP;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grinderwolf.swm.plugin.SWMPlugin;
import br.com.poison.arcade.pvp.game.arena.Arena;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.map.SignedLocation;
import br.com.poison.core.arcade.room.map.area.Cuboid;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.util.Util;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;

public abstract class Game extends ArcadeGame<PvP> {

    private final File MAP_DIRECTORY;

    public Game(PvP instance, Integer minRooms, Integer maxRooms, String mapsDirectory, ArcadeCategory category) {
        super(instance, minRooms, maxRooms, mapsDirectory, category);

        MAP_DIRECTORY = new File(instance.getDataFolder().getAbsolutePath() + "/maps");
    }

    public abstract void sendKit(Player player);

    public abstract void sendSidebar(User user);

    public abstract void updateSidebar(User user);

    public abstract void onDeath(User user, User killer);

    public abstract void initEntities(Arena arena);

    @Override
    public boolean load() {
        Core.getLogger().info("Iniciando modo de jogo " + getName() + "...");

        if (!loadMaps()) return false;

        int mapId = 0;
        for (int i = 0; i < getMinArenas(); i++) {
            if (mapId == getMaps().size())
                mapId = 0;

            Map map = getMap(mapId);

            mapId++;

            if (loadArena(map, SlotCategory.VOID) == null) return false;
        }

        return true;
    }

    @Override
    public void unload() {
        Core.getLogger().info("Encerrando jogo " + getName() + "...");

        clearPlayers();

        getArenas().clear();
    }

    @Override
    public Arena loadArena(Map map, SlotCategory slot) {
        long start = System.currentTimeMillis();

        // Gerando ID da sala
        int id = (getIdCreator().getAndIncrement() + 1);

        Arena arena = new Arena(id, this, map, slot);

        getInstance().getLogger().info("[" + getName() + "/" + id + "] Tentando iniciar arena...");

        String templateName = getName(),
                worldName = templateName + "-" + map.getName() + "-" + id;

        SlimeWorldAPI.cloneWorldFromTemplate(SWMPlugin.getInstance(),
                "file",
                templateName,
                worldName, () -> initEntities(arena));

        getArenas().add(arena);

        getInstance().getLogger().info("[" + getName() + "/" + id + "] Arena iniciada com sucesso! " +
                "(Tempo de resposta: " + Util.formatMS(start) + "ms)");

        return arena;
    }

    public boolean loadMaps() {
        File mapDirectory = new File(MAP_DIRECTORY, getName());

        if (!mapDirectory.isDirectory())
            return false;

        File[] maps = mapDirectory.listFiles();

        if (maps == null || maps.length == 0)
            return false;

        for (int index = 0; index < maps.length; index++) {
            File mapFile = maps[index];

            Core.getLogger().info("[" + (index + 1) + "/" + maps.length + "] Iniciando mapa " + mapFile.getName() + "...");

            try {
                File config = new File(mapFile, "config.json");

                if (!config.exists()) {
                    Core.getLogger().info("[" + (index + 1) + "/" + maps.length + "] A configuração do mapa " + mapFile.getName() + " não foi encontrada!");
                    continue;
                }

                JsonObject json = Core.PARSER.parse(new FileReader(config)).getAsJsonObject();

                String name = json.get("name").getAsString();
                int buildLimit = json.get("build_limit").getAsInt();

                Map map = new Map(index, name, getCategory(), mapFile, json, buildLimit);

                JsonArray locations = json.get("locations").getAsJsonArray();

                for (JsonElement location : locations) {
                    SignedLocation signed = getSignedLocation(location);

                    if (signed != null)
                        map.getSignedLocations().add(signed);
                }

                SignedLocation pos_1 = map.getLocation("map_limit_pos1"),
                        pos_2 = map.getLocation("map_limit_pos2");

                if (pos_1 == null || pos_2 == null) continue;

                map.setArea(new Cuboid(pos_1.getLocation(), pos_2.getLocation()));

                getMaps().add(map);

                Core.getLogger().info("[" + (map.getId() + 1) + "/" + maps.length + "] Mapa " + map.getName() + " iniciado.");
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível carregar o mapa...", e);
                return false;
            }
        }

        return true;
    }
}
