package br.com.poison.core.bukkit.inventory.report;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.bukkit.api.mechanics.inventory.sound.SoundCategory;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.resources.report.Report;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class ReportsInventory extends Inventory {

    public ReportsInventory(Player player) {
        super(player, "Lista de denúncias", 5, 21);
    }

    @Override
    public void init() {
        clear();

        List<Report> reports = Core.getReportData().listAll();

        if (!reports.isEmpty()) {
            reports.sort(Comparator.comparing(Report::getId));

            setTotalPages((reports.size() / getMaxItems()) + 1);
            addBorderPage();

            int slot = 10, last = slot;
            for (int i = 0; i < getMaxItems(); i++) {
                setPageIndex(getMaxItems() * (getPageNumber() - 1) + i);

                if (getPageIndex() >= reports.size()) break;

                Report report = reports.get(getPageIndex());

                if (report != null) {
                    Profile profile = report.getProfile();

                    if (profile == null) continue;

                    addItem(slot, new Item(Material.SKULL_ITEM, 1, 3)
                            .skullByBase64(profile.getSkin().getTexture().getValue())
                            .name((profile.isOnline() ? "§a" : "§c") + profile.getName())
                            .lore("§eClique para ver mais!")
                            .click(event -> {
                                playSound(SoundCategory.CHANGE);

                                new ReportInfoInventory(getPlayer(), report, this).init();
                            })
                    );
                }

                slot++;
                if (slot == (last + 7)) {
                    slot += 2;
                    last = slot;
                }
            }
        } else
            addItem(13, new Item(Material.WEB)
                    .name("§cNão há denúncias ;)"));

        display();
    }
}
