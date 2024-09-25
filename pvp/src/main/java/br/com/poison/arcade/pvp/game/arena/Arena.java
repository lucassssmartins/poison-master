package br.com.poison.arcade.pvp.game.arena;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.room.map.Map;
import br.com.poison.core.arcade.room.Room;
import br.com.poison.core.arcade.room.condition.RoomCondition;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.manager.VanishManager;
import br.com.poison.core.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Arena extends Room {

    public Arena(int id, ArcadeGame<PvP> game, Map map, SlotCategory slot) {
        super(id, game, map, slot);

        setCondition(RoomCondition.PLAYING);
    }

    @Override
    public void join(Player player) {
        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        Profile profile = user.getProfile();

        Game game = (Game) getGame();

        game.sendSidebar(user);
        game.sendKit(player);

        game.addPlayer(player.getUniqueId());

        Location spawn = getLocation("spawn");

        if (spawn != null)
            player.teleport(spawn);

        joinMember(player, user.getRoute());

        hideAndShow(player);

        profile.setGame(user.getRoute());

        player.sendMessage("§aConectado em " + user.getRoute().getArenaId() + "§a!");
    }

    public void spawn(Player player) {
        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        user.setProtected(true);

        player.refresh();
        player.teleport(getLocation("spawn"));

        if (user.getPrimaryKit() != null || user.getSecondaryKit() != null)
            BukkitCore.getCooldownManager().resetCooldown(player);

        Game game = (Game) getGame();

        game.sendKit(player);
        game.updateSidebar(user);
    }

    protected void hideAndShow(Player player) {
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

            User user = PvP.getUserManager().read(target.getUniqueId());

            Profile profile = user.getProfile();

            if (!user.getArena().equals(this)) continue;

            if (!VanishManager.canNotSee(source, profile))
                player.showPlayer(target);

            target.showPlayer(player);
        }
    }
}
