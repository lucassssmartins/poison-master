package br.com.poison.arcade.duels.game.list.standard.simulator.kit;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.client.Client;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.api.user.cooldown.CooldownManager;
import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public abstract class Kit {

    private final String name;
    private final String lore;

    private final RankCategory rank = RankCategory.VENOM;
    private final Material icon;

    private final long cooldown;

    private final List<Item> extraItems;

    public Kit(String name, String lore, Material icon, long cooldown) {
        this(name, lore, icon, cooldown, new ArrayList<>());
    }

    public boolean isKitItem(ItemStack stack) {
        return extraItems.stream().anyMatch(item -> item.isSimilar(stack));
    }

    public boolean isUsingKit(UUID uuid) {
        Client client = Duels.getClientManager().read(uuid);
        if (client == null) return false;

        return client.getData().isUsingKit(this);
    }

    public boolean hasKit(UUID uuid) {
        Client client = Duels.getClientManager().read(uuid);

        if (client == null) return false;

        return client.getProfile().hasRank(rank);
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
