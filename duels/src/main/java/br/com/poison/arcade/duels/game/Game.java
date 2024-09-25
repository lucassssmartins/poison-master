package br.com.poison.arcade.duels.game;

import br.com.poison.arcade.duels.Duels;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.grinderwolf.swm.plugin.SWMPlugin;
import br.com.poison.arcade.duels.arena.Arena;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.ClientData;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.arcade.duels.manager.ClientManager;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.Room;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.map.SignedLocation;
import br.com.poison.core.arcade.room.map.area.Cuboid;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.arcade.team.Team;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.bukkit.listener.loader.ignore.IgnoreEvent;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

@Getter
@Setter
@IgnoreEvent
public abstract class Game extends ArcadeGame<Duels> implements Listener {

    private final File mapSource;

    private final ClientManager client;

    private Arena.TeamInfoSidebar info;

    public Game(Duels instance, Integer minRooms, Integer maxRooms, String mapsDirectory, ArcadeCategory category) {
        super(instance, minRooms, maxRooms, mapsDirectory, category);

        this.client = Duels.getClientManager();

        this.mapSource = new File(instance.getDataFolder().getAbsolutePath() + "/maps");
    }

    /* Métodos Abstratos */
    public abstract void loadSidebar(Client client);

    public abstract void updateSidebar(Client client);

    public void updateTeamStatsAfterEnd(Team winner, List<Team> losers) {
        /* Setando XP */
        for (Player player : winner.getPlayers()) {
            DuelMember member = Core.getDuelManager().read(player.getUniqueId());

            if (member == null) continue;

            int xp = Core.RANDOM.ints(35, 50).findFirst().orElse(35);

            member.addXp(xp);

            player.sendMessage("§a+" + xp + " XP's");
        }

        for (Team loser : losers) {
            for (Player player : loser.getPlayers()) {
                DuelMember member = Core.getDuelManager().read(player.getUniqueId());

                if (member == null) continue;

                int xp = Core.RANDOM.ints(13, 25).findFirst().orElse(13);

                member.removeXp(xp);

                player.sendMessage("§c-" + xp + " XP's");
            }
        }
    }

    public abstract void loadInventory(Player player);

    public void loadDefaultInventory(Player player) {
        Client client = getClient().read(player.getUniqueId());

        PlayerInventory inv = player.getInventory();

        if (client.isProtected()) {
            inv.clear();
            inv.setArmorContents(null);

            if (client.getArena().hasCondition(RoomCondition.ENDING))
                inv.setItem(0, new Item(Material.PAPER)
                        .name("§aJogar Novamente")
                        .interact(event -> {
                            Client target = getClient().read(event.getPlayer().getUniqueId());

                            if (target != null)
                                event.getPlayer().performCommand("playagain " + target.getGame().getName());
                        }));

            inv.setItem(8, new Item(Material.DARK_OAK_DOOR_ITEM)
                    .name("§cRetornar ao Lobby")
                    .interact(event -> event.getPlayer().performCommand("lobby")));
        }
    }

    public enum DeathCause {NORMAL, VOID}

    public void sendDeathMessage(Arena arena, Client client, DeathCause cause) {
        ClientData data = client.getData();

        Client target = getClient().read(data.getLastHit());

        if (data.hasLastHit())
            data.getLastHitPlayer().playSound(Sound.SUCCESSFUL_HIT);

        arena.sendMessage(client.getTeamColor() + client.getProfile().getName() + " §7" + (cause.equals(DeathCause.VOID)
                ? (data.hasLastHit() ? "foi jogado no void por " + target.getTeamColor() + target.getProfile().getName() : "caiu no void")
                : "morreu" + (data.hasLastHit() ? " para " + target.getTeamColor() + target.getProfile().getName() : "")) + "§7.");
    }

