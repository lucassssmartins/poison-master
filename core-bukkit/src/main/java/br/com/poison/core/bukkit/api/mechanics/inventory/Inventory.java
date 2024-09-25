package br.com.poison.core.bukkit.api.mechanics.inventory;

import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class Inventory {

    private final Player player;

    private String title;

    private org.bukkit.inventory.Inventory holder;

    private final Inventory last;
    private final Map<Integer, Item> contents;

    private final List<ItemStack> protectedContents;
    private final List<Integer> protectedSlots;

    // Variáveis configuráveis
    private boolean allowClick, returnable, paginated, allowDrag = true, allowShift = true;

    private int size, rows, maxItems, pageIndex, pageNumber = 1, totalPages = 1;

    public Inventory(Player player, String title, Inventory last, int rows, int maxItems) {
        this.player = player;
        this.title = title;

        this.last = last;
        this.contents = new HashMap<>();

        this.returnable = last != null;
        this.paginated = maxItems > 0;

        this.rows = rows;
        this.size = rows * 9;

        this.maxItems = maxItems;

        this.protectedContents = new ArrayList<>();
        this.protectedSlots = new ArrayList<>();

        this.holder = Bukkit.createInventory(player, size, title);
    }

    public Inventory(Player player, String title, Inventory last, int rows) {
        this(player, title, last, rows, 0);
    }

    public Inventory(Player player, String title, int rows, int maxItems) {
        this(player, title, null, rows, maxItems);
    }

    public Inventory(Player player, String title, int rows) {
        this(player, title, null, rows, 0);
    }

    public Inventory(Player player, String title, boolean allowClick, int rows) {
        this(player, title, null, rows, 0);

        setAllowClick(allowClick);
    }

    /**
     * Inicializar o processo de criação do inventário.
     */
    public abstract void init();

    public void display() {
        if (player == null) return;

        contents.forEach((slot, item) -> holder.setItem(slot, item));

        player.openInventory(holder);

        BukkitCore.getInventoryManager().save(player, this);
    }

    public void close() {
        player.closeInventory();
    }

    public void clear() {
        if (holder != null)
            holder.clear();

        if (!contents.isEmpty())
            contents.clear();
    }

    public void clear(int... selectedSlots) {
        for (int slot : selectedSlots) {

            Item item = contents.get(slot);
            if (item == null) continue;

            item.type(Material.AIR);
            contents.remove(slot);
        }
    }

    public boolean isProtectedContent(ItemStack stack) {
        return stack != null && protectedContents.stream().anyMatch(item -> item.isSimilar(stack));
    }

    public boolean isProtectedSlot(int slot) {
        return protectedSlots.contains(slot);
    }

    public void updateTitle() {
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
                entity.activeContainer.windowId,
                "minecraft:chest",
                new ChatMessage(title),
                player.getOpenInventory().getTopInventory().getSize());

        entity.playerConnection.sendPacket(packet);
        entity.updateInventory(entity.activeContainer);
    }

    public void addItem(int slot, Item item) {
        addItem(slot, item, false);
    }

    public void addItem(int slot, Item item, boolean protection) {
        contents.put(slot, item);

        if (protection)
            protectedContents.add(item);
    }

    public void addBorderPage() {
        // Adicionar botão de página anterior.
        if (pageNumber > 1) {
            addItem(rows * 9 - 9, new Item(Material.STAINED_GLASS_PANE, 1, 14)
                    .name("§cPágina anterior")
                    .click(event -> {
                        setPageNumber(pageNumber - 1);

                        init();

                        playSound(SoundCategory.CHANGE);
                    }));
        }

        // Adicionar botão de próxima página.
        if (pageNumber < totalPages) {
            addItem(rows * 9 - 1, new Item(Material.STAINED_GLASS_PANE, 1, 5)
                    .name("§aPróxima página")
                    .click(event -> {
                        setPageNumber(pageNumber + 1);

                        init();

                        playSound(SoundCategory.CHANGE);
                    }));
        }
    }

    public void addBackButton(int slot) {
        addItem(slot, new Item(Material.ARROW)
                .name("§cVoltar atrás")
                .click(event -> {
                    if (last == null) {
                        playSound(SoundCategory.WRONG);
                        return;
                    }
                    last.init();

                    playSound(SoundCategory.CHANGE);
                })
        );
    }

    public void addBackButton() {
        addBackButton(rows * 9 - 5);
    }

    public Item addPreferenceButton(boolean preference, String name) {
        return new Item(Material.STAINED_GLASS_PANE, 1, (preference ? 5 : 14))
                .name((preference ? "§a" : "§c") +name)
                .lore("§fAtivo: §7" + (preference ? "Sim" : "Não"),
                        "",
                        "§eClique para alternar!");
    }

    public void playSound(SoundCategory category) {
        player.playSound(player.getLocation(), category.getSound(), 1.5f, 2.0f);
    }
}