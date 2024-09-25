package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;

public class Default extends Kit {

    public Default() {
        super("PvP", Material.STONE_SWORD, KitType.PRIMARY, RankCategory.PLAYER,
                Collections.singletonList("§7Este kit não possui habilidades."),
                new ArrayList<>(),
                new ArrayList<>(),
                0, 0);
    }
}
