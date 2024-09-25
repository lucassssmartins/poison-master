package br.com.poison.arcade.pvp.command;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.bukkit.manager.VanishManager;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;

public class GoCommand implements CommandInheritor {

    @Command(name = "go", rank = RankCategory.TRIAL)
    public void go(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        User user = PvP.getUserManager().read(profile.getId());

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <jogador>.");
            return;
        }

        User target = PvP.getUserManager().fetch(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (user.equals(target) || user.getArena().equals(target.getArena())) {
            profile.sendMessage("§cVocê não pode ir para a sua própria sala.");
            return;
        }

        profile.sendMessage("§aEnviando...");

        if (!VanishManager.inVanish(profile))
            VanishManager.vanish(profile);

        PvP.getGameManager().redirect(user, target.getArena());

        profile.player().teleport(target.getProfile().player());
    }
}
