package br.com.poison.core.util.bukkit;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.io.File;
import java.util.Set;

public class WorldUtil {

    public static void setup(World world) {
        world.setPVP(true);

        world.setDifficulty(Difficulty.HARD);

        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("sendCommandFeedback", "false");
        world.setGameRuleValue("logAdminCommands", "false");

        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MIN_VALUE);
        world.setThunderDuration(Integer.MIN_VALUE);

        world.setSpawnLocation(100, 50, 100);
        world.setAutoSave(false);
        world.setTime(6000);

        world.getEntities().forEach(Entity::remove);
    }

    public static void setup(World world, Set<Chunk> chunks) {
        setup(world);

        chunks.forEach(Chunk::load);
    }
}
