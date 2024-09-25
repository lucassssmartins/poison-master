package br.com.poison.core.bukkit.command.list;

import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.profile.Profile;

public class VipCommands implements CommandInheritor {

    @Command(name = "spec", aliases = {"espectar", "assistir"})
    public void watch(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (!profile.hasRank(RankCategory.POISON)) {
            profile.sendMessage("§cVocê não pode espectar §6partidas§c ;(",
                    "§cAdquira o rank " + RankCategory.POISON.getPrefix() + "§c ou superior para espectar partidas§c!");
            return;
        }

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <jogador>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (profile.equals(target)) {
            profile.sendMessage("§cVocê não pode assistir a si mesmo.");
            return;
        }

        if (!target.inGame() || !target.inServer(ServerCategory.DUELS)) {
            profile.sendMessage("§cO jogador " + target.getName() + " não está jogando!");
            return;
        }

        GameRouteContext game = target.getGame(), route = GameRouteContext.builder()
                .arcade(game.getArcade())
                .map(game.getMap())
                .id(game.getId())
                .slot(game.getSlot())
                .input(InputMode.SPECTATOR)
                .build();

        profile.sendMessage("§aPreparando...");
        profile.redirect(route);
    }

    @Command(name = "fake", aliases = {"nick"}, rank = RankCategory.MEDIA, runAsync = true)
    public void fake(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        profile.sendMessage("§cEste comando está em manutenção!");
        /*
        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <nick>.");
            return;
        }

        String nickname = args[0];

        if (!Validator.isNickname(nickname) || Bukkit.getPlayer(nickname) != null || UUIDFetcher.request(nickname) != null) {
            profile.sendMessage("§cO nick informado é inválido!");
            return;
        }

        if (profile.isUsingFake(nickname)) {
            profile.sendMessage("§cVocê já está usando este nick!");
            return;
        }

        TagController.updateNickname(context.getPlayer(), nickname);

        profile.sendMessage("§aO seu nick foi atualizado com sucesso."); */
    }
}
