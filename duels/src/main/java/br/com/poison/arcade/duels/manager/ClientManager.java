package br.com.poison.arcade.duels.manager;

import br.com.poison.arcade.duels.client.Client;
import br.com.poison.core.manager.base.Manager;

import java.util.UUID;

public class ClientManager extends Manager<UUID, Client> {

    @Override
    public void save(Client client) {
        getCacheMap().putIfAbsent(client.getProfile().getId(), client);
    }

    public Client fetch(String name) {
        return documents(client -> client.getProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}