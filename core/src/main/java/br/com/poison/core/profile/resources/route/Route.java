package br.com.poison.core.profile.resources.route;

import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.server.category.ServerCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Route {

    private ServerCategory server = ServerCategory.PROXY;
    private ServerCategory lastServer = ServerCategory.PROXY;

    private GameRouteContext game = GameRouteContext.builder().build();

    private long entryAt = System.currentTimeMillis();

    public void update(ServerCategory category, GameRouteContext game) {
        this.lastServer = server;
        this.server = category;

        this.game = game;

        this.entryAt = System.currentTimeMillis();
    }

    public boolean hasDefinedGame() {
        return game != null && game.isValid();
    }
}