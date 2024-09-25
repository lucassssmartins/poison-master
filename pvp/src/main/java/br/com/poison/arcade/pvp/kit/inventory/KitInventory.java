package br.com.poison.arcade.pvp.kit.inventory;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KitInventory extends Inventory {

    private final User user;
    
    private final KitType type;

    @Setter
    private OrderType orderType;

    public KitInventory(Player player, KitType type) {
        super(player, "Selecionar Kit " + type.getId(), 6, 21);

        this.user = PvP.getUserManager().read(player.getUniqueId());
        
        this.type = type;
        this.orderType = OrderType.A_Z;
    }

    @Override
    public void init() {
        clear();

        List<Kit> kits = orderType.handleOrder(type);

        if (kits.isEmpty()) {
            addItem(22, new Item(Material.WEB).name("§cNenhum kit encontrado ;("));
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
            if (kit == null) continue;

            List<String> lore = new ArrayList<>(kit.getLore());

            lore.add("");

            Item kitItem;

            if (kit.hasPermission(getPlayer().getUniqueId())) {
                lore.add((!user.isUsingKit(kit.getName()) ? "§eClique para selecionar!" : "§cKit em uso!"));

                kitItem = new Item(kit.getIcon())
                        .name("§a" + kit.getName())
                        .lore(lore)
                        .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                        .click(event -> {
                            close();

                            playSound(SoundCategory.SUCCESS);

                            getPlayer().performCommand("kit" + (type.equals(KitType.SECONDARY) ? "2" : "") + " " + kit.getName().toLowerCase());
                        });
            } else {
                lore.add("§cVocê não tem acesso a este kit!");

                kitItem = new Item(Material.STAINED_GLASS_PANE, 1, 14)
                        .name("§c" + kit.getName())
                        .lore(lore)
                        .click(event -> {
                            close();

                            playSound(SoundCategory.WRONG);

                            getPlayer().sendMessage("§cVocê não tem acesso a este kit!");
                        });
            }

            if (kitItem != null)
                addItem(slot, kitItem);

            slot += 1;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        Kit selecionedKit = type.equals(KitType.PRIMARY) ? user.getPrimaryKit() : user.getSecondaryKit();

        Item selecionedItem = new Item(selecionedKit == null ? Material.ITEM_FRAME : selecionedKit.getIcon());
        selecionedItem.name("§aKit selecionado §6- §e" + (selecionedKit == null ? "Nenhum" : selecionedKit.getName()));

        if (selecionedKit != null)
            selecionedItem.lore(selecionedKit.getLore());

        addItem(getRows() * 9 - 4, new Item(Material.BARRIER)
                .name("§cRemover kit atual")
                .lore("§7" + (selecionedKit == null ? "Nenhum kit selecionado!" : "Clique para remover este kit!"))
                .click(event -> {
                    if (selecionedKit == null) return;

                    if (type.equals(KitType.PRIMARY))
                        user.setPrimaryKit(null);
                    else if (type.equals(KitType.SECONDARY))
                        user.setSecondaryKit(null);

                    playSound(SoundCategory.SUCCESS);

                    Core.getMultiService().async(() -> user.getGame().sendSidebar(user));

                    init();
                }));

        addItem(getRows() * 9 - 5, selecionedItem);

        addItem(getRows() * 9 - 6, new Item(Material.SLIME_BALL)
                .name("§aOrdenar por:")
                .lore("§7" + orderType.getTag())
                .click(event -> {
                    setOrderType(orderType.next());

                    playSound(SoundCategory.CHANGE);

                    init();
                }));

        display();
    }

    @Getter
    @AllArgsConstructor
    public enum OrderType {
        A_Z("A-Z"),
        Z_A("Z-A");

        private final String tag;

        public OrderType next() {
            return this == Z_A ? A_Z : Z_A;
        }

        public List<Kit> handleOrder(KitType type) {
            List<Kit> kits = PvP.getKitManager().getKits(type);

            if (this == OrderType.Z_A) {
                kits.sort(Comparator.comparing(Kit::getName).reversed());
            } else {
                kits.sort(Comparator.comparing(Kit::getName));
            }

            return kits;
        }
    }
}