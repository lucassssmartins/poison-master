package br.com.poison.core.bukkit.inventory.profile.stats.category;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.resources.member.type.pvp.stats.PvPStats;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class PvPStatsInventory extends Inventory {

    private final PvPMember member;

    public PvPStatsInventory(Player player, Profile profile, Inventory last) {
        super(player, "Estatísticas > PvP", last, 4);

        PvPMember member = Core.getPvpData().fetch(profile.getId(), false);

        if (member == null)
            member = Core.getPvpData().input(profile);

        if (member != null)
            Core.getPvpManager().save(member);

        this.member = member;
    }

    @Override
    public void init() {
        clear();

        PvPStats stats = member.getStats();

        addItem(10, new Item(Material.WOOD_SWORD)
                .name("§aArena")
                .lore("§fKills: §7" + Util.formatNumber(stats.getArena().getKills()),
                        "§fDeaths: §7" + Util.formatNumber(stats.getArena().getDeaths()),
                        "",
                        "§fStreak: §7" + Util.formatNumber(stats.getArena().getStreak()),
                        "§fMelhor Streak: §7" + Util.formatNumber(stats.getArena().getBestStreak()))
                .flags(ItemFlag.values()));

        addItem(11, new Item(Material.STAINED_GLASS, 1, 7)
                .name("§aFPS")
                .lore("§fKills: §7" + Util.formatNumber(stats.getFps().getKills()),
                        "§fDeaths: §7" + Util.formatNumber(stats.getFps().getDeaths()),
                        "",
                        "§fStreak: §7" + Util.formatNumber(stats.getFps().getStreak()),
                        "§fMelhor Streak: §7" + Util.formatNumber(stats.getFps().getBestStreak())));

        addItem(12, new Item(Material.LAVA_BUCKET)
                .name("§aLava")
                .lore("§fFácil: §a" + Util.formatNumber(stats.getLava().getEasy()),
                        "§fMédio: §e" + Util.formatNumber(stats.getLava().getMedium()),
                        "§fDifícil: §c" + Util.formatNumber(stats.getLava().getHard()),
                        "§fExtremo: §4" + Util.formatNumber(stats.getLava().getExtreme()),
                        "",
                        "§fMelhor tempo: §7" + TimeUtil.formatTime(stats.getLava().getBestTime())));

        addItem(13, new Item(Material.WATER_BUCKET)
                .name("§aMLG")
                .lore("§fAcertos: §7" + Util.formatNumber(stats.getMlg().getWins()),
                        "§fErros: §7" + Util.formatNumber(stats.getMlg().getDefeats()),
                        "",
                        "§fSequência de Acertos: §7" + Util.formatNumber(stats.getMlg().getStreak()),
                        "§fMelhor Sequência: §7" + Util.formatNumber(stats.getMlg().getBestStreak())));

        addItem(14, new Item(Material.STAINED_CLAY, 1, 3)
                .name("§aDamage")
                .lore("§7..."));

        if (isReturnable())
            addBackButton();

        display();
    }
}
