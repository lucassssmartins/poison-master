package br.com.poison.core.resources.invitation.type;

import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.resources.invitation.Invitation;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DuelInvitation extends Invitation {

    private final GameRouteContext route;

    public DuelInvitation(GameRouteContext route, UUID sender, UUID target, int expireSeconds) {
        super(sender, target, expireSeconds);

        this.route = route;
    }
}
