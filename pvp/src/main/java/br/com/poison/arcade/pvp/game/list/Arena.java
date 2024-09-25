package br.com.poison.arcade.pvp.game.list;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.inventory.KitInventory;
import br.com.poison.arcade.pvp.kit.inventory.ShopKitInventory;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.resources.member.type.pvp.stats.list.ArenaStats;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.bukkit.BukkitUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class Arena extends Game {

    public Arena(PvP instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.ARENA);
    }

    @Override
    public void sendKit(Player player) {
        User user = PvP.getUserManager().read(player.getUniqueId());

        PlayerInventory inv = player.getInventory();

        if (user.isProtected()) {
            inv.setItem(0, new Item(Material.CHEST)
                    .name("§aSelecionar Kit 1")
                    .interact(event -> new KitInventory(event.getPlayer(), KitType.PRIMARY).init()));

            inv.setItem(1, new Item(Material.CHEST)
                    .name("§aSelecionar Kit 2")
                    .interact(event -> new KitInventory(event.getPlayer(), KitType.SECONDARY).init()));

            inv.setItem(4, new Item(Material.EMERALD)
                    .name("§aLoja de Kits")
                    .interact(event -> new ShopKitInventory(event.getPlayer()).init()));

            inv.setItem(8, new Item(Material.DARK_OAK_DOOR_ITEM)
                    .name("§cVoltar ao Lobby")
                    .interact(event -> event.getPlayer().performCommand("lobby")));
        } else {
            BukkitUtil.makeRecraftInventory(player);

            inv.setItem(0, new Item(Material.STONE_SWORD));

            int slot = 1;

            Kit primary = user.getPrimaryKit();
            if (primary != null) {
                List<Item> bonusItems = primary.getExtraItems();

                if (!bonusItems.isEmpty())
                    for (Item bonusItem : bonusItems) {
                        inv.setItem(slot, bonusItem);

                        slot++;
                    }
            }

            Kit secondary = user.getSecondaryKit();
            if (secondary != null) {
                List<Item> bonusItems = secondary.getExtraItems();

                if (!bonusItems.isEmpty())
                    for (Item bonusItem : bonusItems) {
                        inv.setItem(slot, bonusItem);

                        slot++;
                    }
            }

            inv.setItem(8, new Item(Material.COMPASS));
        }
    }

    @Override
    public void sendSidebar(User user) {
        PvPMember member = user.getMember();
        ArenaStats arena = member.getStats().getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.setTitle("ARENA");
        sidebar.clear();
        sidebar.blankRow();

        sidebar.addRow("kills", "Kills: ", "§7" + Util.formatNumber(arena.getKills()));
        sidebar.addRow("deaths", "Deaths: ", "§7" + Util.formatNumber(arena.getDeaths()));
        sidebar.addRow("streak", "Streak: ", "§7" + Util.formatNumber(arena.getStreak()));

        if (user.getPrimaryKit() != null) {
            sidebar.blankRow();
            sidebar.addRow("kit", "Kit: ", "§a" + user.getPrimaryKit().getName());
        }

        if (user.getSecondaryKit() != null) {
            if (user.getPrimaryKit() == null)
                sidebar.blankRow();

            sidebar.addRow("kit2", "Kit"
                    + (user.getPrimaryKit() != null ? " 2" : "") + ": ", "§a" + user.getSecondaryKit().getName());
        }

        sidebar.blankRow();
        sidebar.addRow("coins", "Coins: ", "§6" + Util.formatNumber(member.getCoins()));
        sidebar.addRow("rating", "Rating: ", "§b" + Util.formatNumber(member.getXp()) + " " + member.getLeague().getSymbolPrefix());

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagController.updateTag(sidebar.getOwner());
    }

    @Override
    public void updateSidebar(User user) {
        PvPMember member = user.getMember();
        ArenaStats arena = member.getStats().getArena();

        Sidebar sidebar = user.getSidebar();

        sidebar.updateRow("kills", "§7" + Util.formatNumber(arena.getKills()));
        sidebar.updateRow("deaths", "§7" + Util.formatNumber(arena.getDeaths()));
        sidebar.updateRow("streak", "§7" + Util.formatNumber(arena.getStreak()));

        if (user.getPrimaryKit() != null)
            sidebar.updateRow("kit", "§a" + user.getPrimaryKit().getName());

        if (user.getSecondaryKit() != null)
            sidebar.updateRow("kit2", "§a" + user.getSecondaryKit().getName());

        sidebar.updateRow("coins", "§6" + Util.formatNumber(member.getCoins()));
        sidebar.updateRow("rating", "§b" + Util.formatNumber(member.getXp()) + " " + member.getLeague().getSymbolPrefix());
    }

    @Override
    public void onDeath(User user, User killer) {
        Profile profile = user.getProfile();
        PvPMember member = user.getMember();

        ArenaStats stats = member.getStats().getArena();

        br.com.poison.arcade.pvp.game.arena.Arena arena = user.getArena();

        Player player = profile.player();

        boolean hasKiller = killer != null;

        int xp = Core.RANDOM.ints(7, 15).findFirst().orElse(6);

        if (stats.getStreak() >= 5 && hasKiller) {
            arena.sendMessage("§e" + player.getName()
                    + "§c perdeu seu streak de §e" + stats.getStreak()
                    + "§c para §e" + killer.getProfile().getName() + "§c!");
        }

        stats.setDeaths(stats.getDeaths() + 1);
        stats.setStreak(0);

        member.removeXp(xp);

        profile.sendMessage("§cVocê morreu" + (hasKiller ? " para " + killer.getProfile().getName() : "") + ".",
                "§c-" + xp + " XP's");

        member.saveStats(member.getStats());

        if (player.getFireTicks() > 0)
            Core.getMultiService().syncLater(() -> player.setFireTicks(0), 5L);

        user.getArena().spawn(player);

        if (hasKiller) {
            profile = killer.getProfile();
            member = killer.getMember();

            stats = member.getStats().getArena();

            xp = Core.RANDOM.ints(15, 30).findFirst().orElse(10);

            int coins = Core.RANDOM.ints(40, 100).findFirst().orElse(40);

            stats.setKills(stats.getKills() + 1);
            stats.setStreak(stats.getStreak() + 1);

            if (stats.getStreak() > stats.getBestStreak())
                stats.setBestStreak(stats.getStreak());

            if (stats.getStreak() % 5 == 0)
                arena.sendMessage("§b" + member.getName() + "§e está com um streak de §b" + stats.getStreak() + "§e!");

            member.addXp(xp);
            member.addCoins(coins);

            member.saveStats(member.getStats());

            profile.sendMessage("§aVocê matou " + player.getName() + ".",
                    "§b+" + xp + " XP's",
                    "§6+" + coins + " Coins");

            killer.setCombatExpiresAt(Long.MIN_VALUE);

            updateSidebar(killer);
        }
    }

    @Override
    public void initEntities(br.com.poison.arcade.pvp.game.arena.Arena arena) {

    }
}
