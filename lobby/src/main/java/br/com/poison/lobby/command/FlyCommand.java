package br.com.poison.lobby.command;

import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.preference.Preference;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandInheritor {

    @Command(name = "fly", aliases = {"voar"}, rank = RankCategory.VENOM)
    public void fly(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        Player player = context.getPlayer();

        player.setAllowFlight(!player.getAllowFlight());
        player.setFlying(player.getAllowFlight());

        Preference preference = profile.getPreference();

        preference.setFlyingLobby(player.isFlying());
        profile.savePreference(preference);

        profile.sendMessage(player.isFlying() ? "§aO seu modo de voo foi ativado!" : "§cO seu modo de voo foi desativado!");
    }
}
