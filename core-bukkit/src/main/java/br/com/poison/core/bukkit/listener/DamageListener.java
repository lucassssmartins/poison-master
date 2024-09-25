package br.com.poison.core.bukkit.listener;

import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity(),
                    damager = player.getKiller();

            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();

                if (projectile.getShooter() instanceof Player)
                    damager = (Player) projectile.getShooter();
            }

            if (damager == null) return;

            ItemStack hand = player.getItemInHand();

            double damage = event.getDamage();
            double damageSword = getDamage(hand.getType());

            boolean isMore = damage > 1;

            if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                        double minus;
                        if (isCritical(player)) {
                            minus = (damageSword + (damageSword / 2)) * 1.3 * (effect.getAmplifier() + 1);
                        } else {
                            minus = damageSword * 1.3 * (effect.getAmplifier() + 1);
                        }
                        damage = damage - minus;
                        damage += 2 * (effect.getAmplifier() + 1);
                        break;
                    }
                }
            }
            if (!hand.getEnchantments().isEmpty()) {
                if (hand.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS) && isArthropod(event.getEntityType())) {
                    damage = damage - (1.5 * hand.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS));
                    damage += hand.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
                }
                if (hand.containsEnchantment(Enchantment.DAMAGE_UNDEAD) && isUndead(event.getEntityType())) {
                    damage = damage - (1.5 * hand.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD));
                    damage += hand.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
                }
                if (hand.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                    damage = damage - 1.25 * hand.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                    damage += hand.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                }
            }
            if (isCritical(player)) {
                damage = damage - (damageSword / 2);
                damage += 1;
            }

            if (isMore)
                damage -= 2;

            PlayerDamagePlayerEvent damagePlayerEvent = new PlayerDamagePlayerEvent(
                    player, damager,
                    damage, event.getFinalDamage(),
                    (!(event.getDamager() instanceof Player) ? PlayerDamagePlayerEvent.AttackType.PROJECTILE : PlayerDamagePlayerEvent.AttackType.ATTACK),
                    event.isCancelled()
            );

            damagePlayerEvent.call();

            event.setDamage(damagePlayerEvent.getDamage());

            event.setCancelled(damagePlayerEvent.isCancelled());
        }
    }

    public boolean isItemFilter(Material type) {
        String name = type.name();

        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE");
    }

    @SuppressWarnings("deprecation")
    private boolean isCritical(Player p) {
        return p.getFallDistance() > 0 && !p.isOnGround() && !p.hasPotionEffect(PotionEffectType.BLINDNESS);
    }

    private boolean isArthropod(EntityType type) {
        switch (type) {
            case SILVERFISH:
            case SPIDER:
            case CAVE_SPIDER:
                return true;
            default:
                break;
        }
        return false;
    }

    private boolean isUndead(EntityType type) {
        switch (type) {
            case PIG_ZOMBIE:
            case WITHER_SKULL:
            case ZOMBIE:
            case SKELETON:
                return true;
            default:
                break;
        }
        return false;
    }

    private double getDamage(Material type) {
        double damage = type.equals(Material.AIR) ? 0.5 : 2.0;

        if (type.toString().contains("DIAMOND_")) {
            damage = 8.0;
        } else if (type.toString().contains("IRON_")) {
            damage = 7.0;
        } else if (type.toString().contains("STONE_")) {
            damage = 6.0;
        } else if (type.toString().contains("WOOD_")) {
            damage = 5.0;
        } else if (type.toString().contains("GOLD_")) {
            damage = 5.0;
        }
        if (!type.toString().contains("_SWORD")) {
            damage--;
            if (!type.toString().contains("_AXE")) {
                damage--;
                if (!type.toString().contains("_PICKAXE")) {
                    damage--;
                    if (!type.toString().contains("_SPADE")) {
                        damage = 1.0;
                    }
                }
            }
        }
        return damage;
    }
}