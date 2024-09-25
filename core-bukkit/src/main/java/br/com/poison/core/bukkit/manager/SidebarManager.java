package br.com.poison.core.bukkit.manager;

import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@Getter
public class SidebarManager {

    private final Set<Sidebar> sidebars = new HashSet<>();

    public Sidebar fetch(Player owner) {
        return sidebars.stream().filter(sidebar -> sidebar.getOwner().getUniqueId().equals(owner.getUniqueId())).findFirst().orElse(null);
    }

    public void save(Sidebar sidebar) {
        sidebars.add(sidebar);
    }

    public void delete(Player owner) {
        sidebars.removeIf(sidebar -> sidebar.getOwner().getUniqueId().equals(owner.getUniqueId()));
    }
}
