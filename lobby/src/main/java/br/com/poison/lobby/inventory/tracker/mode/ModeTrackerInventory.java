package br.com.poison.lobby.inventory.tracker.mode;

import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.Util;
import org.bukkit.entity.Player;

public class ModeTrackerInventory extends Inventory {

    private final Player receiver;

    public ModeTrackerInventory(Player player, Player receiver) {
        super(player, (receiver == null ? "Selecionar modo" : "Duelar com " + receiver.getName()), 4);

        this.receiver = receiver;
    }

    @Override
    public void init() {
        clear();

        int slot = 10;
        for (ArcadeCategory category : ArcadeCategory.values()) {
            if (!category.getServer().equals(ServerCategory.DUELS)) continue;

            addItem(slot, new Item(category.getIcon())
                    .name("§a" + category.getName())
                    .lore("§7" + Util.formatNumber(category.getPlayingNow()) + " jogando agora.",
                            "",
                            "§eClique para jogar!")
                    .click(event -> {
                        playSound(SoundCategory.CHANGE);

                        new ModeSearchedInventory(getPlayer(), receiver, category, this).init();
                    }));

            slot++;

            if (slot == 17)
                slot += 2;
        }

        display();
    }
}
