package br.com.poison.arcade.duels.command;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.server.category.ServerCategory;
import org.bukkit.entity.Player;

public class PlayAgainCommand implements CommandInheritor {

    @Command(name = "playagain")
    public void playAgain(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Client client = Duels.getClientManager().read(player.getUniqueId());

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " <modo>.");
            return;
        }

        ArcadeCategory arcade = ArcadeCategory.fetch(args[0]);

        if (arcade == null || !arcade.getServer().equals(ServerCategory.DUELS)) {
            player.sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        Duels.getGameManager().sendArena(client, arcade);
    }
}
