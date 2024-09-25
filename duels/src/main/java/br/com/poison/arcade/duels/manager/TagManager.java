package br.com.poison.arcade.duels.manager;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TagManager {

    public static void updateTag(Client client) {
        Player player = client.getProfile().player();

        Tag tag = client.getTag();

        String order = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        String prefix = tag.getPrefix();
        if (prefix.length() > 16) prefix = prefix.substring(0, 16);

        // Removendo times antigos
        for (Team old : player.getScoreboard().getTeams()) {
            if (old.getName().startsWith("tag:") && old.hasEntry(player.getName()))
                old.unregister();
        }

        Team team = createTeamIfNotExists(order, player, player.getName(), prefix, "");

        Scoreboard scoreboard = team.getScoreboard();

        player.setDisplayName(team.getPrefix() + player.getName() + team.getSuffix());
        player.setPlayerListName(team.getPrefix() + player.getName() + team.getSuffix());

        player.setScoreboard(scoreboard);

        // Agora, vamos adicionar para todos os outros jogadores.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            Client target = Duels.getClientManager().read(onlinePlayer.getUniqueId());
            if (target == null) continue;

            Tag profileTag = target.getTag();
            if (profileTag == null) profileTag = Tag.PLAYER;

            String userOrder = "tag:" + profileTag.getOrder() + ":" + onlinePlayer.getEntityId();

            String userPrefix = profileTag.getPrefix();
            if (userPrefix.length() > 16) userPrefix = userPrefix.substring(0, 16);

            // Removendo times antigos
            for (Team old : scoreboard.getTeams()) {
                if (old.getName().startsWith("tag:") && old.hasEntry(onlinePlayer.getName()))
                    old.unregister();
            }

            // Aplicando times
            createTeamIfNotExists(userOrder, player, onlinePlayer.getName(), userPrefix, "");

            createTeamIfNotExists(order, onlinePlayer, player.getName(), team.getPrefix(), team.getSuffix());
        }
    }

    private static Team createTeamIfNotExists(String order, Player player, String entry, String prefix, String suffix) {
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard()))
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team team = scoreboard.getTeam(order);
        if (team == null) team = scoreboard.registerNewTeam(order);

        team.setCanSeeFriendlyInvisibles(false);

        team.setPrefix(prefix);
        team.setSuffix(suffix);

        if (!team.hasEntry(entry))
            team.addEntry(entry);

        return team;
    }
}
