package br.com.poison.auth.listener;

import br.com.poison.auth.user.User;
import br.com.poison.auth.Auth;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Arrays;
import java.util.List;

public class UserListener implements Listener {

    private final List<String> AUTH_COMMANDS = Arrays.asList("/login", "/logar", "/autenticar", "/register", "/registrar", "/cadastrar");

    @EventHandler
    public void spawn(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(Auth.getSpawn());
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        if (profile == null) return;

        new User(profile);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Auth.getUserManager().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        Auth.getUserManager().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void chat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void command(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        User user = Auth.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        String message = event.getMessage();

        if (!user.isAuthenticated() && !(message.startsWith("/login") || message.startsWith("/logar") || message.startsWith("/autenticar")
                || message.startsWith("/register") || message.startsWith("/registrar") || message.startsWith("/cadastrar"))) {
            event.setCancelled(true);

            player.sendMessage("§cOps! Autentique-se para usar comandos.");
        }
    }
}