    public void handleDeath(Client client, Arena arena, DeathCause cause) {
        Player player = client.getProfile().player();

        Game game = (Game) arena.getGame();

        ClientData data = client.getData();

        Team team = client.getTeam();

        // Restaurar vida
        if (player.getHealth() < player.getMaxHealth())
            player.setHealth(player.getMaxHealth());

        if (!cause.equals(DeathCause.VOID)) {
            Vector pushDirection = player.getLocation().getDirection().multiply(-1);

            // Define a intensidade do empurrão
            double pushStrength = 0.5;

            // Aplica o empurrão usando setVelocity
            player.setVelocity(pushDirection.multiply(pushStrength));
        }

        // Se a partida for 1v1, executar verificações para 1v1.
        if (arena.hasSlot(SlotCategory.SOLO)) {

            if (!isWonInVoid()) {
                if (isAllowedResurface()) {
                    client.resurface();

                    sendDeathMessage(arena, client, cause);
                } else {
                    game.loadInventory(player);

                    player.teleport(team.getLocation());

                    sendDeathMessage(arena, client, cause);
                }
                return;
            }

            if (isScoredGame()) {
                Team winner = arena.getTeams(search -> !search.equals(team)).stream().findFirst().orElse(null);

                if (winner != null) {
                    winner.setScore(winner.getScore() + 1);

                    arena.sendMessage(winner.getColoredName() + "§e marcou §6+1§e ponto!");

                    if (winner.isReachedMaxScore())
                        arena.onEnd(winner);
                    else
                        arena.onStarting();
                }

                return;
            }

            /* Caso a partida seja padronizada, só fazer a vitória para o outro time */
            if (data.isStats(ClientStatsType.PLAYER)) {
                sendDeathMessage(arena, client, cause);

                arena.getTeams(search -> !search.equals(team)).stream().findFirst().ifPresent(arena::onEnd);
            }

        } else {
            // Aqui a partida pode ser 2v2, 3v3, 4v4...

            /* Executando verificação para jogos de pontuação. */
            if (isScoredGame()) {
                Team winner = arena.getTeams(search -> !search.equals(team)).stream().findFirst().orElse(null);

                if (winner != null) {
                    // Se todos os jogadores do time perdedor estiverem mortos, aplica a pontuação.
                    if (getAliveClients(team) <= 0) {
                        winner.setScore(winner.getScore() + 1);

                        winner.getPlayers().forEach(target -> target.playSound(Sound.LEVEL_UP));

                        arena.sendMessage("§eO time " + winner.getColoredName() + "§e marcou §6+1§e ponto!");
                    } else {
                        // Caso tenha algum jogador vivo, aplicar a morte para ele

                        client.died();

                        // Caso os jogadores mortos do time seja 0, aplicar a pontuação
                        if (getAliveClients(team) == 0) {
                            winner.setScore(winner.getScore() + 1);

                            winner.getPlayers().forEach(target -> target.playSound(Sound.LEVEL_UP));

                            arena.sendMessage("§eO time " + winner.getColoredName() + "§e marcou §6+1§e ponto!");
                        }
                    }

                    if (winner.isReachedMaxScore())
                        arena.onEnd(winner);
                    else
                        arena.onStarting();
                }
            }

            if (isScoredGame()) {
                arena.getTeams(search -> !search.equals(team)).stream()
                        .findFirst()
                        .ifPresent(winner -> applyScoreLogic(client, arena, cause, winner, team));
            }

            /* Executando verificação para jogos padrões (Sem cama / Pontuação) */
            if (data.isStats(ClientStatsType.PLAYER)) {
                if (getAliveClients(team) > 1) {
                    client.died();

                    sendDeathMessage(arena, client, cause);
                } else
                    arena.getTeams(winner -> !winner.equals(team)).stream().findFirst().ifPresent(arena::onEnd);
            }
        }
    }

    protected void applyScoreLogic(Client client, Arena arena, DeathCause cause, Team winner, Team loser) {
        if (getAliveClients(loser) > 1) {
            client.died();

            sendDeathMessage(arena, client, cause);
            return;
        }

        winner.setScore(winner.getScore() + 1);
        winner.getPlayers().forEach(player -> player.playSound(Sound.SUCCESSFUL_HIT));

        arena.sendMessage("§eO time " + winner.getColoredName() + "§e marcou §6+1§e ponto!");

        if (winner.isReachedMaxScore())
            arena.onEnd(winner);
        else
            arena.onStarting();
    }

    /* Fim dos métodos abstratos */

    @Override
    public boolean load() {
        getInstance().getLogger().info("[" + getName() + "] Iniciando jogo...");

        if (!loadMaps()) return false;

        int mapId = 0;

        for (int i = 0; i < getMinArenas(); i++) {
            if (mapId == getMaps().size())
                mapId = 0;

            Map map = getMap(mapId);
            mapId++;

            if (loadArena(map, SlotCategory.SOLO) == null) return false;
        }

        getInstance().getLogger().info("[" + getName() + "] Jogo iniciado com sucesso.");

        return true;
    }

    @Override
    public void unload() {
        getInstance().getLogger().info("[" + getName() + "] Removendo arenas...");

        getInstance().getLogger().info("[" + getName() + "] Jogo encerrado com sucesso!");
    }

