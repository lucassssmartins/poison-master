package br.com.poison.core.bukkit.api.user.cooldown.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
public class Cooldown {

    @Setter
    private String name;

    private long duration;
    private long startTime = System.currentTimeMillis();

    public Cooldown(String name, long duration) {
        this.name = name;
        this.duration = duration;
    }

    public void update(long duration, long startTime) {
        this.duration = duration;
        this.startTime = startTime;
    }

    public double getRemaining() {
        long endTime = startTime + TimeUnit.SECONDS.toMillis(duration);
        return (-(System.currentTimeMillis() - endTime)) / 1000D;
    }

    public boolean expired() {
        return getRemaining() < 0D;
    }

    public double getPercentage() {
        return (getRemaining() * 100) / duration;
    }
}
