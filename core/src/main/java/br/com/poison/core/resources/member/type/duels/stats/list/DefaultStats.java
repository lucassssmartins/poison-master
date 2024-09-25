package br.com.poison.core.resources.member.type.duels.stats.list;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultStats {

    private int wins, defeats;

    private int streak, bestStreak;

    public int getMatches() {
        return wins + defeats;
    }

    public String getWinAverage() {
        if (getMatches() == 0) return "0";

        double winPercentage = ((double) wins / getMatches()) * 100.0;

        return winPercentage % 1 == 0 ? String.valueOf((int) winPercentage) : String.valueOf(Math.round(winPercentage * 10.0) / 10.0);
    }
}
