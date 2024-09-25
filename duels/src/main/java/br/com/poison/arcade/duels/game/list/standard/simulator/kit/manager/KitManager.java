package br.com.poison.arcade.duels.game.list.standard.simulator.kit.manager;

import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import br.com.poison.arcade.duels.Duels;
import br.com.poison.core.Core;
import br.com.poison.core.util.loader.ClassLoader;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class KitManager {

    @Getter
    private static final Set<Kit> kits = new HashSet<>();

    public static void handle() {
        for (Class<?> kitClass : ClassLoader.getClassesForPackage(Duels.getPlugin(Duels.class),
                "br.com.poison.arcade.duels.game.list.standard.simulator.kit.list")) {
            if (Kit.class.isAssignableFrom(kitClass)) {
                try {
                    Kit kit = (Kit) kitClass.newInstance();

                    kits.add(kit);
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao carregar o kit " + kitClass.getSimpleName(), e);
                }
            }
        }
    }
}
