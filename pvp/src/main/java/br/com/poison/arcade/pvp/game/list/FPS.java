package br.com.poison.arcade.pvp.game.list;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.game.arena.Arena;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.api.mechanics.npc.type.server.NpcServer;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.resources.member.type.pvp.stats.list.FpsStats;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.bukkit.BukkitUtil;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

public class FPS extends Game {

    public FPS(PvP instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.FPS);
    }

    @Override
    public void sendKit(Player player) {
        User user = PvP.getUserManager().read(player.getUniqueId());

        PlayerInventory inv = player.getInventory();

        if (user.isProtected()) {
            inv.setItem(8, new Item(Material.DARK_OAK_DOOR_ITEM)
                    .name("§cVoltar ao lobby")
                    .interact(event -> event.getPlayer().performCommand("lobby")));
        } else {
            BukkitUtil.makeArmorInventory(player, BukkitUtil.ArmorType.IRON);
            BukkitUtil.makeRecraftInventory(player);

            inv.setItem(0, new Item(Material.DIAMOND_SWORD).enchantment(Enchantment.DAMAGE_ALL, 1));
        }
    }

    @Override
    public void sendSidebar(User user) {
        PvPMember member = user.getMember();
        FpsStats fps = member.getStats().getFps();

        Sidebar sidebar = user.getSidebar();

        sidebar.setTitle("FPS");
        sidebar.clear();
        sidebar.blankRow();

        sidebar.addRow("kills", "Kills: ", "§7" + Util.formatNumber(fps.getKills()));
        sidebar.addRow("deaths", "Deaths: ", "§7" + Util.formatNumber(fps.getDeaths()));
        sidebar.addRow("streak", "Streak: ", "§7" + Util.formatNumber(fps.getStreak()));

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
        FpsStats fps = member.getStats().getFps();

        Sidebar sidebar = user.getSidebar();

        sidebar.updateRow("kills", "§7" + Util.formatNumber(fps.getKills()));
        sidebar.updateRow("deaths", "§7" + Util.formatNumber(fps.getDeaths()));
        sidebar.updateRow("streak", "§7" + Util.formatNumber(fps.getStreak()));

        sidebar.updateRow("coins", "§6" + Util.formatNumber(member.getCoins()));
        sidebar.updateRow("rating", "§b" + Util.formatNumber(member.getXp()) + " " + member.getLeague().getSymbolPrefix());
    }

    @Override
    public void onDeath(User user, User killer) {
        Profile profile = user.getProfile();
        PvPMember member = user.getMember();

        FpsStats fps = member.getStats().getFps();

        br.com.poison.arcade.pvp.game.arena.Arena arena = user.getArena();

        Player player = profile.player();

        boolean hasKiller = killer != null;

        int xp = Core.RANDOM.ints(7, 15).findFirst().orElse(6);

        if (fps.getStreak() >= 5 && hasKiller) {
            arena.sendMessage("§e" + player.getName()
                    + "§c perdeu seu streak de §e" + fps.getStreak()
                    + "§c para §e" + killer.getProfile().getName() + "§c!");
        }

        fps.setDeaths(fps.getDeaths() + 1);
        fps.setStreak(0);

        member.removeXp(xp);

        profile.sendMessage("§cVocê morreu" + (hasKiller ? " para " + killer.getProfile().getName() : "") + ".",
                "§c-" + xp + " XP's");

        member.saveStats(member.getStats());

        user.getArena().spawn(player);

        if (hasKiller) {
            profile = killer.getProfile();
            member = killer.getMember();

            fps = member.getStats().getFps();

            xp = Core.RANDOM.ints(15, 30).findFirst().orElse(10);

            int coins = Core.RANDOM.ints(40, 100).findFirst().orElse(40);

            fps.setKills(fps.getKills() + 1);
            fps.setStreak(fps.getStreak() + 1);

            if (fps.getStreak() > fps.getBestStreak())
                fps.setBestStreak(fps.getStreak());

            if (fps.getStreak() % 5 == 0)
                arena.sendMessage("§b" + member.getName() + "§e está com um streak de §b" + fps.getStreak() + "§e!");

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
    public void initEntities(Arena arena) {
        Location location = arena.getLocation("npc_lobby");

        if (location == null) return;

        NpcServer lobby = BukkitCore.getNpcManager().spawnServer(location, new Property("textures",
                "eyJ0aW1lc3RhbXAiOjE1NjM4NDUxMzc3NjUsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdkMGI4NDIyYmZiYWU1ZGM5OWRlOTU2ZTczZWFhZWZmNDUxZTBjYzY1MjBhMTVkN2Q0OTgzNWUzYjZmZGFmYzcifX19",
                "XVLgcj9SfV5unNrlSdW+08QIjNbYF8YzClXsj2HBlIR6w7f5GQ05vryirZrQwXmyUyiHEN3UMV7kUvXlkbzHzWczqWasGErXuc4LCune7UqEizBe9XweBc9waBFJyKlu6QtDQLEkfMEmlMH7EEmbHNUt61ObUgXcO/zCUxOJO2LBnulQxwb0uvzQhSd1oy9mLiVaQg9Eq2vakVKSq3iqg29aeVN4fLa4J2ZPB0W06dXdlyf+RK9TB6rUDKh3VFHVFyTnixiBfN8KmMOiAIdLnrV/3KvPwgLgAD+FHw5nYDe7q1IxjkusIfi/eBypU4V5vUhIma9thM1YN0ILN3A/kHTT0Vwu22mg4wb85sqAlv3T1eLGKk7hx2EtoNRwdS33gDpCv5gZG5ZCPrdGuu9yrxgYdwGeL8cjzP9y/rcNBp/pVlFazAJgoqUicypPPv67dbwKeMDcco4bZs31dsx6sbHJHCoOG1Uiz4dewxEF6z8dn7kHIIE6n+Lg6tMcYg+yskga96L7sccpMaE10AlBsJf7Fn//BQOOssOZoQyiAmB/P4MIuDgkh8MbXsiIEKIjQun9dpiyWtGUDPYtWtx9AhfjOPqpLEeJrFek7QkNn7ZFjU9g1n4mFaTPv9ef3amn4bJ/Gwjmb7yrpwhlSNwU4TOuvLlKBc6obESPLS70EYw="));

        lobby.setAction((player, action) -> player.performCommand("lobby"));

        lobby.setHand(new Item(Material.DARK_OAK_DOOR_ITEM));

        lobby.display();

        HologramServer lobbyHologram = BukkitCore.getHologramManager().spawnServer("lobby", location);

        lobbyHologram.setText(Arrays.asList(
                "§e§lVOLTAR AO LOBBY",
                "§7(Clique aqui)"
        ));
    }
}
