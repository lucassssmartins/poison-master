package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class Hulk extends Kit implements Listener {

    public Hulk() {
        super("Hulk", Material.SADDLE, KitType.PRIMARY, RankCategory.PLAYER,
                Arrays.asList("§7Pegue seus inimigos em suas costas", "§7e lançe-os para longe."),
                new ArrayList<>(),
                new ArrayList<>(),
                0, 15L);
    }

    @EventHandler
    public void onUse(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), target = (Player) event.getRightClicked();

            if (isUsingKit(player.getUniqueId()) && isAllowReceive(target.getUniqueId()) && player.getItemInHand().getType().equals(Material.AIR)
                    && !player.isInsideVehicle() && !target.isInsideVehicle()) {
                if (hasCooldown(player)) return;

                addCooldown(player, getCooldown());

                player.setPassenger(event.getRightClicked());
            }
        }
    }

    @EventHandler
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer();
        Player hulk = event.getDamager();

        if (hulk.getPassenger() != null && hulk.getPassenger() == player && isUsingKit(hulk.getUniqueId())
                && hulk.getPassenger() == player) {
            event.setCancelled(true);
            player.setSneaking(true);

            Vector v = hulk.getEyeLocation().getDirection().multiply(1.6F);
            v.setY(0.6D);
            player.setVelocity(v);

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setSneaking(false);
                }
            }.runTaskLater(PvP.getPlugin(PvP.class), 10L);
        }
    }
}
