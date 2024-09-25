package br.com.poison.core.arcade.team;

import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class Team {

    private final ArcadeGame<?> holder;

    private final String name, tag;
    private final ChatColor color;

    private Location location;

    private final Set<Player> players = new HashSet<>();

    private final int maxPlayers;


    private int score;

    public void reset() {
        score = 0;
        players.clear();
    }

    public Set<Profile> getProfiles() {
        return Core.getProfileManager()
                .documents(profile -> players.stream().anyMatch(player -> profile.getId().equals(player.getUniqueId())))
                .collect(Collectors.toSet());
    }

    public String getPrefix() {
        return color + "[" + tag + "]";
    }

    public String getColoredTag() {
        return color + tag;
    }

    public String getColoredName() {
        return color + name;
    }

    public String getBoldColorName() {
        return color + "§l" + name;
    }

    public boolean isReachedMaxScore() {
        return holder.isScoredGame() && score >= holder.getMaxScore();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
}
