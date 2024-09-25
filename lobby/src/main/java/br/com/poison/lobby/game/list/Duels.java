package br.com.poison.lobby.game.list;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.game.Game;
import br.com.poison.lobby.game.arena.Arena;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.api.mechanics.npc.type.server.NpcServer;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.league.League;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import br.com.poison.core.resources.member.type.duels.stats.DuelStats;
import br.com.poison.core.util.Util;
import br.com.poison.lobby.inventory.tracker.mode.ModeSearchedInventory;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Duels extends Game {

    public Duels(Lobby instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.DUELS);
    }

    @Override
    public boolean load(User user) {
        Profile profile = user.getProfile();

        Player player = profile.player();

        DuelMember member = Core.getDuelData().fetch(player.getUniqueId(), true);

        if (member == null)
            member = Core.getDuelData().input(profile);

        Core.getDuelData().persist(profile.getId());
        Core.getDuelManager().save(member);

        sendSidebar(user);
        sendHotbar(user);

        addPlayer(player.getUniqueId());

        return true;
    }

    @Override
    public void leave(User user) {
        Core.getDuelData().cache(user.getProfile().getId());

        removePlayer(user.getProfile().getId());
    }

    @Override
    public void sendSidebar(User user) {
        DuelMember member = Core.getDuelManager().read(user.getProfile().getId());

        DuelStats stats = member.getStats();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lDUELS");
        sidebar.blankRow();

        sidebar.addRow("text", "§7Desafie usando");
        sidebar.addRow("text2", "§7/duelar <jogador>.");

        sidebar.blankRow();
        sidebar.addRow("players", "Players: ", "§a" + Core.getServerManager().getTotalPlayers());

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagController.updateTag(sidebar.getOwner());
    }

    @Override
    public void initEntities(Arena arena) {
        List<Location> locations = arena.getLocations("npc_simulator", "npc_soup", "npc_gladiator", "npc_uhc", "npc_nodebuff", "npc_sumo");

        if (locations.isEmpty()) return;

        // Init NPCs

        makeEntity(ArcadeCategory.SIMULATOR, locations.get(0),
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        makeEntity(ArcadeCategory.SOUP, locations.get(1),
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        makeEntity(ArcadeCategory.GLADIATOR, locations.get(2),
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        makeEntity(ArcadeCategory.UHC, locations.get(3),
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        makeEntity(ArcadeCategory.NODEBUFF, locations.get(4),
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        makeEntity(ArcadeCategory.SUMO, locations.get(5),
                "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");
    }

    protected void makeEntity(ArcadeCategory category, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getNpcManager().spawnServer(location, new Property("textures", value, signature));

        npc.setAction((player, action) -> new ModeSearchedInventory(player, null, category, null).init());

        npc.setHand(new Item(category.getIcon()));

        npc.display();

        HologramServer hologram = BukkitCore.getHologramManager().spawnServer(category.getName().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§b" + category.getName(),
                "§e" + Util.formatNumber(category.getPlayingNow()) + " jogando."
        ));
    }
}
