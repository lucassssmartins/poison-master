package br.com.poison.core.profile.resources.rank.tag;

import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.profile.resources.permission.category.PermissionCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum Tag {

    PLAYER(RankCategory.PLAYER, "Membro", "§7", "Z", false, false),

    /* Colored Tags */
    MAGIC("Magic", ChatColor.MAGIC, "§7§k", true),
    RED("Vermelho", ChatColor.RED, "§c", true),
    BLUE("Azul", ChatColor.BLUE, "§9", true),
    GREEN("Green", ChatColor.GREEN, "§a", true),
    YELLOW("Yellow", ChatColor.YELLOW, "§e", true),
    LIGHT_PURPLE("Light Purple", ChatColor.LIGHT_PURPLE, "§d", true),
    PURPLE("Purple", ChatColor.DARK_PURPLE, "§5", true),
    WHITE("White", ChatColor.WHITE, "§f", true),
    AQUA("Aqua", ChatColor.AQUA, "§b", true),

    /* Vip Tags */
    VENOM(RankCategory.VENOM, "Venom", "§a§lVENOM §r§a", "N", true, true),
    POISON(RankCategory.POISON, "Poison", "§2§lPOISON §r§2", "M", true, true),
    BETA(RankCategory.BETA, "Beta", "§1§lBETA §r§1", "K", true, true),

    /* Creator Tags */
    MEDIA(RankCategory.MEDIA, "Midia", "§b§lMIDIA §r§b", "J", false, true),
    PLUS_MEDIA(RankCategory.PLUS_MEDIA, "Midia+", "§3§lMIDIA+ §r§3", "H", false, true),

    /* Staff Tags */
    BUILDER(RankCategory.BUILDER, "Builder", "§a§lBUILDER §r§a", "G", false, true),
    TRIAL(RankCategory.TRIAL, "Trial", "§d§lTRIAL §r§d", "F", false, true),
    MOD(RankCategory.MOD, "Mod", "§5§lMOD §r§5", singletonList("moderador"), "E", false, true),
    MODPLUS(RankCategory.MODPLUS, "Mod+", "§5§lMOD+ §r§5", asList("moderador+", "moderadorplus"), "D", false, true),
    DEV(RankCategory.DEV, "Dev", "§3§lDEV §r§3", asList("developer", "desenvolvedor"), "C", false, true),
    OWNER(RankCategory.OWNER, "", "§4§lDONO §r§4", singletonList("dono"), "A", false, true);

    private final RankCategory category;

    private final String name;
    private final ChatColor color;
    private final String prefix;

    private final List<String> aliases;

    private final String order;
    private final boolean colored, reward, exclusive;

    /* Constructor for Colored/Special Tags */
    Tag(String name, ChatColor color, String prefix, boolean colored) {
        this(RankCategory.PLAYER, name, color, prefix, new ArrayList<>(), "X", colored, !colored, true);
    }

    Tag(RankCategory category, String name, String prefix, String order, boolean reward, boolean exclusive) {
        this(category, name, category.getColor(), prefix, new ArrayList<>(), order, false, reward, exclusive);
    }

    Tag(RankCategory category, String name, String prefix, List<String> aliases, String order, boolean reward, boolean exclusive) {
        this(category, name, category.getColor(), prefix, aliases, order, false, reward, exclusive);
    }

    public static Tag fetch(String name) {
        return Arrays.stream(values())
                .filter(tag -> !tag.isColored() && (tag.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name))
                        || tag.name().equalsIgnoreCase(name) || tag.getName().equalsIgnoreCase(name)))
                .findFirst().orElse(null);
    }

    public static Tag fetch(RankCategory category) {
        return Arrays.stream(values())
                .filter(tag -> !tag.isColored() && tag.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

    public static Tag fetch(Predicate<Tag> filter) {
        return Arrays.stream(values()).filter(filter).findFirst().orElse(null);
    }

    public String getColoredName() {
        return getColor() + getName();
    }

    public String getColoredMagic() {
        return getColor() + "§k";
    }

    public String getPermission() {
        return String.format(PermissionCategory.TAG.getKey(), getName().toLowerCase());
    }
}
