package br.com.poison.core.resources.member.type.duels;

import br.com.poison.core.Constant;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.resources.member.Member;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.duels.inventory.DuelInventory;
import br.com.poison.core.resources.member.type.duels.stats.DuelStats;
import lombok.Getter;

@Getter
public class DuelMember extends Member {

    protected transient final Profile profile;

    private DuelInventory inventory;
    private DuelStats stats;

    public DuelMember(Profile profile) {
        super(profile);

        this.profile = profile;

        this.inventory = new DuelInventory();
        this.stats = new DuelStats();
    }

    @Override
    protected void save(String... fields) {
        for (String field : fields)
            Core.getDuelData().update(this, field);
    }

    /* Stats */
    public void saveStats(DuelStats stats) {
        this.stats = stats;
        save("stats");
    }

    /* Inventory */

    public String getData(ArcadeCategory arcade) {
        switch (arcade) {
            case SIMULATOR:
                return inventory.getSimulatorData();
            default:
                return "";
        }
    }

    public String getDefaultData(ArcadeCategory arcade) {
        switch (arcade) {
            case SIMULATOR:
                return Constant.SIMULATOR_INVENTORY;
            default:
                return "";
        }
    }

    public void saveData(ArcadeCategory arcade, String data) {
        DuelInventory inventory = getInventory();

        switch (arcade) {
            case SIMULATOR:
                inventory.setSimulatorData(data);
                break;
        }

        updateInventory(inventory);
    }

    protected void updateInventory(DuelInventory inventory) {
        this.inventory = inventory;
        save("inventory");
    }
}
