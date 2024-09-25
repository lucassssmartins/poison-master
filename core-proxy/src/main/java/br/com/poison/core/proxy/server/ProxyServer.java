package br.com.poison.core.proxy.server;

import br.com.poison.core.manager.ServerManager;

public class ProxyServer extends ServerManager {

    @Override
    public int port() {
        return 25565;
    }
}
