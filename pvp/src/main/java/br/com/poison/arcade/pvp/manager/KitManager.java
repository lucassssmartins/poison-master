package br.com.poison.arcade.pvp.manager;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.Core;
import br.com.poison.core.util.loader.ClassLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class KitManager {

    private final PvP pvp;

    private final Set<Kit> kits = new HashSet<>();

    public void load() {
        Core.getLogger().info("Iniciando kits...");

        int total = 0;

        for (Class<?> kitClass : ClassLoader.getClassesForPackage(pvp, "br.com.poison.arcade.pvp.kit.type")) {

            if (Kit.class.isAssignableFrom(kitClass)) {
                try {
                    Kit kit = (Kit) kitClass.newInstance();
                    kits.add(kit);

                    total += 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (total > 0)
            Core.getLogger().info(total + " kits foram carregados com sucesso!");
    }

    public Kit fetch(String name) {
        return getKits().stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Kit fetch(KitType type, String name) {
        return getKits(type).stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Kit> getKits(KitType type) {
        return getKits().stream().filter(kit -> kit.getKitType().equals(type)).collect(Collectors.toList());
    }
}