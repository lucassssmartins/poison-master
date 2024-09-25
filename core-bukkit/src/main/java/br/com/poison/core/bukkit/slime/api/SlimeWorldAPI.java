package br.com.poison.core.bukkit.slime.api;

import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.commands.CommandManager;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.backend.database.DatabaseCredentials;
import br.com.poison.core.bukkit.slime.action.SlimeAction;
import br.com.poison.core.bukkit.slime.loader.HikariSqlLoader;
import br.com.poison.core.util.bukkit.WorldUtil;
import lombok.SneakyThrows;
import org.bukkit.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SlimeWorldAPI {

    public static void registerHikari(String user, String database, String password) {
        SWMPlugin.getInstance().registerLoader(Constant.SLIME_HIKARI_SQL_LOADER, new HikariSqlLoader(
                DatabaseCredentials.builder()
                        .host("127.0.0.1")
                        .port(3306)
                        .user("root")
                        .database(database)
                        .build()
        ));
    }

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String loaderName, String templateWorld, String worldName) {
        cloneWorldFromTemplate(plugin, loaderName, templateWorld, worldName, () -> {
        });
    }

    public static void cloneWorldFromTemplate(SWMPlugin plugin, String loaderName, String template, String name,
                                              SlimeAction afterGenerate) {
        SlimeLoader loader = plugin.getLoader(loaderName);

        if (loader == null) {
            plugin.getLogger().info("Loader \"" + loaderName + "\" não encontrado.");
            return;
        }

        WorldsConfig config = ConfigManager.getWorldConfig();

        WorldData worldData = config.getWorlds().get(template);

        if (worldData == null) {
            plugin.getLogger().info("Mundo \"" + template + "\" não encontrado.");
            return;
        }

        if (template.equalsIgnoreCase(name)) {
            plugin.getLogger().info("Os mundos não podem ter os nomes iguais. (" + template + "/" + name + ")");
            return;
        }

        HikariSqlLoader hikariLoader = (HikariSqlLoader) plugin.getLoader(Constant.SLIME_HIKARI_SQL_LOADER);

        if (hikariLoader == null) {
            plugin.getLogger().info("A source do Hikari não foi encontrada!");
            return;
        }

        CommandManager.getInstance().getWorldsInUse().add(name);

        // Gerar mundo
        plugin.getLogger().info("Gerando mundo \"" + name + "\" de \"" + template + "\"...");

        CompletableFuture.runAsync(() -> {
            try {
                Instant start = Instant.now();

                /* Configurando Mapa */
                WorldData data = new WorldData();

                data.setSpawn("100, 100, 100");
                data.setDataSource(Constant.SLIME_HIKARI_SQL_LOADER);
                data.setDifficulty("hard");
                data.setEnvironment("normal");

                data.setAllowAnimals(false);
                data.setAllowMonsters(false);
                data.setPvp(true);

                SlimeWorld world = plugin.loadWorld(loader, template, true, data.toPropertyMap())
                        .clone(name, hikariLoader);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        plugin.generateWorld(world);

                        World bukkitWorld = Bukkit.getWorld(world.getName());

                        if (bukkitWorld != null) {
                            plugin.getLogger().info("Executando ação do mundo " + name);

                            WorldUtil.setup(bukkitWorld);

                            afterGenerate.run();
                        } else
                            plugin.getLogger().info("Não foi possível executar a ação do mundo " + name);

                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (1x)", e);
                        return;
                    }

                    plugin.getLogger().info("Mundo \"" + name + "\" carregado e gerado em "
                            + Duration.between(start, Instant.now()).toMillis() + "ms.");
                });
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Não foi possível gerar o mundo \"" + name + "\" (2x)", e);
            }
        });
    }

    @SneakyThrows
    public static void removeWorldsFromLoader(String loaderName) {
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(loaderName);

        if (loader == null) {
            Core.getLogger().info("Loader não encontrado!");
            return;
        }

        loader.listWorlds().forEach(world -> {
            try {
                loader.deleteWorld(world);
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível deletar o mundo \"" + world + "\".", e);
            }
        });
    }

    public static void unloadWorld(World world, SlimeAction afterUnloadWorld) {
        HikariSqlLoader loader = (HikariSqlLoader) SWMPlugin.getInstance().getLoader(Constant.SLIME_HIKARI_SQL_LOADER);

        if (loader == null) {
            Core.getLogger().log(Level.WARNING, "Não foi possível encontrar o loader \"Mongo Atlas\".");
            return;
        }

        boolean unloaded = false;

        try {
            Bukkit.unloadWorld(world, false);

            loader.deleteWorld(world.getName());

            unloaded = true;

            Core.getLogger().info("[SlimeWorld] Mundo \"" + world.getName() + "\" encerrado com sucesso.");
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao encerrar o mundo \"" + world.getName() + "\"...", e);
        }

        if (unloaded)
            afterUnloadWorld.run();
    }
}
