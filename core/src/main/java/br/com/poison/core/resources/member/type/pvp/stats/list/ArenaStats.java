package br.com.poison.core.resources.member.type.pvp.stats.list;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArenaStats {

    private int kills, deaths;
    private int streak, bestStreak;
}
