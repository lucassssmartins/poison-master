package br.com.poison.core.arcade.room;

import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.map.SignedLocation;
import br.com.poison.core.arcade.room.map.rollback.RollbackBlock;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.member.RoomMember;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.arcade.team.Team;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.paperspigot.Title;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public abstract class Room {

    private final int id;
    private final String discriminator;

    private final ArcadeGame<?> game;

    private final Map map;

    private final Set<RoomMember> members;
    private final Set<RollbackBlock> rollbackBlocks;

    private SlotCategory slot;
    private RoomCondition condition;

    private Team winner;

    private Set<UUID> reservations;
    private List<Team> teams;

    private int timer, maxPlayers;

    private final long startedAt;

    public Room(int id, ArcadeGame<?> game, Map map, SlotCategory slot) {
        this.id = id;
        this.discriminator = id + "bn" + map.getId();

        this.game = game;

        this.map = map;

        this.slot = slot;
        this.condition = RoomCondition.WAITING;

        this.members = new HashSet<>();
        this.rollbackBlocks = new HashSet<>();

        this.teams = new ArrayList<>();
        this.reservations = new HashSet<>();

        this.maxPlayers = slot.getTotalPlayers();

        this.startedAt = System.currentTimeMillis();
    }

    public abstract void join(Player player);

    @Override
    public boolean equals(Object arena) {
        if (this == arena) return true;
        if (arena == null || getClass() != arena.getClass()) return false;

        Room room = (Room) arena;

        return room.getGame().equals(getGame()) && room.getId() == getId();
    }

    public World getWorld() {
        return Bukkit.getWorld(game.getName().toLowerCase() + "-" + map.getName().toLowerCase() + "-" + id);
    }

    /* Members */
    public Set<Player> getMembers(InputMode input) {
        return members.stream()
                .filter(member -> member.getRoute().getInput().equals(input))
                .map(RoomMember::getPlayer)
                .collect(Collectors.toSet());
    }

    public Set<Player> getPlayers() {
        return getMembers(InputMode.PLAYER);
    }

    public boolean isMember(UUID id) {
        return members.stream().anyMatch(member -> member.getPlayer().getUniqueId().equals(id));
    }

    public void joinMember(Player player, GameRouteContext route) {
        members.add(new RoomMember(player, route));
    }

    public void leaveMember(Player player) {
        members.removeIf(member -> member.getPlayer().getUniqueId().equals(player.getUniqueId()));
    }

    /* Other Methods */
    public boolean isReserved() {
        return !reservations.isEmpty();
    }

    public boolean hasReservation(UUID linker) {
        return reservations.contains(linker);
    }

    public String getMode() {
        return game.getName() + " " + slot.getTag();
    }

    public boolean hasSlot(SlotCategory slot) {
        return this.slot.equals(slot);
    }

    public void setTeams(Team... teams) {
        this.teams.addAll(Arrays.asList(teams));
    }

    public boolean hasCondition(RoomCondition condition) {
        return this.condition.equals(condition);
    }

    public boolean isAvailable() {
        return hasCondition(RoomCondition.WAITING) && !isReserved() && isNotFull();
    }

    public boolean isNotFull() {
        return getPlayers().size() < getMaxPlayers();
    }

    public List<Team> getTeams(Predicate<Team> filter) {
        return getTeams().stream().filter(filter).collect(Collectors.toList());
    }

    public boolean isFilledTeams() {
        if (teams.isEmpty()) return false;

        return getTeams().stream().allMatch(Team::isFull);
    }

    public boolean isTeamFilledAndAnotherNotAlone() {
        if (teams.isEmpty()) return false;

        int full = (int) teams.stream().filter(Team::isFull).count(),
                notFull = (int) teams.stream().filter(team -> !team.isFull() && !team.getPlayers().isEmpty()).count();

        return full == 1 && notFull >= 1;
    }

    public int getActualRound() {
        Team team = teams.stream().max(Comparator.comparingInt(Team::getScore)).orElse(null);

        int round = team != null ? team.getScore() + 1 : 1;

        return Math.min(round, getGame().getMaxScore());
    }

    public void loadRollback(Location location, RollbackBlock.RollbackType type) {
        rollbackBlocks.add(new RollbackBlock(location, type));
    }

    public boolean isRollback(Block block) {
        return getRollback(block) != null;
    }

    public RollbackBlock getRollback(Block block) {
        return rollbackBlocks.stream().filter(b -> block.getLocation().equals(b.getLocation())).findFirst().orElse(null);
    }

    public boolean hasLocation(String name) {
        return getMap().getSignedLocations().stream().anyMatch(signed -> signed.getTitle().equalsIgnoreCase(name));
    }

    public Location getLocation(String name) {
        return getMap().getLocation(name).getLocation().getBukkitLocation(getWorld());
    }

    public List<Location> getLocations(String... names) {
        List<Location> locations = new ArrayList<>();

        for (String name : names) {
            SignedLocation location = getMap().getLocation(name);

            if (location == null || location.getLocation() == null) continue;

            locations.add(location.getLocation().getBukkitLocation(getWorld()));
        }

        return locations;
    }

    public void sendMessage(String message) {
        getMembers().forEach(member -> member.getPlayer().sendMessage(message));
    }

    public void playSound(Sound sound) {
        getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, 2.0f, 2.0f));
    }

    public void sendTitle(String title, String subTitle) {
        getPlayers().forEach(player -> player.sendTitle(new Title(title, subTitle, 5, 40, 20)));
    }

    public void sendTitle(String title) {
        sendTitle(title, "");
    }
}
