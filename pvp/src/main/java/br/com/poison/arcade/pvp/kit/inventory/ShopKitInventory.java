package br.com.poison.arcade.pvp.kit.inventory;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Constant;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShopKitInventory extends Inventory {

    private final User user;

    public ShopKitInventory(Player player) {
        super(player, "Loja de Kits", Math.min((PvP.getKitManager().getKits().size() / 9) + 5, 5), 16);

        this.user = PvP.getUserManager().read(player.getUniqueId());
    }

    @Override
    public void init() {
        clear();

        Profile profile = user.getProfile();
        PvPMember member = user.getMember();

        List<Kit> kits = PvP.getKitManager().getKits()
                .stream()
                .filter(kit -> !kit.hasPermission(profile.getId()))
                .sorted(Comparator.comparingInt(Kit::getPrice))
                .collect(Collectors.toList());

        if (kits.isEmpty()) {
            addItem(22, new Item(Material.WEB).name("§cVocê já tem todos os kits ;)"));
            display();
            return;
        }

        setTotalPages((kits.size() / getMaxItems()) + 1);
        addBorderPage();

        int slot = 10, last = slot;

        for (int i = 0; i < getMaxItems(); i++) {
            setPageIndex(getMaxItems() * (getPageNumber() - 1) + i);

            if (getPageIndex() >= kits.size()) break;

            Kit kit = kits.get(getPageIndex());

            /* Kit Item */
            if (kit != null) {
                List<String> lore = new ArrayList<>(kit.getLore());

                lore.add("");
                lore.add("§fPreço: §6" + Util.formatNumber(kit.getPrice()) + " coins");
                lore.add("");
                lore.add("§aClique para adquirir!");

                addItem(slot, new Item(kit.getIcon())
                        .name("§a" + kit.getName())
                        .lore(lore)
                        .click(event -> {
                            close();

                            playSound(member.getCoins() < kit.getPrice() ? SoundCategory.WRONG : SoundCategory.SUCCESS);

                            if (member.getCoins() < kit.getPrice()) {
                                profile.sendMessage("§cVocê precisa de mais §e"
                                        + Util.formatNumber((kit.getPrice() - member.getCoins())) + " coins§c para comprar este kit!");
                                return;
                            }

                            profile.setPermission("kitpvp.kit." + kit.getName().toLowerCase(), Constant.CONSOLE_UUID);

                            member.removeCoins(kit.getPrice());

                            profile.sendMessage("§aA sua compra foi realizada! Agora você tem o kit §e" + kit.getName() + "§a!");

                            user.getGame().updateSidebar(user);
                        }));
            }

            /* Slot */

            slot += 1;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        display();
    }
}
