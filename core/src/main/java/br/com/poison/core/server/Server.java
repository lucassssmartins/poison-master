package br.com.poison.core.server;

import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.Core;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class Server {

    private final ServerCategory category;

    private final int id;

    private int players;
    private final int port, maxPlayers;

    private boolean enabled;

    private final long startedAt;

    private final Map<String, Object> properties;

    public Server(ServerCategory category, int port, int maxPlayers) {
        this.category = category;

        this.id = Core.getServerManager().getTotalCategories(category) + 1;
        this.port = port;

        this.players = 0;
        this.maxPlayers = maxPlayers;

        this.enabled = true;

        this.startedAt = System.currentTimeMillis();

        this.properties = new HashMap<>();
    }

    /**
     * Atualizar as informações do servidor.
     */
    public void update(int players) {
        setPlayers(players);

        Core.getServerManager().update(this);
    }

    public boolean isArcade() {
        return category.isAllowArcade();
    }

    public String getName() {
        return category.getName();
    }

    public String getServerId() {
        return Core.getServerManager().getTotalCategories(category) > 1 ? category.getId(id) : category.getId();
    }

    public ServerInfo getServerInfo() {
        return ProxyServer.getInstance().getServerInfo(getServerId());
    }

    public void writeProperty(String key, Object value) {
        properties.put(key.toLowerCase(), value);
    }

    public Object getProperty(String key) {
        return properties.get(key.toLowerCase());
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key.toLowerCase());
    }
}
