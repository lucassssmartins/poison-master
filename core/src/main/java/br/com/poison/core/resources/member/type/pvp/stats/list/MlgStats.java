package br.com.poison.core.resources.member.type.pvp.stats.list;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MlgStats {

    private int wins, defeats;
    private int streak, bestStreak;
}
