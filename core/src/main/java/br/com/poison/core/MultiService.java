package br.com.poison.core;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public interface MultiService {

    /* Schedule Holders */
    void async(Runnable runnable);

    void sync(Runnable runnable);

    void syncLater(Runnable runnable, long ticks);

    /* User Holders */
    <T> T getPlayer(UUID id, Class<T> tClass);

    <T> T getPlayer(String name, Class<T> tClass);

    /* Message Holder */
    void broadcast(String message, boolean prefix);

    void broadcast(String message);

    void sendMessage(UUID id, String message);

    void sendMessage(UUID id, String... message);

    void sendMessage(UUID id, BaseComponent message);

    void sendMessage(UUID id, BaseComponent... message);
}
