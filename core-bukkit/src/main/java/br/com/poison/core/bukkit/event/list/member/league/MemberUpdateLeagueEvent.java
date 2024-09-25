package br.com.poison.core.bukkit.event.list.member.league;

import br.com.poison.core.bukkit.event.list.member.MemberEvent;
import br.com.poison.core.resources.league.League;
import br.com.poison.core.resources.member.Member;
import lombok.Getter;

@Getter
public final class MemberUpdateLeagueEvent extends MemberEvent {

    private final League league;

    public MemberUpdateLeagueEvent(Member member, League league) {
        super(member);

        this.league = league;
    }
}
