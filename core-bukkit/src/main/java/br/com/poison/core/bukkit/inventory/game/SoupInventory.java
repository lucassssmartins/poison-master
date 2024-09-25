package br.com.poison.core.bukkit.inventory.game;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SoupInventory extends Inventory {

    public SoupInventory(Player player) {
        super(player, "Sopas", true, 4);
    }

    @Override
    public void init() {
        clear();

        for (int slot = 0; slot < (getRows() * 9); slot++)
            addItem(slot, new Item(Material.MUSHROOM_SOUP));

        display();
    }
}
