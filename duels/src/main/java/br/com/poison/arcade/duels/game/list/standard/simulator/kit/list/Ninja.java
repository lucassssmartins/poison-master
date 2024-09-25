package br.com.poison.arcade.duels.game.list.standard.simulator.kit.list;

import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class Ninja extends Kit implements Listener {

    public Ninja() {
        super("Ninja", "Apareça nas costas do seu inimigo.", Material.EMERALD, 8);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        Client client = Duels.getClientManager().read(player.getUniqueId());

        if (client == null) return;

        if (isUsingKit(player.getUniqueId()) && client.getData().hasLastHit()) {
            Player target = client.getData().getLastHitPlayer();

            if (hasCooldown(player)) return;

            if (player.getLocation().distance(target.getLocation()) > 50) {
                player.sendMessage("§cO jogador está longe demais ;(");
                return;
            }

            addCooldown(player, getCooldown());

            player.teleport(target);
            player.sendMessage("§aVocê foi teleportado até o §e" + target.getName() + "§a!");
        }
    }
}
