package br.com.poison.core.proxy.event.list.profile;

import br.com.poison.core.proxy.event.EventBase;
import br.com.poison.core.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileEvent extends EventBase {
    private final Profile profile;
}
