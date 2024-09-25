package br.com.poison.lobby.game.list;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.game.arena.Arena;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.api.mechanics.npc.type.server.NpcServer;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.resources.member.type.pvp.stats.PvPStats;
import br.com.poison.core.util.Util;
import br.com.poison.lobby.game.Game;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class PvP extends Game {

    public PvP(Lobby instance, Integer minRooms, Integer maxRooms, String mapsDirectory) {
        super(instance, minRooms, maxRooms, mapsDirectory, ArcadeCategory.PVP);
    }

    @Override
    public boolean load(User user) {
        PvPMember member = Core.getPvpData().fetch(user.getProfile().getId(), true);

        if (member == null)
            member = Core.getPvpData().input(user.getProfile());

        Core.getPvpData().persist(user.getProfile().getId());
        Core.getPvpManager().save(member);

        sendSidebar(user);
        sendHotbar(user);

        addPlayer(member.getId());

        return true;
    }

    @Override
    public void leave(User user) {
        removePlayer(user.getProfile().getId());

        Core.getPvpData().cache(user.getProfile().getId());
    }

    @Override
    public void sendSidebar(User user) {
        PvPMember member = Core.getPvpManager().read(user.getProfile().getId());

        PvPStats stats = member.getStats();

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lPVP");
        sidebar.blankRow();

        sidebar.addRow("text", "§7Selecione um jogo!");

        sidebar.blankRow();
        sidebar.addRow("players", "Players: ", "§a" + Util.formatNumber(Core.getServerManager().getTotalPlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagController.updateTag(sidebar.getOwner());
    }

    @Override
    public void initEntities(Arena arena) {
        List<Location> locations = arena.getLocations("npc_arena", "npc_fps", "npc_lava");

        if (locations.isEmpty()) return;

        handleEntity(ArcadeCategory.ARENA, locations.get(0), "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        handleEntity(ArcadeCategory.FPS, locations.get(1), "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");

        handleEntity(ArcadeCategory.LAVA, locations.get(2), "ewogICJ0aW1lc3RhbXAiIDogMTcwOTQwOTM4NDc4MSwKICAicHJvZmlsZUlkIiA6ICIwMzAwZWY4YjJkYmM0ZGM3YmYzNGUzN2I3MGEwNGZkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXNzaW9NYXJ0aW0iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZiNWVmY2VmNjY4N2UyMGJkM2Y4NzRmMjc2ZGUxMjQ2OTAyMzQ4ZTVhYWY1MWUxMzBlMjkwZTcxZGI2MjU1MiIKICAgIH0KICB9Cn0=",
                "LndbFjn1SQf4mosZJ7XUjDmeG3hjE2v6zk86AaWKaVo/AsgjeOjXZYs2Gsa/oYtiFeEB560bJ/Wra2BDUDTckTyB+5WlzTbrhLbF9yZBEojzvdqLhM/kqzEem44D4aiFv2ei90Gj1rXFHkNh/ErzNn5Jt6ar6a8HPnWyuWNAOTiKjUGe4z9mwRJVh0E0sv4BtJ19lQq12FKyAZjQ5k7o8cHUNHtzX6az1H5vZ1PzbUVbuLq3t6k5auKMoLDm2Rl4MfXOHU4u+3u8JQ6OaC1e13IS1xRASI5ddzMSZ6wm8hPTgz1BS6wr/pt2vBd4/yc2Hf1hP8hDzIZkS55pYtjCu0MCSXHit+1mL1cUD8yaCrYhoYDZfDd/Z2+pSMPdTxCVCSmm9LZIFvhfVvOxKMLUVeXsaNJYUf5Ji48aCxaljpB8GA6kIIyOIw1RcbMQOXI3zQzEZPh7Re2mKVSbSifhL2RS5aYbDXnTe81xdFHrvOiXud5K8h5jlqih5UnWfzSEVi+jSOK8OW6uLFVJk1juxFlAKtk1ISVK/KiaKB1Cd7OCOwgvjFBtaf4PS51PK0eakDt+GVbhcYws1B3UfykMeJMKiqRW+M4tlhw855LZtQpG4/aLgdsoVy72td7E04LpWbRT3edj4BpBIBBpt5Fuazvrz9tLt8po6wA3foGz8Ls=");
    }

    protected void handleEntity(ArcadeCategory category, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getNpcManager().spawnServer(location, new Property("textures", value, signature));

        npc.setAction((player, action) -> {
            Profile profile = Core.getProfileManager().read(player.getUniqueId());

            if (profile != null)
                profile.redirect(category, SlotCategory.VOID);
        });

        npc.display();

        HologramServer hologram = BukkitCore.getHologramManager().spawnServer(category.getName().toLowerCase(), location);

        hologram.setText(Arrays.asList(
                "§b" + category.getName(),
                "§e0 jogando."
        ));
    }
}
