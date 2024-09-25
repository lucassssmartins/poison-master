package br.com.poison.core.arcade.route;

import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.server.category.ServerCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
public final class GameRouteContext {

    private final int id;

    private final ArcadeCategory arcade;

    private final SlotCategory slot;
    private final InputMode input;

    private Set<UUID> link;

    private final int map, slots;

    public String getArenaId() {
        return id + "zf" + map;
    }

    @Override
    public boolean equals(Object routeObj) {
        if (this == routeObj) return true;
        if (routeObj == null || getClass() != routeObj.getClass()) return false;

        GameRouteContext route = (GameRouteContext) routeObj;

        return route.getArcade().equals(arcade) && route.getId() == id && route.getServer().equals(getServer());
    }

    public boolean isValid() {
        return arcade != null && slot != null;
    }

    public boolean hasLink() {
        return link != null && !link.isEmpty();
    }

    public ServerCategory getServer() {
        return arcade.getServer();
    }
}