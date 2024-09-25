package br.com.poison.lobby.inventory.hub;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.game.arena.Arena;
import br.com.poison.lobby.user.User;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.Room;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LobbiesInventory extends Inventory {

    private final User user;

    public LobbiesInventory(Player player) {
        super(player, "Selecionar lobby", 4, 12);

        this.user = Lobby.getUserManager().read(player.getUniqueId());
    }

    @Override
    public void init() {
        clear();

        List<Room> arenas = user.getGame().getArenas()
                .stream()
                .sorted(Comparator.comparingInt(Room::getId))
                .collect(Collectors.toList());

        setTotalPages((arenas.size() / getMaxItems()) + 1);
        addBorderPage();

        int slot = 10, last = slot;
        for (int i = 0; i < getMaxItems(); i++) {
            setPageIndex(getMaxItems() * (getPageNumber() - 1) + i);

            if (getPageIndex() >= arenas.size()) break;

            Arena arena = (Arena) arenas.get(getPageIndex());

            boolean isHere = user.getArena().equals(arena);

            if (arena != null) {
                int finalSlot = slot;

                addItem(slot, new Item(Material.INK_SACK, 1, isHere ? 10 : 8)
                        .name("§aLobby "
                                + (arena.getGame().isCategory(ArcadeCategory.HUB) ? "Principal" : arena.getGame().getName()) + " #" + arena.getId())
                        .lore("§7" + Util.formatNumber(arena.getPlayers().size()) + "/" + arena.getMaxPlayers() + " jogando.",
                                "",
                                "§eClique para entrar!")
                        .updater(view -> {
                            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                            Item item = getContents().get(finalSlot);
                            if (item == null) return;

                            item.lore("§7" + Util.formatNumber(arena.getPlayers().size()) + "/" + arena.getMaxPlayers() + " jogando.",
                                    "",
                                    "§eClique para entrar!");

                            view.setItem(finalSlot, item);

                            getPlayer().updateInventory();
                        })
                        .click(event -> {
                            close();

                            Player player = getPlayer();

                            playSound(isHere ? SoundCategory.WRONG : SoundCategory.SUCCESS);

                            if (isHere) {
                                player.sendMessage("§cVocê já está conectado neste lobby!");
                                return;
                            }

                            Lobby.getGameManager().redirect(user, arena);
                        }));
            }

            slot++;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        display();
    }
}
