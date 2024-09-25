package br.com.poison.core.bukkit.inventory.report;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.util.extra.DateUtil;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.resources.report.Report;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReportInfoInventory extends Inventory {

    private final Report report;

    public ReportInfoInventory(Player player, Report report, Inventory last) {
        super(player, "Ver denúncia > " + report.getName(), last, 5);

        this.report = report;
    }

    @Override
    public void init() {
        clear();

        Profile profile = report.getProfile();

        addItem(13, new Item(Material.SKULL_ITEM, 1, 3)
                .skullByBase64(profile.getSkin().getTexture().getValue())
                .name((profile.isOnline() ? "§a" : "§c") + profile.getName())
                .lore("§fExpira em: §7" + TimeUtil.formatTime(report.getExpiresAt()),
                        "§fÚltima denúncia: §7" + DateUtil.getDate(report.getLastReportAt()))
                .updater(view -> {
                    if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                    Item item = getContents().get(13);

                    item.lore("§fExpira em: §7" + TimeUtil.formatTime(report.getExpiresAt()),
                            "§fÚltima denúncia: §7" + DateUtil.getDate(report.getLastReportAt()));

                    view.setItem(13, item);

                    getPlayer().updateInventory();
                })
        );

        addItem(20, new Item(Material.ENDER_PEARL)
                .name("§aRedirecionar")
                .lore("§7Clique para ir",
                        "§7até o jogador.",
                        "",
                        "§eClique para redirecionar!")
                .click(event -> {
                    close();

                    playSound(SoundCategory.SUCCESS);

                    getPlayer().performCommand("go " + report.getName());
                }));

        addItem(22, new Item(Material.BARRIER)
                .name("§cRemover denúncia")
                .lore("§7Clique para remover",
                        "§7essa denúncia.",
                        "",
                        "§eClique para remover!")
                .click(event -> {
                    playSound(SoundCategory.SUCCESS);

                    Core.getReportData().delete(report.getUniqueId());

                    getLast().init();
                }));

        addItem(24, new Item(Material.BOOK)
                .name("§aVer motivos")
                .lore("§7Veja os motivos",
                        "§7dessa denúncia.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    playSound(SoundCategory.CHANGE);

                    new ReportReasonsInventory(getPlayer(), report, this).init();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
