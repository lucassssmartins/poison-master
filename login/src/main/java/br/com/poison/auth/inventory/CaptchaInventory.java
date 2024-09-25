package br.com.poison.auth.inventory;

import br.com.poison.auth.user.User;
import br.com.poison.auth.Auth;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CaptchaInventory extends Inventory {

    private final User user;

    public CaptchaInventory(Player player) {
        super(player, "Verificação", 3);

        this.user = Auth.getUserManager().read(player.getUniqueId());
    }

    @Override
    public void init() {
        clear();

        for (int i = 0; i < getRows() * 9; i++) {
            addItem(i, new Item(Material.SKULL_ITEM, 1, 3)
                    .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmM4ZmMzYTdjYTYwNTgzZmM2ZDQ4MTNlNDE0N2M1MmQzYTdmYzRiZDlmYjJlOGY3ZjIzYWU3YzMyZTRiM2U0In19fQ==")
                    .name("§cErrado")
                    .click(event -> {
                        close();

                        getPlayer().kickPlayer("§cVocê errou a verificação, tente novamente!");
                    }));
        }

        addItem(Core.RANDOM.nextInt(getRows() * 9), new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzNhYzdhZWE0MDJjZTYyOWE0ODMzYTQ1MjI4ZGY0NWE1MDYyZDExMmYwZjgyNmMzYzM0M2NmMDY4MDkxY2JlYSJ9fX0=")
                .name("§aConcluir")
                .click(event -> {
                    close();

                    user.setCaptcha(true);
                    user.setTime(1);

                    getPlayer().sendMessage("§aA verificação foi concluída.");

                    playSound(SoundCategory.SUCCESS);
                }));

        display();
    }
}
