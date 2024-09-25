package br.com.poison.lobby.manager;

import br.com.poison.lobby.user.User;
import br.com.poison.core.manager.base.Manager;

import java.util.UUID;

public class UserManager extends Manager<UUID, User> {

    @Override
    public void save(User user) {
        getCacheMap().put(user.getProfile().getId(), user);
    }
}
