package br.com.poison.core.bukkit.command.list.special;

import br.com.poison.core.bukkit.api.user.medal.MedalController;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.medal.Medal;

public class MedalCommand implements CommandInheritor {

    @Command(name = "medal", aliases = {"medalha"})
    public void medal(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (args.length == 0) {
            MedalController.sendMedals(profile);
            return;
        }

        Medal medal = Medal.fetch(args[0]);

        if (medal == null || !profile.hasMedal(medal)) {
            profile.sendMessage("§cA medalha solicitada não existe ou você não a possui.");
            return;
        }

        if (profile.isUsingMedal(medal)) {
            profile.sendMessage("§cVocê já está usando essa medalha!");
            return;
        }

        profile.setMedal(medal);
        profile.sendMessage("§aA medalha " + medal.getColoredName() + "§a foi selecionada.");
    }
}
