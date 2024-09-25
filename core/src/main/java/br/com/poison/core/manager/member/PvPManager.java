package br.com.poison.core.manager.member;

import br.com.poison.core.manager.base.Manager;
import br.com.poison.core.resources.member.type.pvp.PvPMember;

import java.util.UUID;

public class PvPManager extends Manager<UUID, PvPMember> {

    @Override
    public void save(PvPMember member) {
        getCacheMap().put(member.getId(), member);
    }

    public PvPMember fetch(String name) {
        return documents(member -> member.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
