package br.com.poison.core.bukkit.inventory.profile.stats.category;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import br.com.poison.core.resources.member.type.duels.stats.DuelStats;
import br.com.poison.core.resources.member.type.duels.stats.list.DefaultStats;
import br.com.poison.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DuelStatsInventory extends Inventory {

    private final DuelMember member;

    public DuelStatsInventory(Player player, Profile profile, Inventory last) {
        super(player, "Estatísticas > Duels", last, 4);

        DuelMember member = Core.getDuelData().fetch(profile.getId(), false);

        if (member == null)
            member = Core.getDuelData().input(profile);

        Core.getDuelManager().save(member);

        this.member = member;
    }

    @Override
    public void init() {
        clear();

        DuelStats stats = member.getStats();


        addItem(10, handleStats(ArcadeCategory.SIMULATOR, stats));
        addItem(11, handleStats(ArcadeCategory.NODEBUFF, stats));
        addItem(12, handleStats(ArcadeCategory.SUMO, stats));
        addItem(13, handleStats(ArcadeCategory.GLADIATOR, stats));
        addItem(14, handleStats(ArcadeCategory.UHC, stats));
        addItem(15, handleStats(ArcadeCategory.SOUP, stats));

        if (isReturnable())
            addBackButton();

        display();
    }

    protected Item handleStats(ArcadeCategory category, DuelStats duelStats) {
        Item item = new Item(Material.valueOf(category.getIcon()));

        item.name("§a" + category.getName());

        DefaultStats stats = getDefaultStats(category, duelStats);

        item.lore("§fPartidas jogadas: §7" + Util.formatNumber(stats.getMatches()),
                "§fPorcentagem de vitória: §7" + stats.getWinAverage() + "%",
                "",
                "§fVitórias: §7" + Util.formatNumber(stats.getWins()),
                "§fDerrotas: §7" + Util.formatNumber(stats.getDefeats()),
                "",
                "§fWinstreak: §7" + Util.formatNumber(stats.getStreak()),
                "§fMelhor Winstreak: §7" + Util.formatNumber(stats.getBestStreak()));

        return item;
    }

    private static DefaultStats getDefaultStats(ArcadeCategory category, DuelStats duelStats) {
        switch (category) {
            case SOUP:
                return duelStats.getSoup();
            case NODEBUFF:
                return duelStats.getNodebuff();
            case SUMO:
                return duelStats.getSumo();
            case SIMULATOR:
                return duelStats.getSimulator();
            case GLADIATOR:
                return duelStats.getGladiator();
            case UHC:
                return duelStats.getUhc();
            default:
                return new DefaultStats();
        }
    }
}
