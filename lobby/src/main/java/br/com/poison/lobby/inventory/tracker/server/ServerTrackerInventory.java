package br.com.poison.lobby.inventory.tracker.server;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.game.Game;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class ServerTrackerInventory extends Inventory {

    private final User user;

    public ServerTrackerInventory(Player player) {
        super(player, "Selecionar Jogo", 3);

        this.user = Lobby.getUserManager().read(player.getUniqueId());
    }

    @Override
    public void init() {
        clear();

        createMode(10, Material.IRON_CHESTPLATE, ServerCategory.PVP);
        createMode(11, Material.DIAMOND_SWORD, ServerCategory.DUELS);

        display();
    }

    protected void createMode(int slot, Material material, ServerCategory category) {
        Game game = Lobby.getGameManager().getGame(category.name());

        Item item = new Item(material);

        item.name("§a" + category.getName());

        item.lore("§7" + Util.formatNumber(Core.getServerManager().getTotalPlayers(category) + (game != null ? game.getPlayers() : 0))
                + " jogando.",
                "",
                "§eClique para jogar!");

        item.updater(view -> {
            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

            item.lore("§7" + Util.formatNumber(Core.getServerManager().getTotalPlayers(category) + (game != null ? game.getPlayers() : 0))
                    + " jogando.",
                    "",
                    "§eClique para jogar!");

            view.setItem(slot, item);

            getPlayer().updateInventory();
        });

        item.click(event -> {
            close();

            playSound(SoundCategory.CHANGE);

            if (game != null)
                Lobby.getGameManager().redirect(user, game);
            else
                user.getProfile().redirect(category);
        });

        item.flags(ItemFlag.values());

        addItem(slot, item);
    }
}