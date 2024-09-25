package br.com.poison.arcade.duels.client;

import br.com.poison.arcade.duels.game.Game;
import br.com.poison.arcade.duels.arena.Arena;
import br.com.poison.arcade.duels.client.data.ClientData;
import br.com.poison.arcade.duels.client.data.stats.ClientStats;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.arcade.team.Team;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
@Setter
@RequiredArgsConstructor
public class Client {

    private final Profile profile;
    private final DuelMember member;

    private final ClientData data;

    private GameRouteContext route;
    private Arena arena;

    private Team team;
    private Sidebar sidebar;

    private Tag tag;

    private boolean validRoute = false;

    public void load(Player player) {
        this.sidebar = new Sidebar(player, getGame().getCategory().name().replace("_", " "));

        if (arena != null)
            arena.join(player);
    }

    public void setStats(ClientStatsType type) {
        ClientStats stats = data.getStats();

        stats.setType(type);
        stats.setDuration(type.getDuration());
    }

    public void resurface() {
        Player player = profile.player();

        if (player != null) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            player.setGameMode(GameMode.SURVIVAL);

            player.setAllowFlight(true);
            player.setFlying(true);

            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 45, 3));

            player.playSound(Sound.BLAZE_DEATH);

            setStats(ClientStatsType.RESURFACING);
        }
    }

    public void died() {
        Player player = profile.player();

        if (player != null) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            player.setGameMode(GameMode.SURVIVAL);

            player.setAllowFlight(true);
            player.setFlying(true);

            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 45, 3));

            player.playSound(Sound.BLAZE_DEATH);

            setStats(ClientStatsType.DIED);
        }
    }

    public boolean inArena(Arena arena) {
        return this.arena.equals(arena);
    }

    public boolean isProtected() {
        return !arena.hasCondition(RoomCondition.PLAYING);
    }

    public boolean isPlayer() {
        return data.isStats(ClientStatsType.PLAYER);
    }

    public boolean isSameGame(Client consumer) {
        return getGame().isCategory(consumer.getGame().getCategory()) && inArena(consumer.getArena());
    }

    public boolean isSameTeam(Client consumer) {
        return team != null && team.getName().equalsIgnoreCase(consumer.getTeam().getName());
    }

    public Game getGame() {
        return (Game) arena.getGame();
    }

    public String getChatName() {
        return (isPlayer()
                ? team.getPrefix() + " " + profile.getTag().getPrefix() + profile.getName()
                : getColoredName())
                + ": §f";
    }

    public String getColoredName() {
        return isPlayer()
                ? team.getColor() + profile.getName()
                : data.getStats().getType().getColor() + profile.getName();
    }

    public ChatColor getTeamColor() {
        return team.getColor();
    }
}
