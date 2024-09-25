package br.com.poison.core.manager;

import br.com.poison.core.manager.base.Manager;
import br.com.poison.core.resources.clan.Clan;

import java.util.UUID;

public class ClanManager extends Manager<UUID, Clan> {

    @Override
    public void save(Clan clan) {
        getCacheMap().put(clan.getId(), clan);
    }

    public Clan fetchByName(String name) {
        return documents(clan -> clan.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Clan fetchByTag(String tag) {
        return documents(clan -> clan.getTag().equalsIgnoreCase(tag)).findFirst().orElse(null);
    }
}
