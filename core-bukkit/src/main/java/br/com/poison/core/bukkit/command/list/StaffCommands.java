package br.com.poison.core.bukkit.command.list;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.bukkit.inventory.report.ReportsInventory;
import br.com.poison.core.bukkit.manager.PlayerManager;
import br.com.poison.core.bukkit.manager.VanishManager;
import br.com.poison.core.bukkit.service.server.options.ServerOptions;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.preference.Preference;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.profile.resources.route.Route;
import org.bukkit.*;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

public class StaffCommands implements CommandInheritor {

    @Command(name = "reports", aliases = {"rps"}, rank = RankCategory.TRIAL)
    public void reports(BukkitCommandContext context) {
        new ReportsInventory(context.getPlayer()).init();
    }

    @Command(name = "go", rank = RankCategory.TRIAL)
    public void go(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <player>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null || !target.isOnline()) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (target.equals(profile)) {
            profile.sendMessage("§cVocê não pode ir até você mesmo.");
            return;
        }

        // Se não for nulo, jogador está no mesmo servidor.
        if (profile.inServer(target.getServer())) {
            player.teleport(target.player());
        } else {
            // Se for nulo, jogador não está no mesmo servidor e enviar o redirect.

            Route route = target.getRoute();

            profile.sendMessage("§aEnviando...");

            if (route.hasDefinedGame()) {
                GameRouteContext game = route.getGame();

                try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
                    jedis.setex(Constant.TELEPORT_KEY + profile.getId(), 5, target.getId().toString());
                }

                profile.redirect(GameRouteContext.builder()
                        .arcade(game.getArcade())
                        .slot(game.getSlot())
                        .input(InputMode.VANISHER)
                        .id(game.getId())
                        .map(game.getMap())
                        .build());
            } else
                profile.redirect(route.getServer());
        }
    }

    @Command(name = "teleport", aliases = {"tp"}, rank = RankCategory.PLUS_MEDIA)
    public void teleport(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " <jogador> <X> <Y> <Z>.");
            return;
        }

        /* Realizando teleporte até outro jogador */
        if (args.length == 1) {
            Player target = context.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(TARGET_NOT_FOUND);
                return;
            }

            if (player.equals(target)) {
                player.sendMessage("§cVocê não pode teleportar para si mesmo.");
                return;
            }
            player.teleport(target);

            player.sendMessage("§eVocê foi teleportado para o jogador §b" + target.getName() + "§e.");

            log(context.getSender(), player.getName() + " teleportou-se até o jogador " + target.getName());

            return;
        }

        /* Realizando teleporte de um jogador para outro jogador. */
        if (args.length == 2) {
            Player first = context.getPlayer(args[0]), secondary = context.getPlayer(args[1]);

            if (first == null || secondary == null) {
                player.sendMessage(TARGET_NOT_FOUND);
                return;
            }

            if (first.equals(secondary) && secondary.equals(first)) {
                player.sendMessage("§cVocê não pode teleportar este jogador para ele mesmo.");
                return;
            }

            first.teleport(secondary);

            player.sendMessage("§eVocê teleportou o jogador §b" + first.getName() + "§e até o §b" + secondary.getName() + "§e.");

            log(context.getSender(), player.getName() + " teleportou " + first.getName() + " até o jogador " + secondary.getName());
            return;
        }

        /* Realizando teleporte para localização específica. */
        if (args.length == 3) {
            double pos_X = Double.parseDouble(args[0]), pos_Y = Double.parseDouble(args[1]), pos_Z = Double.parseDouble(args[2]);

            Location location = new Location(player.getWorld(), pos_X, pos_Y, pos_Z);

            player.teleport(location);

            player.sendMessage(String.format("§eVocê foi teleportado para a localização (§bX: §7%s, §bY: §7%s, §bZ: §7%s§e)",
                    pos_X, pos_Y, pos_Z));

            log(context.getSender(), String.format(player.getName() + " teleportou-se até a localização (X: %s, Y: %s, Z: %s)",
                    pos_X, pos_Y, pos_Z));
        }
    }

    @Command(name = "tpall", aliases = {"teleportall"}, rank = RankCategory.OWNER)
    public void teleportAll(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        player.sendMessage("§aTeleportando...");

        if (args.length == 0) {
            PlayerManager.handleTeleport(player.getLocation());

            log(context.getSender(), player.getName() + " teleportou todos os jogadores do servidor.");
            return;
        }

        if (args.length == 3) {
            double pos_X = Double.parseDouble(args[0]), pos_Y = Double.parseDouble(args[1]), pos_Z = Double.parseDouble(args[2]);

            Location location = new Location(player.getWorld(), pos_X, pos_Y, pos_Z);

            PlayerManager.handleTeleport(location);

            log(context.getSender(), String.format(player.getName() + " teleportou todos os jogadores do servidor ate (X: %s, Y: %s, Z: %s)",
                    pos_X, pos_Y, pos_Z));
        }
    }

    @Command(name = "vanish", aliases = {"v", "admin"}, rank = RankCategory.TRIAL)
    public void vanish(BukkitCommandContext context) {
        VanishManager.vanish(context.getProfile());
    }

    @Command(name = "invsee", aliases = {"inventory"}, rank = RankCategory.TRIAL)
    public void invSee(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " <player>.");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage("§cVocê não pode ver o seu o próprio inventário.");
            return;
        }

        player.openInventory(target.getInventory());

        log(context.getSender(), player.getName() + " abriu o inventário de " + target.getName());
    }

    @Command(name = "gamemode", aliases = {"gm"}, rank = RankCategory.PLUS_MEDIA)
    public void gameMode(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " <modo>");
            return;
        }

        GameMode gameMode = Arrays.stream(GameMode.values())
                .filter(mode -> mode.name().equalsIgnoreCase(args[0]))
                .findFirst().orElse(null);

        if (gameMode == null) {
            gameMode = GameMode.getByValue(Integer.parseInt(args[0]));

            if (gameMode == null) {
                player.sendMessage(" §cO modo de jogo \"" + args[0] + "\" não foi encontrado!");
                return;
            }
        }

        String name = gameMode.equals(GameMode.SURVIVAL) ? "Sobrevivência"
                : gameMode.equals(GameMode.ADVENTURE) ? "Aventura"
                : gameMode.equals(GameMode.SPECTATOR) ? "Espectador" : "Criativo";

        if (player.inGameMode(gameMode)) {
            player.sendMessage("§cVocê já está no modo de jogo " + name + ".");
            return;
        }

        if (args.length == 1) {
            player.setGameMode(gameMode);

            player.sendMessage("§aVocê entrou no modo de jogo §e" + name + "§a.");

            log(context.getSender(), player.getName() + " entrou no modo de jogo " + name.toUpperCase());
            return;
        }

        Player target = context.getPlayer(args[1]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (target.inGameMode(gameMode)) {
            player.sendMessage("§cO jogador " + target.getName() + " já está no modo de jogo " + name + ".");
            return;
        }

        target.setGameMode(gameMode);

        target.sendMessage("§aO jogador §e" + player.getName() + "§a mudou o seu modo de jogo para §e" + name + "§a.");
        player.sendMessage("§aVocê mudou o modo de jogo do jogador §e" + target.getName() + "§a para §e" + name + "§a.");

        log(context.getSender(), player.getName() + " mudou o modo de jogo do " + target.getName() + " para " + name.toUpperCase());
    }

    @Command(name = "build", rank = RankCategory.BUILDER)
    public void build(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        Preference preference = profile.getPreference();

        preference.setBuildMode(!preference.isBuildMode());
        profile.savePreference(preference);

        profile.sendMessage(preference.isBuildMode() ? "§aO seu modo de construção foi ativado." : "§cO seu modo de construção foi desativado.");
    }

    @Command(name = "chat", rank = RankCategory.MODPLUS, onlyPlayer = false)
    public void chatCommand(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        sender.sendMessage("§cUso: /chat <on/off/clear>");
    }

    @Command(name = "chat.on", rank = RankCategory.MODPLUS, onlyPlayer = false)
    public void chatOnCommand(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        if (ServerOptions.CHAT_ENABLED) {
            sender.sendMessage("§cO chat do servidor já está ativado!");
            return;
        }

        ServerOptions.CHAT_ENABLED = true;
    }

    @Command(name = "chat.off", rank = RankCategory.MODPLUS, onlyPlayer = false)
    public void chatOffCommand(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        if (!ServerOptions.CHAT_ENABLED) {
            sender.sendMessage("§cO chat do servidor já está desativado!");
            return;
        }

        ServerOptions.CHAT_ENABLED = false;
    }

    @Command(name = "chat.clear", aliases = {"cc"}, rank = RankCategory.MODPLUS, onlyPlayer = false)
    public void chatClearCommand(BukkitCommandContext context) {
        for (int i = 0; i < 100; i++)
            broadcast(" ");

        broadcast("§6O chat foi limpo!");
    }
}
