package br.com.poison.arcade.duels.command;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.entity.Player;

public class GoCommand implements CommandInheritor {

    @Command(name = "go", rank = RankCategory.TRIAL)
    public void go(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Client client = Duels.getClientManager().read(player.getUniqueId());

        Profile profile = client.getProfile();

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <jogador>.");
            return;
        }

        Client target = Duels.getClientManager().fetch(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (client.equals(target)) {
            profile.sendMessage("§cVocê não pode ir até você mesmo.");
            return;
        }

        if (!target.isValidRoute() || target.getArena() == null) {
            profile.sendMessage("§cO jogador não está jogando no momento.");
            return;
        }

        Duels.getGameManager().sendArena(client, target.getArena(), ClientStatsType.VANISH);

        player.teleport(target.getProfile().player());
    }
}
