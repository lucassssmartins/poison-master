package br.com.poison.core.arcade.room.member;

import br.com.poison.core.arcade.route.GameRouteContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class RoomMember {
    private final Player player;
    private final GameRouteContext route;
}