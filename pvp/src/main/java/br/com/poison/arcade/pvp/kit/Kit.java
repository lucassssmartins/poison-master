package br.com.poison.arcade.pvp.kit;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.api.user.cooldown.CooldownManager;
import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public abstract class Kit {

    private final String name;
    private final Material icon;

    private final KitType kitType;
    private final RankCategory rank;

    private final List<String> lore;
    private final List<Item> extraItems;
    private final List<String> prohibitedCombinations;

    private final int price;
    private final long cooldown;

    public boolean isFree() {
        return price == 0 || rank.equals(RankCategory.PLAYER);
    }

    public boolean isKitItem(ItemStack stack) {
        return extraItems.stream().anyMatch(item -> item.isSimilar(stack));
    }

    public boolean isUsingKit(UUID uuid) {
        User user = PvP.getUserManager().read(uuid);
        if (user == null) return false;

        return user.isUsingKit(name);
    }

    public boolean isAllowReceive(UUID uuid) {
        User user = PvP.getUserManager().read(uuid);
        if (user == null) return false;

        return !user.isProtected() && user.getGame().getCategory().equals(ArcadeCategory.ARENA);
    }

    public boolean hasPermission(UUID uuid) {
        Profile profile = Core.getProfileManager().read(uuid);

        if (profile == null) return false;
        if (isFree()) return true;

        return profile.hasRank(rank) || profile.hasPermission("kitpvp.kit." + name.toLowerCase());
    }

    public void addCooldown(Player player, long time) {
        BukkitCore.getCooldownManager().addCooldown(player.getUniqueId(), "kit-" + name.toLowerCase(), time);
    }

    public boolean hasCooldown(Player player) {
        CooldownManager manager = BukkitCore.getCooldownManager();

        String name = getName().toLowerCase();

        if (manager.hasCooldown(player, "kit-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "kit-" + name);
            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining())
                    + "s para usar seu kit novamente!");
            return true;
        }

        return false;
    }
}
