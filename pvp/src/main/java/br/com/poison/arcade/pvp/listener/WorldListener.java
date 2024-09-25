package br.com.poison.arcade.pvp.listener;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.arcade.pvp.user.lava.LavaLevel;
import br.com.poison.core.Constant;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import br.com.poison.core.bukkit.event.list.cooldown.CooldownEndEvent;
import br.com.poison.core.bukkit.inventory.game.RecraftInventory;
import br.com.poison.core.bukkit.inventory.game.SoupInventory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WorldListener implements Listener {

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        if (user.isProtected() && user.getGame().getCategory().equals(ArcadeCategory.ARENA)
                && event.getTo().distance(user.getArena().getLocation("spawn")) >= 5)
            user.removeProtection(player);
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            User user = PvP.getUserManager().read(player.getUniqueId());

            if (user == null) return;

            if (user.isProtected() && user.getGame().getCategory().equals(ArcadeCategory.FPS)
                    && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);

                user.removeProtection(player);
            }
        }
    }

    @EventHandler
    public void onLauncher(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        Material down = event.getTo().getBlock().getRelative(BlockFace.DOWN).getType();

        if (down.equals(Material.SPONGE)) {
            user.setOnLauncher(true);

            player.setVelocity(player.getLocation().getDirection().setY(6f));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2.0f, 2.0f);
        }
    }

    @EventHandler
    public void fallByLauncher(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            User user = PvP.getUserManager().read(event.getEntity().getUniqueId());

            if (user != null && user.isOnLauncher() && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);

                user.setOnLauncher(false);
            }
        }
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        if (!user.getProfile().isStaffer() && user.hasCombat() && !event.getMessage().startsWith("/report")) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode usar comandos em combate.");
        }
    }

    @EventHandler
    public void cooldownEnd(CooldownEndEvent event) {
        Player player = event.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        if (user.getGame().isCategory(ArcadeCategory.ARENA)) {
            Cooldown cooldown = event.getCooldown();

            String replacedName = cooldown.getName().replace("kit-", "");
            String kitName = replacedName.substring(0, 1).toUpperCase() + replacedName.substring(1);

            player.sendMessage("§aO kit " + kitName + " está disponível!");

            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 2f, 2f);
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        if (event.getAction().name().contains("RIGHT")) {
            if (event.hasBlock()) {
                Block block = event.getClickedBlock();

                if (block.getType().name().contains("SIGN")) {
                    Sign sign = (Sign) block.getState();

                    String[] lines = sign.getLines();

                    for (String line : lines) {
                        if (line.contains("Sopa")) {
                            new SoupInventory(player).init();
                            break;
                        }

                        if (line.contains("Recraft")) {
                            new RecraftInventory(player).init();
                            break;
                        }

                        if (user.getGame().isCategory(ArcadeCategory.LAVA)) {
                            // Níveis do Lava
                            if (line.contains("Fácil")) {
                                user.completeLava(LavaLevel.EASY);
                                break;
                            }

                            if (line.contains("Médio")) {
                                user.completeLava(LavaLevel.MEDIUM);
                                break;
                            }

                            if (line.contains("Difícil")) {
                                user.completeLava(LavaLevel.HARD);
                                break;
                            }

                            if (line.contains("Extremo")) {
                                user.completeLava(LavaLevel.EXTREME);
                                break;
                            }
                        }
                    }
                }
            }

            if (event.hasItem()) {
                if (event.getItem().getType().equals(Material.COMPASS)) {
                    boolean found = false;

                    for (Entity entity : player.getNearbyEntities(150, 200, 150)) {
                        if (!(entity instanceof Player)) break;

                        User target = PvP.getUserManager().read(entity.getUniqueId());

                        if (target != null && target.isProtected()
                                && !target.getGame().isCategory(user.getGame().getCategory())) return;

                        player.setCompassTarget(entity.getLocation());
                        player.sendMessage("§aBussola apontando para §e" + entity.getName() + "§a.");

                        found = true;
                        break;
                    }

                    if (!found) {
                        player.sendMessage("§cNenhum alvo encontrado!");
                        player.setCompassTarget(player.getWorld().getSpawnLocation());
                    }
                }
            }
        }
    }

    @EventHandler
    public void signChange(SignChangeEvent event) {
        User user = PvP.getUserManager().read(event.getPlayer().getUniqueId());

        if (user == null || !user.getProfile().isStaffer()) return;

        String[] lines = event.getLines();

        for (String line : lines) {

            if (line.contains("sopa")) {
                event.setLine(0, "§c-§6-§e-§a-§b-");

                event.setLine(1, Constant.SERVER_TITLE);

                event.setLine(2, "Sopa");

                event.setLine(3, "§c-§6-§e-§a-§b-");
            }

            if (line.contains("recraft")) {
                event.setLine(0, "§c-§6-§e-§a-§b-");

                event.setLine(1, Constant.SERVER_TITLE);

                event.setLine(2, "Recraft");

                event.setLine(3, "§c-§6-§e-§a-§b-");
            }

            if (line.contains("lava_facil")) {
                event.setLine(0, Constant.SERVER_TITLE);

                event.setLine(1, "§7Nível: §aFácil");
                event.setLine(2, "§a(Clique)");
            }

            if (line.contains("lava_medio")) {
                event.setLine(0, Constant.SERVER_TITLE);

                event.setLine(1, "§7Nível: §eMédio");
                event.setLine(2, "§a(Clique)");
            }

            if (line.contains("lava_dificil")) {
                event.setLine(0, Constant.SERVER_TITLE);

                event.setLine(1, "§7Nível: §cDifícil");
                event.setLine(2, "§a(Clique)");
            }

            if (line.contains("lava_extremo")) {
                event.setLine(0, Constant.SERVER_TITLE);

                event.setLine(1, "§7Nível: §4Extremo");
                event.setLine(2, "§a(Clique)");
            }

        }
    }
}
