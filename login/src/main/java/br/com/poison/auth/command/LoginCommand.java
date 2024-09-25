package br.com.poison.auth.command;

import br.com.poison.auth.Auth;
import br.com.poison.auth.user.User;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.server.category.ServerCategory;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandInheritor {

    @Command(name = "login", aliases = {"logar", "autenticar"})
    public void login(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = Auth.getUserManager().read(player.getUniqueId());
        Profile profile = user.getProfile();

        String[] args = context.getArgs();

        if (user.isAuthenticated()) {
            profile.sendMessage("§cVocê já está autenticado.");
            return;
        }

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <senha>.");
            return;
        }

        String password = args[0];

        if (!profile.getAuth().checkPassword(password)) {
            if (user.getLoginAttempts() - 1 == 0) {
                player.kickPlayer("§cVocê excedeu o limite de tentativas, tente se autenticar novamente.");
                return;
            }

            user.setLoginAttempts(user.getLoginAttempts() - 1);

            profile.sendMessage("§cA sua senha está incorreta, tente novamente (" + user.getLoginAttempts() + "x).");
            return;
        }

        user.setAuthenticated(true);

        profile.sendMessage("§aAutenticado! Esperamos que você aproveite o nosso servidor ;)");

        profile.redirect(ServerCategory.LOBBY);
    }
}
