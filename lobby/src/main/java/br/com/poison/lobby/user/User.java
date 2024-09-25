package br.com.poison.lobby.user;

import br.com.poison.lobby.Lobby;
import br.com.poison.core.Constant;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.profile.Profile;
import br.com.poison.lobby.game.Game;
import br.com.poison.lobby.game.arena.Arena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class User {

    private final Profile profile;

    private Sidebar sidebar;

    private Game game;
    private Arena arena;

    private GameRouteContext route;

    private boolean visiblePlayers = true;

    public User(Profile profile, Game game, Arena arena, GameRouteContext route) {
        this.profile = profile;

        this.game = game;
        this.arena = arena;

        this.route = route;

        Lobby.getUserManager().save(this);
    }

    public void load() {
        Player player = profile.player();

        this.sidebar = new Sidebar(player, Constant.SERVER_NAME.toUpperCase());

        arena.join(player);
    }

    public boolean inArcade(ArcadeCategory arcade) {
        return game.getCategory().equals(arcade);
    }
}
