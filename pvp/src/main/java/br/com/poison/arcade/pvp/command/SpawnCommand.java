package br.com.poison.arcade.pvp.command;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandInheritor {

    @Command(name = "spawn")
    public void spawnCommand(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) {
            player.sendMessage("§cNão é possível ir para o Spawn!");
            return;
        }

        if (user.isProtected()) {
            player.sendMessage("§cVocê já está no spawn!");
            return;
        }

        user.getArena().spawn(player);
    }
}
