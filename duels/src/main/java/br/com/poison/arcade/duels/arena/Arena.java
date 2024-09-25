package br.com.poison.arcade.duels.arena;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.game.Game;
import br.com.poison.arcade.duels.client.data.ClientData;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.room.Room;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.map.rollback.RollbackBlock;
import br.com.poison.core.arcade.room.member.RoomMember;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.arcade.team.Team;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Arena extends Room {

    public Arena(int id, ArcadeGame<Duels> game, Map map, SlotCategory slot) {
        super(id, game, map, slot);
    }

    @Override
    public void join(Player player) {
        Game game = (Game) getGame();

        Client client = game.getClient().read(player.getUniqueId());

        if (client == null) return;

        player.refresh();

        ClientData data = client.getData();

        joinMember(player, client.getRoute());
        hideAndShow(client);

        client.setTag(data.isStats(ClientStatsType.VANISH) ? Tag.LIGHT_PURPLE : data.isStats(ClientStatsType.SPECTATOR) ? Tag.YELLOW : Tag.MAGIC);

        if (client.isPlayer()) {
            /* Aplicando um time para o jogador */
            for (Team team : getTeams()) {
                if (team == null || team.isFull()) continue;

                client.setTeam(team);

                team.getPlayers().add(player);
                break;
            }

            game.loadInventory(player);

            sendMessage(client.getProfile().getTag().getColoredMagic() + player.getName() + "§e entrou na sala. (§b" + getPlayers().size() + "§e/§b" + getMaxPlayers() + "§e)");
        } else {
            player.setGameMode(GameMode.SURVIVAL);

            player.setAllowFlight(true);
            player.setFlying(true);

            player.sendMessage("§cVocê entrou na partida no modo " + client.getData().getStats().getType().getColoredName() + "§c!");
        }

        Profile profile = client.getProfile();

        game.loadSidebar(client);
        game.addPlayer(player.getUniqueId());

        profile.setGame(client.getRoute());

        Location spawn = getLocation("spawn");

        if (spawn != null)
            player.teleport(spawn);
    }

    public void leave(Player player) {
        Game game = (Game) getGame();

        Client client = game.getClient().read(player.getUniqueId());

        if (client != null && client.isPlayer()) {
            Team team = client.getTeam();

            if (team == null) return;

            team.getPlayers().remove(player);

            if (!hasCondition(RoomCondition.ENDING))
                sendMessage(client.getColoredName() + "§e saiu da sala. (§b" + getPlayers().size() + "§e/§b" + getMaxPlayers() + "§e)");
        }

        leaveMember(player);
        game.removePlayer(player.getUniqueId());
    }

    public void updater() {
        if (hasCondition(RoomCondition.WAITING) && isFilledTeams())
            onStarting();

        if (hasCondition(RoomCondition.STARTING)) {
            /* A partida está iniciando e os times estão cheios. */
            if (isFilledTeams()) {
                if (getTimer() > 0)
                    setTimer(getTimer() - 1);

                if (getTimer() <= 5 && getTimer() >= 1) {
                    sendMessage("§eA partida começa em §6" + getTimer() + "s§e...");
                    sendTitle("§c§l" + getTimer(), "§ePrepare-se!");

                    sendSound(Sound.NOTE_PLING);
                }

                if (getTimer() == 0)
                    onStart();
            } else {
                /* Caso a partida ainda esteja iniciando, porém, faltam jogadores. */
                setCondition(RoomCondition.WAITING);

                getClients().forEach(client -> {
                    Player player = client.getProfile().player();

                    Game game = (Game) getGame();

                    game.loadSidebar(client);
                    game.loadInventory(player);

                    Location spawn = getLocation("spawn");

                    if (player != null && spawn != null)
                        player.teleport(spawn);
                });
            }
        }

        if (hasCondition(RoomCondition.PLAYING)) {
            setTimer(getTimer() + 1);

            // Se os times não estiverem cheios, e o outro time que não estiver cheio, também estiver vázio, vai cancelar a partida.
            if (!isFilledTeams() && !isTeamFilledAndAnotherNotAlone()) {
                sendMessage("§cNão há jogadores suficientes para continuar a partida. ;(");

                getTeams().stream().filter(team -> !team.getPlayers().isEmpty()).findFirst().ifPresent(this::onEnd);
            }

            if (getPlayers().isEmpty())
                onReset();

            if (getTimer() == 600)
                sendMessage("§4§lAVISO §eAos §615m§e de partida, a partida será dada como empate!");

            if (getTimer() == 900)
                onDraw();
        }

        if (hasCondition(RoomCondition.ENDING)) {
            if (getTimer() > 0)
                setTimer(getTimer() - 1);

            if (getPlayers().isEmpty() || getTimer() == 0)
                onReset();
        }
    }

    /* Métodos Condicionais da partida */
    public void onDraw() {
        sendMessage("§4§lAVISO §eA partida foi declarada como empate!");

        onEnd(null);
    }

    public void onReset() {
        getMembers().forEach(member -> member.getPlayer().performCommand("lobby"));

        Instant startTime = Instant.now();

        if (getMembers().isEmpty()) {
            Core.getLogger().info("[" + getGame().getName() + "/" + getId() + "] Reiniciando arena...");

            /* Partida com camas/ ou líquidos, é necessário realizar uma cópia nova da arena. */
            if (getGame().getCategory().isAllowedLiquid()) {
                Game game = (Game) getGame();

                SlimeWorldAPI.unloadWorld(getWorld(), () -> {
                    game.unloadArena(this);
                    game.copyArena(this);
                });
            } else {
                getTeams().forEach(Team::reset);

                if (getGame().isAllowedBuild())
                    onRollback();

                setCondition(RoomCondition.WAITING);

                Core.getLogger().info("[" + getGame().getName() + "/" + getId() + "] Arena reiniciada com sucesso! " +
                        "(Tempo médio: " + Duration.between(startTime, Instant.now()).toMillis() + "ms)");
            }
        }
    }

    public void onStarting() {
        setCondition(RoomCondition.STARTING);
        setTimer(6);

        for (Client client : getClients()) {
            if (client == null || !client.isValidRoute()) continue;

            Player player = client.getProfile().player();

            if (player == null) continue;

            player.setHealth(player.getMaxHealth());

            Game game = client.getGame();

            if (client.isPlayer()) {
                Location spawn = client.getTeam().getLocation();

                if (game.isAllowedRefreshInventory()) {
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                }

                game.loadSidebar(client);

                if (spawn != null && game.isAllowedTeleportOnStarting())
                    player.teleport(spawn);
            }
        }
    }

    public void onStart() {
        setCondition(RoomCondition.PLAYING);

        /* Definindo tags, carregando inventários e sidebars */
        for (Client client : getClients()) {
            if (client == null || !client.isValidRoute()) continue;

            Player player = client.getProfile().player();

            if (player == null) continue;

            Game game = client.getGame();
            Team team = client.getTeam();

            if (client.isPlayer()) {
                client.setTag(team.getColor().equals(ChatColor.RED) ? Tag.RED : Tag.BLUE);

                player.teleport(team.getLocation());

                game.loadInventory(player);

                player.setWalkSpeed(0.2F);
            }

            game.loadSidebar(client);
        }

        /* A partida foi iniciada corretamente */
        sendMessage("§aA partida iniciou! Boa sorte a todos ;)");
        sendSound(Sound.SUCCESSFUL_HIT);
    }

    public void onEnd(Team winner) {
        setCondition(RoomCondition.ENDING);
        setTimer(4);

        Game game = (Game) getGame();

        if (winner != null) {
            setWinner(winner);

            List<Team> losers = getTeams().stream().filter(team -> !team.equals(winner)).collect(Collectors.toList());

            game.updateTeamStatsAfterEnd(winner, losers);

            // Se for uma partida 1v1, enviar o nome do jogador ganhador.
            if (hasSlot(SlotCategory.SOLO))
                winner.getPlayers().stream().findFirst().ifPresent(player -> sendMessage(winner.getColor() + player.getName() + "§e ganhou."));
            else
                // Partida com slot superior a 1v1, enviar o time vencedor.
                sendMessage("§eO time " + winner.getColoredName() + "§e ganhou.");
        }

        for (Client client : getClients()) {
            if (client == null || !client.isValidRoute()) continue;

            Player player = client.getProfile().player();

            if (player == null) continue;

            if (client.isPlayer()) {
                player.setHealth(player.getMaxHealth());

                game.loadInventory(player);
            }

            game.loadSidebar(client);
        }
    }

    public void onRollback() {
        Game game = (Game) getGame();

        if (!getRollbackBlocks().isEmpty()) {
            try {
                MinecraftServer.getServer().postToMainThread(() -> {
                    for (RollbackBlock rollbackBlock : getRollbackBlocks()) {
                        if (rollbackBlock == null || rollbackBlock.getLocation() == null) continue;

                        Block block = rollbackBlock.getLocation().getBlock();

                        if (block != null)
                            block.setType(Material.AIR);
                    }
                });

                getRollbackBlocks().clear();
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível realizar o rollback...", e);
            }
        }
    }

    /* Iniciando Métodos Utilizáveis */
    public Set<Player> getSpectators() {
        return getMembers().stream()
                .filter(member -> !member.getRoute().getInput().equals(InputMode.PLAYER))
                .map(RoomMember::getPlayer)
                .collect(Collectors.toSet());
    }

    public Set<Client> getClients() {
        return Duels.getClientManager()
                .documents(client -> getMembers().stream().anyMatch(member -> client.getProfile().getId().equals(member.getPlayer().getUniqueId())))
                .collect(Collectors.toSet());
    }

    public void sendMessage(String... message) {
        getPlayers().forEach(player -> player.sendMessage(message));
    }

    public void sendSound(Sound sound) {
        getPlayers().forEach(player -> player.playSound(sound));
    }

    public void sendTitle(String title) {
        sendTitle(title, "");
    }

    public void sendTitle(String title, String subTitle) {
        getPlayers().forEach(player -> player.sendTitle(title, subTitle));
    }

    public void hideAndShow(Client client) {
        Player player = client.getProfile().player();

        ClientData data = client.getData();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || player.equals(target)) continue;

            player.hidePlayer(target);
            target.hidePlayer(player);
        }

        for (RoomMember member : getMembers()) {
            if (member == null) continue;

            Player target = member.getPlayer();
            InputMode input = member.getRoute().getInput();

            if (data.isStats(ClientStatsType.VANISH)) {
                player.showPlayer(target);

                if (input.equals(InputMode.VANISHER))
                    target.showPlayer(player);
            }

            if (data.isStats(ClientStatsType.SPECTATOR) && !input.equals(InputMode.VANISHER)) {
                player.showPlayer(target);

                if (input.equals(InputMode.SPECTATOR))
                    target.showPlayer(player);
            }

            if (client.isPlayer()) {
                if (input.equals(InputMode.PLAYER))
                    player.showPlayer(target);

                target.showPlayer(player);
            }
        }
    }

    public void sendTeamInfoInSidebar(Sidebar sidebar, TeamInfoSidebar info) {
        Game game = (Game) getGame();

        for (Team team : getTeams()) {
            if (team == null) continue;

            String field = team.getName().toLowerCase();

            switch (info) {
                case POINTS: {
                    if (hasSlot(SlotCategory.SOLO))
                        team.getPlayers().stream()
                                .findFirst()
                                .ifPresent(player -> sidebar.addRow(field,
                                        team.getColor() + player.getName() + ": ",
                                        "§7" + team.getScore()));
                    else
                        sidebar.addRow(field, team.getColoredTag() + " §f" + team.getName() + ": ",
                                "§7" + team.getScore());

                    break;
                }

                case PING: {
                    team.getPlayers().stream()
                            .findFirst()
                            .ifPresent(player -> sidebar.addRow(field,
                                    team.getColor() + player.getName() + ": ",
                                    "§f" + Util.formatNumber(player.getPing()) + "ms"));
                    break;
                }
            }
        }

        sidebar.blankRow();
    }

    public void updateTeamInfoInSidebar(Sidebar sidebar, TeamInfoSidebar info) {
        Game game = (Game) getGame();

        for (Team team : getTeams()) {
            if (team == null) continue;

            String field = team.getName().toLowerCase();

            switch (info) {
                case POINTS: {
                    if (hasSlot(SlotCategory.SOLO))
                        team.getPlayers().stream()
                                .findFirst()
                                .ifPresent(player -> sidebar.updateRow(field, "§7" + team.getScore()));
                    else
                        sidebar.updateRow(field, "§7" + team.getScore());

                    break;
                }

                case PING: {
                    team.getPlayers().stream()
                            .findFirst()
                            .ifPresent(player -> sidebar.updateRow(field, "§f" + Util.formatNumber(player.getPing()) + "ms"));
                    break;
                }
            }
        }
    }

    public enum TeamInfoSidebar {POINTS, PING}

    /* Encerrando Métodos Utilizáveis */
}
