package br.com.poison.lobby.game.arena;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.Room;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.bukkit.manager.VanishManager;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.lobby.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Arena extends Room {

    private final List<String> SUB_TITLES = Arrays.asList(
            "§fBem-vindo!"
    );

    public Arena(int id, ArcadeGame<Lobby> game, Map map, SlotCategory slot) {
        super(id, game, map, slot);

        setCondition(RoomCondition.PLAYING);
        setMaxPlayers(60);
    }

    @Override
    public void join(Player player) {
        User user = Lobby.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        Game game = (Game) getGame();

        if (game.load(user)) {
            Profile profile = user.getProfile();

            Tag tag = profile.getTag();

            joinMember(player, user.getRoute());

            Location spawn = getLocation("spawn");

            if (profile.isVIP()) {
                if (profile.getPreference().isJoinMessage() && !tag.equals(Tag.PLAYER))
                    sendMessage(tag.getPrefix() + profile.getName() + "§6 entrou no lobby!");

                if (profile.getPreference().isFlyingLobby()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);

                    spawn = spawn.clone().add(0.0, 2.0, 0.0);
                }
            }

            if (spawn != null)
                player.teleport(spawn);

            hideAndShow(player);

            profile.setGame(user.getRoute());

            profile.sendTitle(Constant.SERVER_TITLE, SUB_TITLES.get(Core.RANDOM.nextInt(SUB_TITLES.size())));
        } else
            player.kickPlayer(Constant.PROFILE_CREATE_FAILED);
    }

    protected synchronized void hideAndShow(Player player) {
        Profile source = Core.getProfileManager().read(player.getUniqueId());

        // Escondendo todos os jogadores para o player.
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == null || target.equals(player) || !player.canSee(target)) continue;

            player.hidePlayer(target);
            target.hidePlayer(player);
        }

        // Mostrando apenas os jogadores necessários.
        for (Player target : getPlayers()) {
            if (target == null || !target.isOnline() || player.canSee(target)) continue;

            User user = Lobby.getUserManager().read(target.getUniqueId());

            Profile profile = user.getProfile();

            if (!user.getArena().equals(this)) continue;

            if (!VanishManager.canNotSee(source, profile))
                player.showPlayer(target);

            target.showPlayer(player);
        }
    }
}
