package br.com.poison.auth.command;

import br.com.poison.auth.user.User;
import br.com.poison.auth.Auth;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.server.category.ServerCategory;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandInheritor {

    @Command(name = "register", aliases = {"registrar", "cadastrar"})
    public void register(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = Auth.getUserManager().read(player.getUniqueId());
        Profile profile = user.getProfile();

        String[] args = context.getArgs();

        if (profile.getAuth().isRegistered()) {
            profile.sendMessage("§cVocê já está cadastrado.");
            return;
        }

        if (args.length <= 1) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <senha> <confirmar-senha>.");
            return;
        }

        String password = args[0];

        String confirmationPassword = args[1];

        if (!confirmationPassword.equalsIgnoreCase(password)) {
            profile.sendMessage("§cA senha informada não é igual a senha original, tente novamente.");
            return;
        }

        user.setAuthenticated(true);

        profile.setPassword(password);

        profile.sendMessage("§aCadastrado! Esperamos que você aproveite o nosso servidor ;)");
        profile.redirect(ServerCategory.LOBBY);
    }
}
