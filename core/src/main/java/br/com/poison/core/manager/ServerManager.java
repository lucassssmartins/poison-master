package br.com.poison.core.manager;

import br.com.poison.core.server.Server;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.Core;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class ServerManager {

    public void init(int maxPlayers) {
        input(new Server(Core.getServerCategory(), port(), maxPlayers));
    }

    public void end() {
        Server server = getLocalServer();

        if (server != null)
            delete(server);
    }

    public void update(int players) {
        Server server = getLocalServer();

        if (server != null)
            server.update(players);
    }

    public abstract int port();

    protected final String SERVER_ENTRY_KEY = "server:";

    public void input(Server server) {
        Core.getRedisDatabase().save(SERVER_ENTRY_KEY + server.getPort(), server);
    }

    public void delete(Server server) {
        Core.getRedisDatabase().delete(SERVER_ENTRY_KEY + server.getPort());
    }

    public void update(Server server) {
        Core.getRedisDatabase().update(SERVER_ENTRY_KEY + server.getPort(), server);
    }

    public List<Server> getServers() {
        return Core.getRedisDatabase().loadAll(SERVER_ENTRY_KEY, Server.class);
    }

    public List<Server> getServers(Predicate<Server> filter) {
        return getServers().stream().filter(filter).collect(Collectors.toList());
    }

    public Server getLocalServer() {
        return getServer(port());
    }

    public Server getServer(int port) {
        return Core.getRedisDatabase().load(SERVER_ENTRY_KEY + port, Server.class);
    }

    public Server getServer(ServerCategory category) {
        return getServers(server -> server.getCategory() != null && server.getCategory().equals(category))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public int getTotalCategories(ServerCategory category) {
        return getServers(server -> server.getCategory() != null && server.getCategory().equals(category)).size();
    }

    public int getTotalPlayers() {
        return getServer(ServerCategory.PROXY).getPlayers();
    }

    public int getTotalPlayers(ServerCategory category) {
        return sum(getServers(server -> server.getCategory() != null && server.getCategory().equals(category))
                .stream()
                .map(Server::getPlayers).collect(Collectors.toList()));
    }

    public int getTotalPlayers(ServerCategory... categories) {
        List<ServerCategory> serverList = Arrays.asList(categories);

        return sum(getServers(server -> server.getCategory() != null && serverList.contains(server.getCategory()))
                .stream()
                .map(Server::getPlayers).collect(Collectors.toList()));
    }

    public int sum(List<Integer> ints) {
        int count = 0;

        if (ints.isEmpty()) {
            return 0;
        }

        for (int anInt : ints) {
            count += anInt;
        }

        return count;
    }

}
