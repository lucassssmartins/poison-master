package br.com.poison.core.proxy.listener;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.backend.data.ProfileData;
import br.com.poison.core.manager.ProfileManager;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.profile.resources.relation.type.ProfileType;
import br.com.poison.core.proxy.ProxyConstant;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.mojang.UUIDFetcher;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.logging.Level;

public class ProfileListener implements Listener {

    private final ProfileData profileData;
    private final ProfileManager profileManager;

    public ProfileListener() {
        profileData = Core.getProfileData();
        profileManager = Core.getProfileManager();
    }

    @EventHandler
    public void onOnlineMode(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();

        connection.setOnlineMode(UUIDFetcher.isOnlineMode(connection.getName()));
    }

    @EventHandler
    public void onLoad(LoginEvent event) {
        try {
            PendingConnection connection = event.getConnection();

            Profile profile = profileData.read(connection.getUniqueId(), true);

            if (profile == null) {
                profile = profileData.save(connection.getUniqueId(), connection.getName());

                if (profile == null) {
                    event.setCancelReason(TextComponent.fromLegacyText(Constant.PROFILE_CREATE_FAILED + " (Ex0001)"));
                    event.setCancelled(true);

                    return;
                }
            }

            profileData.persist(connection.getUniqueId());

            profileManager.save(profile);

            Core.getLogger().info("[" + profileManager.size() + "] Conta de " + profile.getName() + " iniciada!");
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro...", e);
        }
    }

    @EventHandler
    public void connected(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Profile profile = profileManager.read(player.getUniqueId());

        if (!profile.getName().equalsIgnoreCase(player.getName()))
            profile.changeName(player.getName());

        if (!profile.isAuthentic() && player.getPendingConnection().isOnlineMode())
            profile.setProfileType(ProfileType.AUTHENTIC);

        if (profile.getIpAddress().isEmpty() || profile.getIpAddress().equalsIgnoreCase(Constant.DEFAULT_ADDRESS))
            profile.setIpAddress(player.getAddress().getHostString());

        profile.setOnline(true);
        profile.updateLastEntry();

        Core.getLogger().info(profile.getName() + " entrou no Proxy.");
    }

    @EventHandler
    public void disconnect(PlayerDisconnectEvent event) {
        Profile profile = profileManager.read(event.getPlayer().getUniqueId());

        if (profile == null) return;

        profile.unload();

        profile.resetFake();

        profile.setOnline(false);
        profile.setServer(ServerCategory.PROXY);

        Core.getLogger().info(profile.getName() + " saiu do Proxy.");

        profileData.cache(profile.getId());
    }

    @EventHandler
    public void onStaffChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        Profile profile = profileManager.read(player.getUniqueId());

        if (profile == null) return;

        // Staff Chat
        if (!event.isCommand()) {
            if (profile.isStaffer() && profile.getPreference().isInStaffChat()) {
                event.setCancelled(true);

                Tag tag = profile.getDefaultTag();

                if (tag == null)
                    tag = Tag.OWNER;

                Tag finalTag = tag;

                profileManager
                        .documents(target -> target.isStaffer() && target.getPreference().isAllowStaffChatMessages())
                        .forEach(target -> target.sendMessage(
                                ProxyConstant.STAFF_CHAT_PREFIX + finalTag.getPrefix() + profile.getName() + ": §f" + event.getMessage()));
            }
        }
    }
}
