package br.com.poison.core.bukkit.event.list.profile;

import br.com.poison.core.bukkit.event.EventHandler;
import br.com.poison.core.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileEvent extends EventHandler {
    private final Profile profile;
}