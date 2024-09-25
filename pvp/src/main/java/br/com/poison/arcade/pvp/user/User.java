package br.com.poison.arcade.pvp.user;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.game.arena.Arena;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.user.lava.LavaLevel;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.pvp.PvPMember;
import br.com.poison.core.resources.member.type.pvp.stats.list.LavaStats;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
@Setter
public class User {

    private final Profile profile;
    private final PvPMember member;

    private Sidebar sidebar;

    private Game game;
    private Arena arena;

    private GameRouteContext route;

    private Kit primaryKit, secondaryKit;

    private boolean isProtected = true, onLauncher = false;

    private long combatExpiresAt = Long.MIN_VALUE;

    public User(Profile profile, PvPMember member, Game game, Arena arena, GameRouteContext route) {
        this.profile = profile;
        this.member = member;

        this.game = game;
        this.arena = arena;

        this.route  = route;

        PvP.getUserManager().save(this);
    }

    @Override
    public boolean equals(Object userObj) {
        if (this == userObj) return true;
        if (userObj == null || getClass() != userObj.getClass()) return false;

        User user = (User) userObj;

        return user.getProfile().getId().equals(profile.getId());
    }

    public void load() {
        Player player = profile.player();

        this.sidebar = new Sidebar(player, game.getName().toUpperCase());

        arena.join(player);
    }

    public void removeProtection(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        setProtected(false);

        game.sendKit(player);

        player.sendMessage("§cVocê perdeu a sua proteção!");
        player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 2.0f, 2.0f);
    }

    public void completeLava(LavaLevel level) {
        // Adicionando estatísticas
        LavaStats lava = member.getStats().getLava();

        int coins = 0;

        switch (level) {
            case EASY: {
                coins = Core.RANDOM.ints(40, 90).findFirst().orElse(30);

                lava.setEasy(lava.getEasy() + 1);
                break;
            }
            case MEDIUM: {
                coins = Core.RANDOM.ints(50, 120).findFirst().orElse(40);

                lava.setMedium(lava.getMedium() + 1);
                break;
            }
            case HARD: {
                coins = Core.RANDOM.ints(75, 160).findFirst().orElse(60);

                lava.setHard(lava.getHard() + 1);
                break;
            }
            case EXTREME: {
                coins = Core.RANDOM.ints(115, 300).findFirst().orElse(80);

                lava.setExtreme(lava.getExtreme() + 1);
                break;
            }
        }

        member.saveStats(member.getStats());

        member.addCoins(coins);

        profile.sendMessage(level.getPrefix() + level.getColor() + ": §fVocê passou o Lava " + level.getColoredName() + "§f!",
                "§6+" + coins + " Coins!");

        profile.sendSound(Sound.LEVEL_UP);

        arena.spawn(profile.player());
    }

    public boolean isUsingKit(String name) {
        return (primaryKit != null && primaryKit.getName().equalsIgnoreCase(name)
                || secondaryKit != null && secondaryKit.getName().equalsIgnoreCase(name));
    }

    public void applyCombat() {
        combatExpiresAt = System.currentTimeMillis() + 10000L;
    }

    public boolean hasCombat() {
        if (combatExpiresAt <= System.currentTimeMillis())
            combatExpiresAt = Long.MIN_VALUE;

        return combatExpiresAt >= System.currentTimeMillis();
    }
}
