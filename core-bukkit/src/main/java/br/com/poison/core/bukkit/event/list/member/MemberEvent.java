package br.com.poison.core.bukkit.event.list.member;

import br.com.poison.core.bukkit.event.EventHandler;
import br.com.poison.core.resources.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberEvent extends EventHandler {
    private final Member member;
}
