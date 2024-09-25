package br.com.poison.core.bukkit.service.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@Getter
@RequiredArgsConstructor
public abstract class TaskHandler implements Runnable {

    private final Plugin plugin;

    public abstract void init();
}