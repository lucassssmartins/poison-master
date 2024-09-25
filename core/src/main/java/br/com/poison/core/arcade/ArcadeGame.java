package br.com.poison.core.arcade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.map.SignedLocation;
import br.com.poison.core.arcade.room.map.synthetic.SyntheticLocation;
import br.com.poison.core.arcade.room.Room;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class ArcadeGame<T> {

    private transient final AtomicInteger idCreator = new AtomicInteger();

    public static final String GAME_KEY = "arcade-playing-now:%s:%s";

    private final T instance;

    private final String name = getClass().getSimpleName();

    private final Integer minArenas, maxArenas;

    private final String mapsDirectory;
    private final ArcadeCategory category;

    private final Set<UUID> players = new HashSet<>();
    private final Set<Room> arenas = new HashSet<>();

    private final List<Map> maps = new ArrayList<>();

    private int maxScore = -1;

    private boolean active = true,
            wonInVoid = true,

           /* Allow Properties */
            allowedBuild = false,
            allowedDamage = true,
            allowedDrops = true,
            allowedSidebarUpdater = true,
            allowedMoveOnStarting = true,
            allowedDamageByFall = false,
            allowedResurface = true,
            allowedTeleportOnStarting = true,
            allowedTakeLife = true,
            allowedDamageItem = true,
            allowedRefreshInventory = true;

    public abstract boolean load();

    public abstract void unload();

    public abstract boolean loadMaps();

    public abstract Room loadArena(Map map, SlotCategory slot);

    public boolean isCategory(ArcadeCategory category) {
        return this.category.equals(category);
    }

    public Map getMap(int id) {
        return maps.stream().filter(map -> map.getId() == id).findFirst().orElse(null);
    }

    public Room getRoom(int id) {
        return arenas.stream().filter(room -> room.getId() == id).findFirst().orElse(null);
    }

    public SignedLocation getSignedLocation(JsonElement jsonElement) {
        JsonObject locationObject = jsonElement.getAsJsonObject();

        String locationName = locationObject.get("name").getAsString();

        double posX = locationObject.get("x").getAsDouble();
        double posY = locationObject.get("y").getAsDouble();
        double posZ = locationObject.get("z").getAsDouble();

        float yaw = locationObject.has("yaw") ? locationObject.get("yaw").getAsFloat() : 0;
        float pitch = locationObject.has("pitch") ? locationObject.get("pitch").getAsFloat() : 0;

        return new SignedLocation(locationName, new SyntheticLocation(posX, posY, posZ, yaw, pitch));
    }

    public boolean isScoredGame() {
        return maxScore > 0;
    }

    public int getPlayers() {
        return Core.getRedisDatabase().getTotalCount(String.format(GAME_KEY, category.getServer().getName().toLowerCase(), category.getName().toLowerCase()));
    }

    public void clearPlayers() {
        Core.getRedisDatabase().delete(String.format(GAME_KEY, category.getServer().getName().toLowerCase(), category.getName().toLowerCase()));

        players.clear();
    }

    public void addPlayer(UUID uuid) {
        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            Pipeline pipeline = jedis.pipelined();

            pipeline.sadd(String.format(GAME_KEY, category.getServer().getName().toLowerCase(), category.getName().toLowerCase()), uuid.toString());
            pipeline.sync();
        }
    }

    public void removePlayer(UUID uuid) {
        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            Pipeline pipeline = jedis.pipelined();

            pipeline.srem(String.format(GAME_KEY, category.getServer().getName().toLowerCase(), category.getName().toLowerCase()), uuid.toString());
            pipeline.sync();
        }
    }
}