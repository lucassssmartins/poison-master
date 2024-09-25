package br.com.poison.lobby.game;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.game.arena.Arena;
import br.com.poison.lobby.manager.GameManager;
import br.com.poison.lobby.user.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grinderwolf.swm.plugin.SWMPlugin;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.map.SignedLocation;
import br.com.poison.core.arcade.room.map.area.Cuboid;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.bukkit.inventory.profile.ProfileInventory;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.util.Util;
import br.com.poison.lobby.inventory.tracker.server.ServerTrackerInventory;
import br.com.poison.lobby.inventory.hub.LobbiesInventory;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;

@Getter
public abstract class Game extends ArcadeGame<Lobby> {

    private final File MAP_DIRECTORY;

    private final GameManager gameManager;

    public Game(Lobby instance, Integer minRooms, Integer maxRooms, String mapsDirectory, ArcadeCategory category) {
        super(instance, minRooms, maxRooms, mapsDirectory, category);

        MAP_DIRECTORY = new File(instance.getDataFolder().getAbsolutePath() + "/maps");

        this.gameManager = Lobby.getGameManager();
    }

    public abstract boolean load(User user);

    public abstract void leave(User user);

    public abstract void sendSidebar(User user);

    public abstract void initEntities(Arena arena);

    public void sendHotbar(User user) {
        Profile profile = user.getProfile();

        Player player = profile.player();

        PlayerInventory inv = player.getInventory();

        inv.clear();
        inv.setArmorContents(null);

        inv.setItem(0, new Item(Material.COMPASS)
                .name("§aSelecionar Jogo")
                .interact(event -> new ServerTrackerInventory(event.getPlayer()).init()));

        inv.setItem(1, new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64(profile.getSkin().getTexture().getValue())
                .name("§aVer conta")
                .interact(event -> new ProfileInventory(event.getPlayer()).init())
                .lore("§7Veja as informações da sua conta."));

        inv.setItem(7, new Item(Material.INK_SACK, 1, (user.isVisiblePlayers() ? 10 : 8))
                .name("§fJogadores: " + (user.isVisiblePlayers() ? "§aON" : "§cOFF")));

        inv.setItem(8, new Item(Material.NETHER_STAR)
                .name("§aSelecionar lobby")
                .interact(event -> new LobbiesInventory(event.getPlayer()).init())
                .lore("§7Selecione um lobby para você jogar."));

        player.updateInventory();
    }

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
        File mapDirectory = new File(MAP_DIRECTORY, getName().toLowerCase());

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
