package br.com.poison.core.bukkit.inventory.profile.preference;

import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.preference.Preference;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public class PreferenceInventory extends Inventory {

    private final Profile profile;

    @Setter
    private PreferenceCategory category;

    public PreferenceInventory(Profile profile, Inventory last) {
        super(profile.player(), "Editar preferências", last, 5);

        this.profile = profile;
        this.category = PreferenceCategory.DEFAULT;
    }

    @Override
    public void init() {
        clear();

        Preference preference = profile.getPreference();

        switch (category) {
            case VIP: {
                addItem(10, new Item(Material.NAME_TAG)
                        .name((preference.isJoinMessage() ? "§a" : "§c") + "Mensagem ao Entrar")
                        .lore("§7" + (preference.isJoinMessage() ? "Desative" : "Ative") + " as mensagens",
                                "§7ao entrar no servidor.",
                                "",
                                "§eClique para " + (preference.isJoinMessage() ? "desativar" : "ativar") + "!")
                        .click(event -> {
                            preference.setJoinMessage(!preference.isJoinMessage());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                addItem(19, addPreferenceButton(preference.isJoinMessage(), "Mensagem ao Entrar")
                        .click(event -> {
                            preference.setJoinMessage(!preference.isJoinMessage());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                break;
            }

            case STAFF: {
                addItem(10, new Item(Material.BOOK)
                        .name((preference.isAllowStaffChatMessages() ? "§a" : "§c") + "Mensagens da equipe")
                        .lore("§7" + (preference.isAllowStaffChatMessages() ? "Desative" : "Ative") + " as mensagens",
                                "§7da equipe.",
                                "",
                                "§eClique para " + (preference.isAllowStaffChatMessages() ? "desativar" : "ativar") + "!")
                        .click(event -> {
                            preference.setAllowStaffChatMessages(!preference.isAllowStaffChatMessages());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                addItem(19, addPreferenceButton(preference.isAllowStaffChatMessages(), "Mensagens da equipe")
                        .click(event -> {
                            preference.setAllowStaffChatMessages(!preference.isAllowStaffChatMessages());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));

                addItem(11, new Item(Material.PAPER)
                        .name((preference.isAllowLogs() ? "§a" : "§c") + "Logs")
                        .lore("§7" + (preference.isAllowLogs() ? "Desative" : "Ative") + " as logs",
                                "§7do servidor.",
                                "",
                                "§eClique para " + (preference.isAllowLogs() ? "desativar" : "ativar") + "!")
                        .click(event -> {
                            preference.setAllowLogs(!preference.isAllowLogs());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                addItem(20, addPreferenceButton(preference.isAllowLogs(), "Logs")
                        .click(event -> {
                            preference.setAllowLogs(!preference.isAllowLogs());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));

                if (profile.hasRank(RankCategory.TRIAL)) {
                    addItem(12, new Item(Material.REDSTONE)
                            .name((preference.isAutoVanishMode() ? "§a" : "§c") + "Vanish Automático")
                            .lore("§7" + (preference.isAutoVanishMode() ? "Desative" : "Ative") + " o vanish",
                                    "§7ao entrar no servidor.",
                                    "",
                                    "§eClique para " + (preference.isAllowLogs() ? "desativar" : "ativar") + "!")
                            .click(event -> {
                                preference.setAutoVanishMode(!preference.isAutoVanishMode());

                                profile.savePreference(preference);

                                playSound(SoundCategory.SUCCESS);
                                init();
                            }));
                    addItem(21, addPreferenceButton(preference.isAutoVanishMode(), "Vanish Automático")
                            .click(event -> {
                                preference.setAutoVanishMode(!preference.isAutoVanishMode());

                                profile.savePreference(preference);

                                playSound(SoundCategory.SUCCESS);
                                init();
                            }));
                }
                break;
            }

            default: {
                addItem(10, new Item(Material.ITEM_FRAME)
                        .name((preference.isAllowClanInteractions() ? "§a" : "§c") + "Interações do Clan")
                        .lore("§7" + (preference.isAllowClanInteractions() ? "Desative" : "Ative") + " as interações",
                                "§7do seu clan.",
                                "",
                                "§eClique para " + (preference.isAllowClanInteractions() ? "desativar" : "ativar") + "!")
                        .click(event -> {
                            preference.setAllowClanInteractions(!preference.isAllowClanInteractions());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                addItem(19, addPreferenceButton(preference.isAllowClanInteractions(), "Interações do Clan")
                        .click(event -> {
                            preference.setAllowClanInteractions(!preference.isAllowClanInteractions());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));

                addItem(11, new Item(Material.PAPER)
                        .name((preference.isAllowDirectMessages() ? "§a" : "§c") + "Mensagens Privadas")
                        .lore("§7" + (preference.isAllowDirectMessages() ? "Desative" : "Ative") + " as suas",
                                "§7mensagens privadas.",
                                "",
                                "§eClique para " + (preference.isAllowDirectMessages() ? "desativar" : "ativar") + "!")
                        .click(event -> {
                            preference.setAllowDirectMessages(!preference.isAllowDirectMessages());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                addItem(20, addPreferenceButton(preference.isAllowDirectMessages(), "Mensagens Privadas")
                        .click(event -> {
                            preference.setAllowDirectMessages(!preference.isAllowDirectMessages());

                            profile.savePreference(preference);

                            playSound(SoundCategory.SUCCESS);
                            init();
                        }));
                break;
            }
        }

        addItem(17, createPreference(PreferenceCategory.DEFAULT));

        if (profile.isVIP())
            addItem(26, createPreference(PreferenceCategory.VIP));

        if (profile.isStaffer())
            addItem(35, createPreference(PreferenceCategory.STAFF));

        if (isReturnable())
            addBackButton();

        display();
    }

    protected Item createPreference(PreferenceCategory category) {
        Item item = new Item(category.getIcon())
                .name(category.getName())
                .lore((this.category.equals(category) ? "§cVocê está vendo." : "§eClique para ver!"))
                .flags(ItemFlag.values());

        if (this.category.equals(category))
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        item.click(event -> {
            if (this.category.equals(category)) return;

            setCategory(category);
            playSound(SoundCategory.CHANGE);

            init();
        });

        return item;
    }

    @Getter
    @AllArgsConstructor
    public enum PreferenceCategory {
        DEFAULT("§7Preferências padrões", Material.WEB),
        VIP("§aPreferências VIPs", Material.EMERALD),
        STAFF("§bPreferências da equipe", Material.ANVIL);

        private final String name;
        private final Material icon;
    }
}
