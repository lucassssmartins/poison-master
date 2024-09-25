package br.com.poison.core.proxy.listener;

import br.com.poison.core.proxy.event.list.profile.field.ProfileFieldChangeEvent;
import br.com.poison.core.Core;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class UpdateListener implements Listener {

    @EventHandler
    public void onProfileField(ProfileFieldChangeEvent event) {
        Core.getProfileManager().save(event.getProfile());
    }
}
