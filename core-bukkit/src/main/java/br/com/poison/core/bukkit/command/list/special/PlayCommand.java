package br.com.poison.core.bukkit.command.list.special;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.server.Server;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import redis.clients.jedis.Jedis;

public class PlayCommand implements CommandInheritor {

    @Command(name = "play", aliases = {"playagain", "jogar"})
    public void playAgain(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <modo>.");
            return;
        }

        ServerCategory server = ServerCategory.fetch(args[0]);

        if (server == null || server.isAllowArcade()) {
            profile.sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        profile.redirect(server);
    }

    @Command(name = "lobby", aliases = {"l", "hub"})
    public void lobby(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        Server server = Core.getServerManager().getServer(profile.getServer());

        if (server != null && server.isArcade()) {
            try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
                jedis.setex(Constant.ROUTE_KEY + profile.getId(), 5, server.getName());
            }
        }

        profile.redirect(ServerCategory.LOBBY);
    }
}
