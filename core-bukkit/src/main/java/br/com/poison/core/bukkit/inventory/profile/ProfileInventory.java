package br.com.poison.core.bukkit.inventory.profile;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.inventory.profile.preference.PreferenceInventory;
import br.com.poison.core.bukkit.inventory.profile.stats.StatsInventory;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ProfileInventory extends Inventory {

    private final Profile profile;

    public ProfileInventory(Player player) {
        super(player, "Meu perfíl", 5);

        this.profile = Core.getProfileManager().read(player.getUniqueId());
    }

    @Override
    public void init() {
        clear();

        addItem(13, new Item(Material.SKULL_ITEM, 1, 3)
                .name("§aSuas informações")
                .skullByBase64(profile.getSkin().getTexture().getValue())
                .lore("§fRank: " + profile.getRank().getPrefix()));

        addItem(29, new Item(Material.REDSTONE_COMPARATOR)
                .name("§aEditar preferências")
                .lore("§eClique para editar!")
                .click(event -> {
                    playSound(SoundCategory.CHANGE);

                    new PreferenceInventory(profile, this).init();
                }));

        addItem(30, new Item(Material.DOUBLE_PLANT)
                .name("§aMedalhas")
                .lore("§eClique para ver!")
                .click(event -> {
                    close();

                    playSound(SoundCategory.CHANGE);

                    getPlayer().performCommand("medalha");
                }));

        addItem(31, new Item(Material.PAPER)
                .name("§aEstatísticas")
                .lore("§eClique para ver!")
                .click(event -> {
                    playSound(SoundCategory.CHANGE);

                    new StatsInventory(getPlayer(), profile, this).init();
                }));

        display();
    }
}
