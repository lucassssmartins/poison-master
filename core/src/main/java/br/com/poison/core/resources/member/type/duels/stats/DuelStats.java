package br.com.poison.core.resources.member.type.duels.stats;

import br.com.poison.core.resources.member.type.duels.stats.list.DefaultStats;
import lombok.Getter;

@Getter
public class DuelStats {

    /* Estatísticas padrões */
    private final DefaultStats
            gladiator = new DefaultStats(),
            soup = new DefaultStats(),
            nodebuff = new DefaultStats(),
            uhc = new DefaultStats(),
            simulator = new DefaultStats(),
            sumo = new DefaultStats();

    public int getTotalWinStreak() {
        return (gladiator.getStreak() + soup.getStreak() + nodebuff.getStreak()
                + uhc.getStreak() + simulator.getStreak() + sumo.getStreak());
    }
}
