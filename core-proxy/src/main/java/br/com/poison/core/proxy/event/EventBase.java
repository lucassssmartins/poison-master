package br.com.poison.core.proxy.event;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;

public class EventBase extends Event {

    public void call() {
        ProxyServer.getInstance().getPluginManager().callEvent(this);
    }
}
