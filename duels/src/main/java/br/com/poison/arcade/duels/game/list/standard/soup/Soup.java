package br.com.poison.arcade.duels.game.list.standard.soup;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.arena.Arena;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.arcade.duels.client.data.ClientData;
import br.com.poison.arcade.duels.client.data.stats.type.ClientStatsType;
import br.com.poison.arcade.duels.game.Game;
import br.com.poison.arcade.duels.manager.TagManager;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.team.Team;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.listener.loader.ignore.IgnoreEvent;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import br.com.poison.core.resources.member.type.duels.stats.list.DefaultStats;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.bukkit.BukkitUtil;
import br.com.poison.core.util.extra.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

@IgnoreEvent
public class Soup extends Game {

    public Soup(Duels instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.SOUP);

        setInfo(Arena.TeamInfoSidebar.PING);

        setWonInVoid(true);
        setAllowedBuild(true);

        setAllowedTeleportOnStarting(false);
        setAllowedRefreshInventory(false);
    }

    @Override
    public void loadSidebar(Client client) {
        Arena arena = client.getArena();
        ClientData data = client.getData();

        Sidebar sidebar = client.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lSOPA");

        sidebar.addRow("id", "§8" + arena.getDiscriminator());
        sidebar.blankRow();

        /* Partida não está em modo de jogo */
        if (!arena.hasCondition(RoomCondition.PLAYING)) {
            sidebar.addRow("map", "Mapa: ", "§a" + arena.getMap().getName());
            sidebar.addRow("players", "Jogadores: ", "§a" + arena.getPlayers().size() + "/" + arena.getMaxPlayers());
            sidebar.blankRow();

            sidebar.addRow("time", arena.hasCondition(RoomCondition.ENDING)
                            ? "Acaba em " : arena.hasCondition(RoomCondition.STARTING)
                            ? "Iniciando em "
                            : "Aguardando...",
                    !arena.hasCondition(RoomCondition.WAITING) ? "§e" + arena.getTimer() + "s" : "");
        } else {
            /* Partida em modo de jogo */
            sidebar.addRow("time", "Tempo: ", "§7" + TimeUtil.time(arena.getTimer()));
            sidebar.blankRow();

            arena.sendTeamInfoInSidebar(sidebar, Arena.TeamInfoSidebar.PING);
        }

        if (!client.isPlayer()) {
            if (!arena.hasCondition(RoomCondition.PLAYING))
                sidebar.blankRow();

            ClientStatsType stats = data.getStats().getType();

            sidebar.addRow("stats", String.format("%sMODO %s", stats.getColor(), stats.getName().toUpperCase()));
            sidebar.blankRow();

            sidebar.addRow("watch", "Assistindo: ", "§e" + arena.getSpectators().size());
        } else {
            if (!arena.hasCondition(RoomCondition.PLAYING))
                sidebar.blankRow();

            sidebar.addRow("streak", "Winstreak: ", "§7" + Util.formatNumber(client.getMember().getStats().getSimulator().getStreak()));
        }

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(client);
    }

    @Override
    public void updateSidebar(Client client) {
    }

    @Override
    public void updateTeamStatsAfterEnd(Team winner, List<Team> losers) {
        for (Player player : winner.getPlayers()) {
            if (player == null) continue;

            Client client = getClient().read(player.getUniqueId());

            if (client == null) continue;

            DuelMember member = client.getMember();

            DefaultStats soup = member.getStats().getSoup();

            soup.setWins(soup.getWins() + 1);
            soup.setStreak(soup.getStreak() + 1);

            if (soup.getStreak() > soup.getBestStreak())
                soup.setBestStreak(soup.getStreak());

            member.saveStats(member.getStats());
        }

        for (Team loser : losers) {
            if (loser == null || loser.getPlayers().isEmpty()) continue;

            for (Player player : loser.getPlayers()) {
                if (player == null) continue;

                Client client = getClient().read(player.getUniqueId());

                if (client == null) continue;

                DuelMember member = client.getMember();

                DefaultStats soup = member.getStats().getSoup();

                soup.setDefeats(soup.getDefeats() + 1);
                soup.setStreak(0);

                member.saveStats(member.getStats());
            }
        }

        super.updateTeamStatsAfterEnd(winner, losers);
    }

    @Override
    public void loadInventory(Player player) {
        Client client = getClient().read(player.getUniqueId());

        PlayerInventory inv = player.getInventory();

        inv.clear();
        inv.setArmorContents(null);

        if (client.isProtected()) {
            loadDefaultInventory(player);
        } else {
            BukkitUtil.makeRecraftInventory(player);

            inv.setItem(0, new ItemStack(Material.STONE_SWORD));
        }
    }
}