    @Override
    public boolean loadMaps() {
        File source = new File(mapSource, getName());

        if (!source.isDirectory()) return false;

        File[] maps = source.listFiles();

        if (maps == null || maps.length == 0)
            return false;

        for (int index = 0; index < maps.length; index++) {
            File consumer = maps[index];

            int mapId = index + 1;

            getInstance().getLogger().info("[" + getName() + "/" + consumer.getName() + "/" + mapId + "] Iniciando mapa...");

            try {
                File json = new File(consumer, "config.json");

                if (!json.exists()) {
                    getInstance().getLogger().info("[" + getName() + "/" + consumer.getName() + "/" + mapId + "] Arquivo de configuração não encontrado!");
                    continue;
                }

                /* Recolhendo e aplicando dados do Mapa */
                JsonObject data = Core.PARSER.parse(new FileReader(json)).getAsJsonObject();

                String mapName = data.get("name").getAsString();
                int buildLimit = data.get("build_limit").getAsInt();

                Map map = new Map(index, mapName, getCategory(), consumer, data, buildLimit);

                // Pegando localizações do mapa
                JsonArray locations = data.get("locations").getAsJsonArray();

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

                /* Dados recolhidos e aplicados */

                getInstance().getLogger().info("[" + getName() + "/" + consumer.getName() + "/" + mapId + "] Mapa carregado com sucesso!");
            } catch (Exception e) {
                getInstance().getLogger().log(Level.WARNING, "Não foi possível carregar o mapa " + consumer.getName(), e);
            }
        }

        return true;
    }

    public boolean isRequiredCreateArenas() {
        int totalArenas = getArenas().size(),
                occupiedArenas = (int) getArenas().stream().filter(room -> !room.isAvailable()).count();

        if (occupiedArenas >= (totalArenas / 2)) {
            for (int i = 0; i < 3; i++) {
                Map map = getMap(Core.RANDOM.nextInt(getMaps().size()));

                if (map != null)
                    loadArena(map, SlotCategory.SOLO);
            }

            Core.getLogger().info("[SlimeWorld] Salas geradas com sucesso para " + getName() + "!");

            return true;
        }

        return false;
    }

    @Override
    public Arena loadArena(Map map, SlotCategory slot) {
        Instant startTime = Instant.now();

        // Gerando ID da sala
        int id = (getIdCreator().getAndIncrement() + 1);

        getInstance().getLogger().info("[" + getName() + "/" + id + "] Tentando iniciar arena...");

        String templateName = getName().toLowerCase() + "-" + map.getName().toLowerCase(),
                worldName = templateName + "-" + id;

        Arena arena = new Arena(id, this, map, slot);

        SlimeWorldAPI.cloneWorldFromTemplate(SWMPlugin.getInstance(),
                "file",
                templateName,
                worldName, () -> arena.setTeams(
                        Team.builder()
                                .name("Vermelho")
                                .tag("V")
                                .color(ChatColor.RED)
                                .holder(this)
                                .location(arena.getLocation("vermelho"))
                                .maxPlayers(slot.getTotalPlayers() / 2)
                                .build(),

                        Team.builder()
                                .name("Azul")
                                .tag("A")
                                .color(ChatColor.BLUE)
                                .holder(this)
                                .location(arena.getLocation("azul"))
                                .maxPlayers(slot.getTotalPlayers() / 2)
                                .build()));

        getArenas().add(arena);

        getInstance().getLogger().info("[" + getName() + "/" + id + "] Arena iniciada com sucesso! " +
                "(Tempo de resposta: " + Duration.between(startTime, Instant.now()).toMillis() + "ms)");

        return arena;
    }

    public void unloadArena(Room room) {
        room.getPlayers().clear();
        room.getTeams().clear();

        getArenas().remove(room);
    }

    public void copyArena(Arena source) {
        Instant startTime = Instant.now();

        int id = source.getId();

        Map map = source.getMap();
        SlotCategory slot = source.getSlot();

        Arena arena = new Arena(id, this, map, slot);

        // Copiando sala
        getInstance().getLogger().info("[" + getName() + "/" + id + "] Tentando copiar arena...");

        String templateName = getName().toLowerCase() + "-" + map.getName().toLowerCase(),
                worldName = templateName + "-" + id;

        SlimeWorldAPI.cloneWorldFromTemplate(SWMPlugin.getInstance(),
                "file",
                templateName,
                worldName, () -> arena.setTeams(
                        Team.builder()
                                .name("Vermelho")
                                .tag("V")
                                .color(ChatColor.RED)
                                .holder(this)
                                .location(arena.getLocation("vermelho"))
                                .maxPlayers(slot.getTotalPlayers() / 2)
                                .build(),

                        Team.builder()
                                .name("Azul")
                                .tag("A")
                                .color(ChatColor.BLUE)
                                .holder(this)
                                .location(arena.getLocation("azul"))
                                .maxPlayers(slot.getTotalPlayers() / 2)
                                .build()));

        getArenas().add(arena);

        getInstance().getLogger().info("[" + getName() + "/" + id + "] Arena copiada com sucesso! "
                + "(Tempo médio: " + Duration.between(startTime, Instant.now()).toMillis() + "ms)");
    }

    public boolean inGame(Client client) {
        return client.getGame().isCategory(getCategory());
    }

    public boolean inGameAndCondition(Client client, RoomCondition condition) {
        return inGame(client) && client.getArena().hasCondition(condition);
    }

    public int getAliveClients(Team team) {
        return (int) getClient().documents(client -> client.getTeam() != null && client.getTeam().equals(team) && client.isPlayer()).count();
    }
}
