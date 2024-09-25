package br.com.poison.core.bukkit.manager;

import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VanishManager implements Listener {

    private static final Set<Profile> vanishers = new HashSet<>();

    private static final Map<UUID, GameMode> gameModeMap = new HashMap<>();
    private static final Map<UUID, ItemStack[]> items = new HashMap<>(), armors = new HashMap<>();

    public static boolean inVanish(Profile profile) {
        return vanishers.contains(profile);
    }

    public static boolean canNotSee(Profile source, Profile vanisher) {
        return !source.hasRank(vanisher.getRank().getCategory()) && inVanish(vanisher);
    }

    public static void removeVanish(Profile profile) {
        if (inVanish(profile))
            vanishers.remove(profile);
    }

    public static void vanish(Profile profile) {
        Player player = profile.player();

        if (!inVanish(profile)) {
            vanishers.add(profile);

            gameModeMap.put(profile.getId(), player.getGameMode());

            items.put(profile.getId(), player.getInventory().getContents());
            armors.put(profile.getId(), player.getInventory().getArmorContents());

            player.setGameMode(GameMode.CREATIVE);

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            // Esconder dos jogadores
            Core.getProfileManager()
                    .documents(user -> canNotSee(user, profile))
                    .forEach(user -> {
                        Player target = user.player();

                        if (target != null)
                            target.hidePlayer(player);
                    });

            profile.sendMessage("§dVocê entrou no modo VANISH.",
                    "§cAgora você está invisível para " + profile.getRank().getCategory().bellow().getPrefix() + "§c e abaixo.");
        } else {
            vanishers.remove(profile);

            player.setGameMode(gameModeMap.get(profile.getId()));

            player.getInventory().setContents(items.get(profile.getId()));
            player.getInventory().setArmorContents(armors.get(profile.getId()));

            gameModeMap.remove(profile.getId());
            items.remove(profile.getId());
            armors.remove(profile.getId());

            // Mostrar para os jogadores
            Bukkit.getOnlinePlayers().forEach(target -> target.showPlayer(player));

            profile.sendMessage("§cVocê saiu do modo VANISH.",
                    "§cAgora você está visível para todos os jogadores.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanish(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        if (profile == null) return;

        vanishers.stream()
                .filter(vanisher -> canNotSee(profile, vanisher))
                .forEach(vanisher -> {
                    System.out.println("Escondendo " + vanisher.getName() + " de " + player.getName());
                    player.hidePlayer(vanisher.player());
                });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            Profile profile = Core.getProfileManager().read(player.getUniqueId());

            if (profile == null) return;

            if (profile.isStaffer() && inVanish(profile)) {
                player.openInventory(clicked.getInventory());

                Core.getProfileManager().log(player, player.getName() + " abriu o inventário de " + clicked.getName());
            }
        }
    }
}
