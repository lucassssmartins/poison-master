package br.com.poison.core.manager.member;

import br.com.poison.core.manager.base.Manager;
import br.com.poison.core.resources.member.type.duels.DuelMember;

import java.util.UUID;

public class DuelManager extends Manager<UUID, DuelMember> {

    @Override
    public void save(DuelMember member) {
        getCacheMap().put(member.getId(), member);
    }

    public DuelMember fetch(String name) {
        return documents(member -> member.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
