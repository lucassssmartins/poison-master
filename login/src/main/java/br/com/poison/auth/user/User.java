package br.com.poison.auth.user;

import br.com.poison.auth.Auth;
import br.com.poison.auth.inventory.CaptchaInventory;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.profile.Profile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class User {

    private final Profile profile;
    private final Sidebar sidebar;

    private int time, loginAttempts = 3;

    private boolean captcha, authenticated, verification = true;

    public User(Profile profile) {
        this.profile = profile;

        Player player = profile.player();

        this.sidebar = new Sidebar(player, "AUTH");

        // Sidebar
        sidebar.clear();
        sidebar.blankRow();

        sidebar.addRow("wait", "§fAguardando...");

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();

        profile.sendMessage(profile.getAuth().isRegistered()
                ? "§cAutentique-se usando: §e/autenticar <senha>§c."
                : "§cCadastre-se usando: §e/cadastrar <senha> <confirmar-senha>§c.");

        // Open Captcha Inventory
        Core.getMultiService().syncLater(() -> new CaptchaInventory(player).init(), 10L);

        Auth.getUserManager().save(this);
    }
}
