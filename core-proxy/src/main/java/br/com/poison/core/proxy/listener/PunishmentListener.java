package br.com.poison.core.proxy.listener;

import br.com.poison.core.Constant;
import br.com.poison.core.backend.data.PunishmentData;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.punishment.Punishment;
import br.com.poison.core.resources.punishment.category.PunishmentCategory;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PunishmentListener implements Listener {

    private final PunishmentData data;

    public PunishmentListener() {
        this.data = Core.getPunishmentData();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void ban(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();

        Profile profile = Core.getProfileData().read(connection.getUniqueId(), false);

        if (profile == null) return;

        Punishment ban = data.hasActiveBan(profile);

        if (ban != null) {
            if (ban.isValid() && (!ban.hasExpired() || ban.isPermanent())) {
                event.setCancelReason(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                        ban.isPermanent() ? "permanentemente" : "temporariamente",
                        ban.getReason().getInfo(),
                        (!ban.isPermanent() ? "§cExpira em: " + TimeUtil.formatTime(ban.getExpiresAt()) : "") + "\n",
                        ban.getCode()
                )));

                event.setCancelled(true);
                return;
            }

            data.delete(ban);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void mute(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!event.isCommand() || !event.isProxyCommand()) {
            if (data.hasActiveMute(player.getUniqueId())) {
                Punishment mute = data.getActivePunishment(player.getUniqueId(), PunishmentCategory.MUTE);

                if (mute != null) {
                    if (!mute.hasExpired() || mute.isPermanent()) {
                        event.setCancelled(true);

                        player.sendMessage(TextComponent.fromLegacyText("§cVocê está "
                                + (mute.isPermanent() ? "permanentemente" : "temporariamente") + " mutado no servidor."));
                        return;
                    }

                    data.delete(mute);
                }
            }
        }
    }
}
