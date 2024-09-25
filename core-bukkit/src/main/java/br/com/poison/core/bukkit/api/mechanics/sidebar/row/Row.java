package br.com.poison.core.bukkit.api.mechanics.sidebar.row;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

@Getter
@Setter
@RequiredArgsConstructor
public class Row {

    private final String tag;
    private final String prefix, suffix;

    private Score score;
    private Team team;
}
