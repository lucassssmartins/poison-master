package br.com.poison.lobby.inventory.tracker.mode;

import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.Profile;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

@Setter
public class ModeSearchedInventory extends Inventory {

    private final ArcadeCategory arcade;
    private final Player receiver;

    public ModeSearchedInventory(Player player, Player receiver, ArcadeCategory arcade, Inventory last) {
        super(player, "Selecionar Modo", last, 3);

        this.arcade = arcade;
        this.receiver = receiver;
    }

    @Override
    public void init() {
        clear();

        Profile profile = Core.getProfileManager().read(getPlayer().getUniqueId());

        int index = 10;
        for (SlotCategory slot : arcade.getSlots()) {

            addItem(index, new Item(arcade.getIcon())
                    .name("§a" + slot.getTag())
                    .amount(slot.ordinal())
                    .flags(ItemFlag.values())
                    .lore("§eClique para jogar!")
                    .click(event -> {
                        close();

                        playSound(SoundCategory.SUCCESS);

                        if (receiver != null)
                            getPlayer().performCommand("duel " + receiver.getName() + " " + arcade.name());
                        else
                            profile.redirect(GameRouteContext.builder()
                                    .arcade(arcade)
                                    .slot(slot)
                                    .input(InputMode.PLAYER)
                                    .build());
                    }));

            index++;
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
