package br.com.poison.core.resources.clan.option;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ClanOptions {

    private final Set<ChatColor> colors = new HashSet<>();

    private int maxParticipants = 12;

    private boolean allowNewParticipants = true;
}
