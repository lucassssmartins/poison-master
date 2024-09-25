package br.com.poison.core.bukkit.inventory.game;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RecraftInventory extends Inventory {

    public RecraftInventory(Player player) {
        super(player, "Recraft", true, 3);
    }

    @Override
    public void init() {
        clear();

        for (int slot = 0; slot < 9; slot++)
            addItem(slot, new Item(Material.RED_MUSHROOM, 64));

        for (int slot = 9; slot < 18; slot++)
            addItem(slot, new Item(Material.BROWN_MUSHROOM, 64));

        for (int slot = 18; slot < 27; slot++)
            addItem(slot, new Item(Material.BOWL, 64));

        display();
    }
}
