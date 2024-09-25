package br.com.poison.core.bukkit.listener;

import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.service.server.options.ServerOptions;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.server.Server;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.Core;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerListener implements Listener {

    private final List<Material> ALLOWED_DROP_ITEM = Arrays.asList(Material.BOWL, Material.MUSHROOM_SOUP, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM);

    private final List<Material> DANGEROUS_ITEMS = Arrays.asList(Material.TNT);

    private final List<EntityType> BLOCKED_ENTITIES = Arrays.asList(EntityType.SLIME, EntityType.CREEPER);

    @EventHandler
    public void onItemDamage(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack hand = player.getItemInHand();

        if (hand != null && isItemFilter(hand.getType()))
            hand.setDurability((short) 0);
    }

    public boolean isItemFilter(Material type) {
        String name = type.name();

        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_PICKAXE");
    }
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();

        if (item == null) return;

        event.setCancelled(!ServerOptions.DROP_ITEM_ENABLED || !ALLOWED_DROP_ITEM.contains(item.getItemStack().getType()));
    }

    @EventHandler
    public void onBedSpawn(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();

        if (stack != null && (stack.getType().equals(Material.BED) || stack.getType().equals(Material.BED_BLOCK)))
            event.getEntity().remove();
    }

    @EventHandler
    public void onClearDrop(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            /* Verificando convites */
            Core.getInvitationManager().checkInvitations();

            /* Verificando drops do servidor */
            for (World world : Bukkit.getServer().getWorlds())
                for (Entity e : world.getEntitiesByClass(Item.class))
                    if (e.getTicksLived() >= 80) {
                        Location location = e.getLocation().clone();

                        world.playEffect(location, Effect.FLAME, 1);
                        e.remove();
                    }
        }
    }

    @EventHandler
    public void onSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Server server = Core.getServerManager().getServer(Core.getServerCategory());

        if (server != null && server.isArcade()) {
            ItemStack item = player.getItemInHand();

            if (item != null && item.getType().equals(Material.MUSHROOM_SOUP) && player.getHealth() != 20.0) {
                double restorerLife = Math.min(player.getHealth() + 7, player.getMaxHealth());

                player.setHealth(restorerLife);

                player.getItemInHand().setType(Material.BOWL);
                player.updateInventory();
            }
        }
    }

    protected boolean cancelBuild(Player player) {
        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        if (profile == null) return true;

        return !(profile.hasRank(RankCategory.BUILDER) || profile.hasRank(RankCategory.OWNER)) || !player.inGameMode(GameMode.CREATIVE) || !profile.getPreference().isBuildMode();
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        Profile profile = Core.getProfileManager().read(event.getPlayer().getUniqueId());

        if (profile.inServer(ServerCategory.DUELS) && profile.inGame() && profile.getGame().getArcade().isAllowedDangerousBlocks() &&
                DANGEROUS_ITEMS.contains(event.getBlock().getType())) {
            event.setCancelled(false);
            return;
        }

        if (!ServerOptions.TNT_PLACE && block.getType().equals(Material.TNT)) {
            event.setCancelled(true);
            return;
        }

        if (ServerOptions.BLOCK_PLACE) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(cancelBuild(event.getPlayer()));
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        event.setCancelled(cancelBuild(event.getPlayer()));
    }

    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {
        event.setCancelled(!ServerOptions.BLOCK_IGNITE);
    }

    @EventHandler
    public void blockExplode(BlockExplodeEvent event) {
        event.setCancelled(!ServerOptions.BLOCK_EXPLODE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void command(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        String command = event.getMessage().split(" ")[0];

        if (profile.hasCooldown("command-cooldown")) {
            event.setCancelled(true);

            profile.sendMessage("§cAguarde " + TimeUtil.newFormatTime(profile.getCooldown("command-cooldown"))
                    + " para executar comandos novamente.");
            return;
        }

        HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(command);

        if (topic == null) {
            event.setCancelled(true);

            player.sendMessage("Comando não encontrado.");
        }

        if (!profile.isStaffer())
            profile.setCooldown("command-cooldown", TimeUnit.SECONDS.toMillis(3));
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        event.setCancelled(!ServerOptions.DAMAGE_ENABLED);
    }

    @EventHandler
    public void achievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void weather(WeatherChangeEvent event) {
        World world = event.getWorld();

        if (ServerOptions.ALWAYS_DAY) {
            world.setTime(0);
            world.setGameRuleValue("doDaylightCycle", "false");
        }

        if (world.hasStorm())
            world.setWeatherDuration(0);

        event.setCancelled(!ServerOptions.WEATHER_ENABLED);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void food(FoodLevelChangeEvent event) {
        if (!ServerOptions.FOOD_ENABLED)
            event.setFoodLevel(20);

        event.setCancelled(!ServerOptions.FOOD_ENABLED);
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void creature(CreatureSpawnEvent event) {
        for (EntityType type : BLOCKED_ENTITIES) {
            if (event.getEntity().getType().equals(type)) {
                event.setCancelled(true);
            }
        }

        if (!(event.getEntity() instanceof ArmorStand)) {
            event.setCancelled(!ServerOptions.SPAWN_CREATURES);
        }
    }
}
