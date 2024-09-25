package br.com.poison.arcade.duels.game.list.standard.simulator.kit.inventory;

import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.ClientData;
import br.com.poison.arcade.duels.game.list.standard.simulator.kit.manager.KitManager;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.entity.Player;

import java.util.Set;

public class KitInventory extends Inventory {

    private final Client client;

    public KitInventory(Player player) {
        super(player, "Selecionar kit", 3);

        this.client = Duels.getClientManager().read(player.getUniqueId());
    }

    @Override
    public void init() {
        clear();

        ClientData data = client.getData();

        Set<Kit> kits = KitManager.getKits();

        int slot = 10;
        for (Kit kit : kits) {
            if (!kit.hasKit(getPlayer().getUniqueId())) continue;

            addItem(slot, new Item(kit.getIcon())
                    .name("§a" + kit.getName())
                    .lore("§7" + kit.getLore(),
                            "",
                            (!data.isUsingKit(kit) ? "§eClique para usar!" : "§cEm uso!"))
                    .click(event -> {
                        close();

                        playSound(data.isUsingKit(kit) ? SoundCategory.WRONG : SoundCategory.SUCCESS);

                        if (data.isUsingKit(kit)) {
                            getPlayer().sendMessage("§cVocê já está usando este kit!");
                            return;
                        }

                        client.getSidebar().updateRow("kit", "§7" + kit.getName());
                        data.setKit(kit);

                        getPlayer().sendMessage("§aO kit §e" + kit.getName() + "§a foi selecionado!");
                    }));

            slot++;
        }
        display();
    }
}
