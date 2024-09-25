package br.com.poison.core.bukkit.api.mechanics.item;

import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.StringUtils;
import br.com.poison.core.bukkit.api.mechanics.item.interact.ItemInteract;
import br.com.poison.core.bukkit.api.mechanics.item.updater.ItemUpdater;
import br.com.poison.core.bukkit.api.mechanics.item.click.ItemClick;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
public class Item extends ItemStack {

    @Getter
    protected static final Set<Item> itemList = new HashSet<>();

    protected ItemMeta meta = getItemMeta();

    private ItemClick click;
    private ItemInteract interact;

    private ItemUpdater updater;

    public Item(Material type) {
        super(type);
    }

    public Item(String type) {
        super(Material.getMaterial(type));
    }

    public Item(Material type, int amount) {
        super(type, amount);
    }

    public Item(Material type, int amount, int id) {
        super(type, amount, (short) id);
    }

    /* Object Methods */
    public String getName() {
        return getType().name();
    }

    public void updateMeta(ItemMeta meta) {
        this.meta = meta;
        setItemMeta(meta);
    }

    public static Item convertItem(ItemStack stack) {
        return itemList.stream().filter(item -> item.isSimilar(stack)).findFirst().orElse(null);
    }

    public static Item fromStack(ItemStack stack) {
        Item item = new Item(stack.getType());

        item.setAmount(stack.getAmount());
        item.setDurability(stack.getDurability());
        item.setData(stack.getData());

        item.setItemMeta(stack.getItemMeta());

        return item;
    }

    public static boolean exists(ItemStack stack) {
        return itemList.stream().anyMatch(item -> item.isSimilar(stack));
    }

    public String typeName() {
        return getType().name();
    }

    public Item click(ItemClick click) {
        this.click = click;

        itemList.add(this);
        return this;
    }

    public Item interact(ItemInteract interact) {
        this.interact = interact;

        itemList.add(this);
        return this;
    }

    public Item updater(ItemUpdater updater) {
        this.updater = updater;

        itemList.add(this);
        return this;
    }

    /* Item Stack Methods */
    public Item type(Material type) {
        setType(type);
        return this;
    }

    public Item name(String name) {
        meta.setDisplayName(Util.color(name));

        updateMeta(meta);
        return this;
    }

    public Item leatherColor(Color color) {
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;

        armorMeta.setLore(null);
        armorMeta.setColor(color);

        return this;
    }

    public Item amount(int amount) {
        setAmount(amount);
        return this;
    }

    public Item durability(int durability) {
        setDurability((short) durability);
        return this;
    }

    public Item lore(List<String> lore) {
        meta.setLore(lore);

        updateMeta(meta);
        return this;
    }


    public Item lore(String... lore) {
        meta.setLore(Arrays.asList(lore));

        updateMeta(meta);
        return this;
    }

    public Item lore(String lore) {
        meta.setLore(StringUtils.formatForLore(lore));

        updateMeta(meta);
        return this;
    }

    public Item flags(ItemFlag... flags) {
        meta.addItemFlags(flags);

        updateMeta(meta);
        return this;
    }

    public Item enchantment(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);

        updateMeta(meta);
        return this;
    }

    public Item skullByName(String name) {
        SkullMeta skullMeta = (SkullMeta) meta;

        skullMeta.setOwner(name);
        updateMeta(skullMeta);

        return this;
    }

    public Item skullByUrl(String url) {
        SkullMeta skullMeta = (SkullMeta) meta;

        if (url != null) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", Base64.getEncoder().encodeToString(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes(StandardCharsets.UTF_8))));
            try {
                Field field = skullMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(skullMeta, profile);

                updateMeta(skullMeta);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public Item skullByBase64(String value) {
        SkullMeta itemMeta = (SkullMeta) meta;

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", value));

        Field field;
        try {
            field = itemMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(itemMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        updateMeta(itemMeta);
        return this;
    }
}
