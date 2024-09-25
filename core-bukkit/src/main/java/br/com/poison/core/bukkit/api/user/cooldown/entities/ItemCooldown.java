package br.com.poison.core.bukkit.api.user.cooldown.entities;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ItemCooldown extends Cooldown {

    private final ItemStack item;

    @Setter
    private boolean selected;

    public ItemCooldown(ItemStack item, String name, Long duration) {
        super(name, duration);
        this.item = item;
    }
}