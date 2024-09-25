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
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.map.rollback.RollbackBlock;
import br.com.poison.core.arcade.team.Team;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.util.extra.TimeUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameListener implements Listener {

    private final GameManager gameManager;
    private final ClientManager clientManager;

    private final List<String> allowedCommands, staffAllowedCommands;

    public GameListener() {
        this.gameManager = Duels.getGameManager();
        this.clientManager = Duels.getClientManager();

        this.allowedCommands = Arrays.asList("l", "lobby", "hub", "desistir", "report", "playagain");

        this.staffAllowedCommands = new ArrayList<>(this.allowedCommands);
        this.staffAllowedCommands.addAll(Arrays.asList("cban", "ban", "mute", "gm", "gamemode", "teleport", "tp"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        if (profile == null) return;

        String commandName = event.getMessage().split(" ")[0].substring(1);

        List<String> allowedCommandList = profile.isStaffer() ? staffAllowedCommands : allowedCommands;

        if (!allowedCommandList.contains(commandName)) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode executar este comando em partida.");
        }
    }

    @EventHandler
    public void onArenaUpdateStats(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Arena arena : gameManager.getArenas()) {
                Game game = (Game) arena.getGame();

                /* Atualizando a arena de segundo em segundo */
                arena.updater();

                /* Buscar clientes e adaptar stats */
                arena.getClients().forEach(client -> {
                    Player player = client.getProfile().player();

                    ClientData data = client.getData();
                    ClientStats stats = data.getStats();

                    Sidebar sidebar = client.getSidebar();

                    // Atualizar times na sidebar
                    if (!arena.getGame().isCategory(ArcadeCategory.NODEBUFF))
                        arena.updateTeamInfoInSidebar(sidebar, game.getInfo());

                    // Atualizar tempo na sidebar
                    if (!arena.hasCondition(RoomCondition.WAITING))
                        sidebar.updateRow("time", (!arena.hasCondition(RoomCondition.PLAYING)
                                ? "§e" + arena.getTimer() + "s"
                                : "§7" + TimeUtil.time(arena.getTimer())));
                    else
                        // Atualizar jogadores
                        sidebar.updateRow("players", "§a" + arena.getPlayers().size() + "/" + arena.getMaxPlayers());

                    if (!client.isPlayer())
                        sidebar.updateRow("watch", "§e" + arena.getSpectators().size());

                    // Cliente está ressurgindo
                    if (data.isStats(ClientStatsType.RESURFACING)) {
                        stats.setDuration(stats.getDuration() - 1);

                        int duration = stats.getDuration() + 1;

                        player.sendMessage("§c" + duration + "s...");
                        player.sendTitle("§c§lVOCÊ MORREU!", "§eRenascendo em §c" + duration + "s§e!");

                        player.playSound(Sound.WOOD_CLICK);

                        // Ressurgir cliente
                        if (stats.hasExpired()) {
                            Team team = client.getTeam();

                            player.setGameMode(GameMode.SURVIVAL);

                            player.setAllowFlight(false);
                            player.setFlying(false);

                            data.setPlayer();
                            game.loadInventory(player);

                            player.playSound(Sound.BURP);
                            player.teleport(team.getLocation());
                        }
                    }
                });
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClientDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player entity = (Player) event.getEntity();

            Client client = clientManager.read(entity.getUniqueId());

            if (client == null || !client.isValidRoute()) return;

            Arena arena = client.getArena();
            Game game = client.getGame();

            if (!client.isPlayer() || !game.isAllowedDamage() || (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && !game.isAllowedDamageByFall())) {
                event.setCancelled(true);
            } else
                event.setCancelled(client.isProtected());

            /* Evento não foi cancelado e o dano final foi maior que a vida da entidade. */
            if (!event.isCancelled() && event.getFinalDamage() >= entity.getHealth()) {
                event.setDamage(0);

                game.handleDeath(client, arena, Game.DeathCause.NORMAL);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClientDamageClient(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity(), damager = (Player) event.getDamager();

            Client client = clientManager.read(player.getUniqueId()), damagerClient = clientManager.read(damager.getUniqueId());

            if (client == null || damagerClient == null) return;

            Game game = damagerClient.getGame();
            Arena arena = damagerClient.getArena();

            if (!damagerClient.isPlayer() || !client.isPlayer()) {
                event.setCancelled(true);
                return;
            }

            if (client.isProtected() || damagerClient.isProtected()) {
                event.setCancelled(true);
                return;
            }

            if (!client.isSameGame(damagerClient) || client.isSameTeam(damagerClient)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(!game.isAllowedDamage());

            if (!event.isCancelled()) {
                if (!game.isAllowedTakeLife())
                    event.setDamage(0);

                damagerClient.getData().setLastHit(player.getUniqueId());

                /* Caso o dano final for maior que a vida atual da entidade, ela morre. */
                if (event.getFinalDamage() >= player.getHealth()) {
                    event.setDamage(0);

                    game.handleDeath(client, arena, Game.DeathCause.NORMAL);
                }
            }
        }
    }

    @EventHandler
    public void onClientMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Client client = clientManager.read(player.getUniqueId());

        if (client == null || !client.isPlayer()) return;

        Arena arena = client.getArena();
        Game game = client.getGame();

        Location current = event.getTo(), spawn = arena.getLocation("spawn");

        if (spawn == null) return;

        /* Se o jogo não permitir se mover quando estiver iniciando, vai cancelar. */
        if (!game.isAllowedMoveOnStarting() && arena.hasCondition(RoomCondition.STARTING) && client.getTeam() != null) {

            if(event.getTo().getY() > event.getFrom().getY()) {
                player.teleport(client.getTeam().getLocation());
            }

            player.setWalkSpeed(0F);
            return;
        }

        /* VOID: Verificando se o Y da localização atual, é menor que o Y do Spawn - 10. Ou se é o jogo Sumo e o jogador está na água. */
        if (current.getY() <= spawn.getY() - 30) {
            if (arena.hasCondition(RoomCondition.PLAYING))
                game.handleDeath(client, arena, Game.DeathCause.VOID);
            else
                player.teleport(spawn);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();

        Client client = clientManager.read(player.getUniqueId());

        if (client == null) return;

        Arena arena = client.getArena();

        if (arena != null) {
            if (!arena.hasCondition(RoomCondition.PLAYING)) {
                player.sendMessage("§cVocê não pode enviar mensagens antes da partida iniciar.");
                return;
            }

            arena.sendMessage(client.getChatName() + event.getMessage());
        }
    }

    @EventHandler
    public void onRollbackPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Client client = clientManager.read(player.getUniqueId());

        if (client == null || client.isProtected() || !client.getGame().isAllowedBuild()) {
            event.setCancelled(true);
            return;
        }

        Arena arena = client.getArena();

        arena.loadRollback(event.getBlockPlaced().getLocation(), RollbackBlock.RollbackType.PLACE_BLOCK);
    }

    @EventHandler
    public void onRollbackBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Client client = clientManager.read(player.getUniqueId());

        if (client == null || client.isProtected() || !client.getGame().isAllowedBuild() || !client.isPlayer()) {
            event.setCancelled(true);
            return;
        }

        Arena arena = client.getArena();

        Block block = event.getBlock();

        if (arena.getGame().getCategory().isAllowedLiquid() && block.getType().equals(Material.COBBLESTONE)) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(!arena.isRollback(block));

        if (!event.isCancelled())
            arena.loadRollback(block.getLocation(), RollbackBlock.RollbackType.REMOVE_BLOCK);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        Client client = clientManager.read(player.getUniqueId());

        if (client == null || !client.isPlayer()) return;

        event.setCancelled(!(client.getArena().hasCondition(RoomCondition.PLAYING) && client.getGame().isAllowedDrops()));
    }

    @EventHandler
    public void onPlayerFood(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Client client = clientManager.read(event.getEntity().getUniqueId());

            if (client == null || !client.isPlayer() || !client.getArena().hasCondition(RoomCondition.PLAYING)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(!(client.getGame().isCategory(ArcadeCategory.SIMULATOR)));
        }
    }
}
