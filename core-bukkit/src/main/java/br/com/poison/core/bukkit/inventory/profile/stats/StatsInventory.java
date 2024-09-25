package br.com.poison.core.bukkit.inventory.profile.stats;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.inventory.profile.stats.category.DuelStatsInventory;
import br.com.poison.core.bukkit.inventory.profile.stats.category.PvPStatsInventory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.Profile;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class StatsInventory extends Inventory {

    private final Profile profile;

    public StatsInventory(Player player, Profile profile, Inventory last) {
        super(player, "Estatísticas", last, 4);

        this.profile = profile;
    }

    @Override
    public void init() {
        clear();

        addItem(10, new Item(Material.IRON_CHESTPLATE)
                .name("§aPvP")
                .lore("§7Clique para ver as estatísticas!")
                .click(event -> {
                    playSound(SoundCategory.CHANGE);

                    new PvPStatsInventory(getPlayer(), profile, this).init();
                }));

        addItem(11, new Item(Material.DIAMOND_SWORD)
                .name("§aDuels")
                .lore("§7Clique para ver as estatísticas!")
                .click(event -> {
                    playSound(SoundCategory.CHANGE);

                    new DuelStatsInventory(getPlayer(), profile, this).init();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
