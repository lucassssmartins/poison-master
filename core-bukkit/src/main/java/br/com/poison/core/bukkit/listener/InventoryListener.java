package br.com.poison.core.bukkit.listener;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.manager.InventoryManager;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.api.mechanics.item.click.ItemClick;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final InventoryManager manager;

    public InventoryListener() {
        manager = BukkitCore.getInventoryManager();
    }

    // Atualizando os itens dos inventários
    @EventHandler
    public void onInventoryUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Map.Entry<UUID, Inventory> entry : manager.getMap().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                Inventory inventory = entry.getValue();

                if (player == null || inventory == null || inventory.getContents().isEmpty()) continue;

                for (Item item : inventory.getContents().values()) {
                    if (item == null || item.getUpdater() == null) continue;

                    item.getUpdater().runUpdate(player.getOpenInventory());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        manager.delete(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.delete(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        manager.delete(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerClickItem(InventoryClickEvent event) {
        if (event.getRawSlot() > event.getClickedInventory().getSize()) {
            event.setCancelled(false);
            return;
        }

        if (event.getClick().equals(ClickType.NUMBER_KEY) || event.getWhoClicked() == null || event.getClickedInventory() == null
                || !event.getInventory().equals(event.getClickedInventory()) || event.getCurrentItem() == null) {
            event.setCancelled(true);
            return;
        }

        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (!manager.exists(player.getUniqueId())) return;

            Inventory inventory = manager.fetch(player.getUniqueId());

            Item item = inventory.getContents().get(event.getSlot());

            if (inventory.isProtectedSlot(event.getSlot())) {
                event.setCancelled(true);
                return;
            }

            if (event.getCursor() != null && !event.getCursor().getType().equals(Material.AIR) && inventory.isProtectedContent(event.getCurrentItem())) {
                event.setCancelled(true);
                return;
            }

            if (event.getClick().name().startsWith("SHIFT") && !inventory.isAllowShift()) {
                event.setCancelled(true);
                return;
            }

            if (item == null || event.getCurrentItem().getType().equals(Material.AIR))
                return;

            event.setCancelled(inventory.isProtectedContent(event.getCurrentItem()) || !inventory.isAllowClick());

            if (!inventory.isAllowClick())
                player.setItemOnCursor(null);

            ItemClick click = item.getClick();

            if (click != null)
                click.runClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            InventoryView view = event.getView();

            Player player = (Player) view.getPlayer();

            Inventory inventory = manager.fetch(player.getUniqueId());

            if (inventory == null) return;

            event.setCancelled(!inventory.isAllowDrag());
        }
    }

    @EventHandler
    public void onInteractInventory(InventoryCloseEvent event) {
        InventoryView view = event.getView();

        ItemStack cursor = view.getCursor();

        if (cursor != null)
            view.setCursor(null);
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack stack = player.getItemInHand();

        if (stack == null || stack.getType().equals(Material.AIR) || !stack.hasItemMeta()) return;
        if (!Item.exists(stack)) return;

        Item item = Item.convertItem(stack);

        if (event.getAction().name().startsWith("RIGHT") && item.getInteract() != null)
            item.getInteract().runInteract(event);
    }
}
