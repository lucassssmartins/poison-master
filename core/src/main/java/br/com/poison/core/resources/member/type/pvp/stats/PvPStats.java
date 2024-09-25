package br.com.poison.core.resources.member.type.pvp.stats;

import br.com.poison.core.resources.member.type.pvp.stats.list.FpsStats;
import br.com.poison.core.resources.member.type.pvp.stats.list.ArenaStats;
import br.com.poison.core.resources.member.type.pvp.stats.list.LavaStats;
import br.com.poison.core.resources.member.type.pvp.stats.list.MlgStats;
import lombok.Getter;

@Getter
public class PvPStats {

    private final ArenaStats arena = new ArenaStats();
    private final FpsStats fps = new FpsStats();
    private final LavaStats lava = new LavaStats();
    private final MlgStats mlg = new MlgStats();
}
