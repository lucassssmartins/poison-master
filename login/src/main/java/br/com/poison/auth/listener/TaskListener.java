package br.com.poison.auth.listener;

import br.com.poison.auth.user.User;
import br.com.poison.auth.Auth;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskListener implements Listener {

    private final int MAX_TIME = 60;

    @EventHandler
    public void timer(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (User user : Auth.getUserManager().documents()) {
                if (user == null) continue;

                Player player = user.getProfile().player();

                if (player == null) continue;

                if (event.getTicks() % 200 == 0 && user.isCaptcha() && !user.isAuthenticated()) {
                    player.sendMessage((user.getProfile().getRelation().getAuth().isRegistered()
                            ? "§cAutentique-se usando: §e/autenticar <senha>§c."
                            : "§cCadastre-se usando: §e/cadastrar <senha> <confirmar-senha>§c.")
                    );
                }

                if (!user.isCaptcha() || !user.isAuthenticated()) {
                    user.setTime(user.getTime() + 1);

                    if (user.getTime() == MAX_TIME) {
                        player.kickPlayer((!user.isCaptcha()
                                ? "§cVocê demorou demais para concluir a verificação"
                                : !user.isAuthenticated() ? "§cVocê demorou demais para se autenticar" : "") + ", tente novamente!");
                        return;
                    }

                    int time = MAX_TIME - user.getTime();

                    PlayerManager.sendBar(player, "§c" + (user.getProfile().getAuth().isRegistered() ? "Autentique-se" : "Cadastre-se")
                            + " em §e" + (time) + "s§c!");

                    player.setLevel(time);
                }
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();

            User user = Auth.getUserManager().read(player.getUniqueId());

            if (user == null) return;

            Inventory inventory = event.getInventory();

            if (inventory == null) return;

            if (!user.isCaptcha() && inventory.getTitle().equalsIgnoreCase("Captcha")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline() || user.isCaptcha()) {
                            cancel();
                            return;
                        }

                        player.openInventory(inventory);
                    }
                }.runTaskLater(Auth.getPlugin(Auth.class), 10L);
            }

        }
    }
}
