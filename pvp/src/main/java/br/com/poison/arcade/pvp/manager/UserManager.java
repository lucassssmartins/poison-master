package br.com.poison.arcade.pvp.manager;

import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.manager.base.Manager;

import java.util.UUID;

public class UserManager extends Manager<UUID, User> {

    @Override
    public void save(User user) {
        getCacheMap().put(user.getProfile().getId(), user);
    }

    public User fetch(String name) {
        return documents(user -> user.getProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}