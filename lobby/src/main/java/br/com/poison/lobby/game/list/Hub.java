package br.com.poison.lobby.game.list;

import br.com.poison.lobby.Lobby;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.api.mechanics.npc.type.server.NpcServer;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.Util;
import br.com.poison.lobby.game.Game;
import br.com.poison.lobby.game.arena.Arena;
import br.com.poison.lobby.user.User;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Hub extends Game {

    public Hub(Lobby instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.HUB);
    }

    @Override
    public boolean load(User user) {
        sendSidebar(user);
        sendHotbar(user);

        Profile profile = user.getProfile();

        addPlayer(profile.getId());

        return true;
    }

    @Override
    public void leave(User user) {
        removePlayer(user.getProfile().getId());
    }

    @Override
    public void sendSidebar(User user) {
        Profile profile = user.getProfile();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§l" + Constant.SERVER_NAME.toUpperCase());

        sidebar.blankRow();
        sidebar.addRow("rank", "Rank: ", profile.getRank().getPrefix());

        sidebar.blankRow();
        sidebar.addRow("players", "Players: ", "§a" + Util.formatNumber(Core.getServerManager().getTotalPlayers()));
        sidebar.addRow("lobby", "Lobby: ", "§7#" + user.getArena().getId());

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagController.updateTag(sidebar.getOwner());
    }

    @Override
    public void initEntities(Arena arena) {
        Location duelsLocation = arena.getLocation("npc_duels"),
                pvpLocation = arena.getLocation("npc_pvp");

        if ((duelsLocation == null || pvpLocation == null)) {
            Core.getLogger().info("Não foi possível encontrar a localização dos NPCs!");
            return;
        }

        // Duels
        NpcServer duels = handleCharacter(ServerCategory.DUELS, ArcadeCategory.DUELS, duelsLocation,
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=",
                true);

        // PvP
        NpcServer pvp = handleCharacter(ServerCategory.PVP, ArcadeCategory.PVP, pvpLocation,
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=",
                false);
    }

    protected NpcServer handleCharacter(ServerCategory server, ArcadeCategory arcade, Location location, String value, String signature, boolean emphasis) {
        NpcServer npc = BukkitCore.getNpcManager().spawnServer(location, new Property("textures", value, signature));

        npc.setAction((player, action) -> {
            User user = Lobby.getUserManager().read(player.getUniqueId());

            if (user == null) return;

            if (arcade != null)
                Lobby.getGameManager().redirect(user, arcade);
            else
                user.getProfile().redirect(server);
        });

        npc.display();

        List<String> text = new ArrayList<>();

        if (emphasis) {
            location = location.clone().add(0, 0.3, 0);

            text.add("§6§lNOVIDADE!");
        }

        text.add("§b" + server.getName());

        int playingNow = Core.getServerManager().getTotalPlayers(server) + getGameManager().getTotalPlayers(server);

        text.add("§e" + Util.formatNumber(playingNow) + " jogando.");

        HologramServer hologram = BukkitCore.getHologramManager().spawnServer(server.getName().toLowerCase(), location);

        hologram.setText(text);

        return npc;
    }

}
