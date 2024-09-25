package br.com.poison.arcade.pvp.game.list;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.game.arena.Arena;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.arcade.pvp.user.lava.LavaLevel;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.resources.member.type.pvp.stats.list.LavaStats;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class Lava extends Game {

    public Lava(PvP instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.LAVA);
    }

    @Override
    public void sendKit(Player player) {
        BukkitUtil.makeRecraftInventory(player);
    }

    @Override
    public void sendSidebar(User user) {
        PvPMember member = user.getMember();
        LavaStats lava = member.getStats().getLava();

        Sidebar sidebar = user.getSidebar();

        sidebar.setTitle("LAVA");
        sidebar.clear();
        sidebar.blankRow();

        sidebar.addRow("levels", "§eNíveis:");
        sidebar.addRow("easy", " Fácil: ", "§a" + Util.formatNumber(lava.getEasy()));
        sidebar.addRow("medium", " Médio: ", "§e" + Util.formatNumber(lava.getMedium()));
        sidebar.addRow("hard", " Difícil: ", "§c" + Util.formatNumber(lava.getHard()));
        sidebar.addRow("extreme", " Extremo: ", "§4" + Util.formatNumber(lava.getExtreme()));

        sidebar.blankRow();
        sidebar.addRow("coins", "Coins: ", "§6" + Util.formatNumber(member.getCoins()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagController.updateTag(sidebar.getOwner());
    }

    @Override
    public void updateSidebar(User user) {
        LavaStats lava = user.getMember().getStats().getLava();

        Sidebar sidebar = user.getSidebar();

        sidebar.updateRow("easy", "§a" + Util.formatNumber(lava.getEasy()));
        sidebar.updateRow("medium", "§e" + Util.formatNumber(lava.getMedium()));
        sidebar.updateRow("hard", "§c" + Util.formatNumber(lava.getHard()));
        sidebar.updateRow("extreme", "§4" + Util.formatNumber(lava.getExtreme()));

        sidebar.updateRow("coins", "§6" + Util.formatNumber(user.getMember().getCoins()));
    }

    @Override
    public void onDeath(User user, User killer) {
        Player player = user.getProfile().player();

        sendKit(player);

        Bukkit.getScheduler().runTaskLater(PvP.getPlugin(PvP.class), () -> player.setFireTicks(0), 1L);
        player.setHealth(player.getMaxHealth());

        player.sendMessage("§cVocê morreu.");

        player.teleport(user.getArena().getMap().getLocation("spawn").getLocation().getBukkitLocation(user.getArena().getWorld()));
    }


    @Override
    public void initEntities(Arena arena) {
        List<Location> locations = arena.getLocations(
                "lava_easy_title_hologram",
                "lava_medium_title_hologram",
                "lava_hard_title_hologram",
                "lava_extreme_title_hologram");

        if (locations.isEmpty()) return;

        handleTitle(LavaLevel.EASY, locations.get(0));
        handleTitle(LavaLevel.MEDIUM, locations.get(1));
        handleTitle(LavaLevel.HARD, locations.get(2));
        handleTitle(LavaLevel.EXTREME, locations.get(3));
    }

    protected void handleTitle(LavaLevel level, Location location) {
        HologramServer hologram = BukkitCore.getHologramManager().spawnServer(level.getName().toLowerCase(), location);

        hologram.setText(Collections.singletonList(level.getColor() + "§l" + level.getName().toUpperCase()));
    }
}
