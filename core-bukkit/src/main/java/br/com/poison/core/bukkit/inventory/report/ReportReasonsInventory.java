package br.com.poison.core.bukkit.inventory.report;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.inventory.Inventory;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.report.info.ReportInfo;
import br.com.poison.core.util.extra.DateUtil;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.resources.report.Report;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ReportReasonsInventory extends Inventory {

    private final Report report;

    public ReportReasonsInventory(Player player, Report report, Inventory last) {
        super(player, "Ver motivos > " + report.getName(), last, 5, 21);

        this.report = report;
    }

    @Override
    public void init() {
        clear();

        List<ReportInfo> infos = report.listInfos();

        if (!infos.isEmpty()) {
            setTotalPages((infos.size() / getMaxItems()) + 1);
            addBorderPage();

            int slot = 10, last = slot;
            for (int i = 0; i < getMaxItems(); i++) {
                setPageIndex(getMaxItems() * (getPageNumber() - 1) + i);

                if (getPageNumber() >= infos.size()) break;

                ReportInfo info = infos.get(getPageIndex());

                if (info != null) {
                    Profile author = Core.getProfileData().read(info.getAuthor(), false);

                    if (author == null) continue;

                    addItem(slot, new Item(Material.PAPER)
                            .name("§e" + getPageIndex())
                            .lore("§7" + info.getReason(),
                                    "",
                                    "§fAutor: §7" + author.getName(),
                                    "§fEnviado em: §7" + DateUtil.getDate(info.getCreatedAt())));
                }

                slot++;
                if (slot == (last + 7)) {
                    slot += 2;
                    last = slot;
                }
            }
        } else
            addItem(13, new Item(Material.WEB)
                    .name("§cNenhum motivo encontrado ;("));

        if (isReturnable())
            addBackButton();

        display();
    }
}
