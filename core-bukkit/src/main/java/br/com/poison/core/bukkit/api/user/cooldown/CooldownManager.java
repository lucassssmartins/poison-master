package br.com.poison.core.bukkit.api.user.cooldown;

import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import br.com.poison.core.bukkit.event.list.cooldown.CooldownEndEvent;
import br.com.poison.core.bukkit.event.list.cooldown.CooldownStartEvent;
import br.com.poison.core.bukkit.manager.PlayerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CooldownManager {

    private final Map<UUID, List<Cooldown>> cooldownMap = new ConcurrentHashMap<>();

    public void display(Player player, Cooldown cooldown) {
        StringBuilder bar = new StringBuilder();

        double percentage = cooldown.getPercentage();
        double count = 20 - Math.max(percentage > 0D ? 1 : 0, percentage / 5);

        char CHAR = '|';

        for (int a = 0; a < count; a++)
            bar.append("§a").append(CHAR);
        for (int a = 0; a < 20 - count; a++)
            bar.append("§c").append(CHAR);

        String replacedName = cooldown.getName().replace("kit-", "");
        String kitName = replacedName.substring(0, 1).toUpperCase() + replacedName.substring(1);

        PlayerManager.sendBar(player, "§f" + kitName + " " + bar + " §f"
                + (new DecimalFormat("#.#").format(cooldown.getRemaining()) + "s"));
    }

    public Cooldown getCooldown(Player player, String cooldownName) {
        if (cooldownMap.containsKey(player.getUniqueId())) {
            List<Cooldown> cooldownList = cooldownMap.get(player.getUniqueId());

            for (Cooldown cooldown : cooldownList) {
                if (cooldown.getName().equalsIgnoreCase(cooldownName))
                    return cooldown;
            }
        }

        return null;
    }

    public void resetCooldown(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }

    public void addCooldown(Player player, Cooldown cooldown) {
        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        event.call();

        if (!event.isCancelled()) {
            boolean addCooldown = true;
            List<Cooldown> cooldownList = cooldownMap.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());

            for (Cooldown search : cooldownList) {
                if (search.getName().equalsIgnoreCase(cooldown.getName())) {
                    search.update(cooldown.getDuration(), cooldown.getStartTime());
                    addCooldown = false;
                }
            }

            if (addCooldown)
                cooldownList.add(cooldown);
        }
    }

    public void addCooldown(UUID uniqueId, String cooldownName, long duration) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player == null) return;

        Cooldown cooldown = new Cooldown(cooldownName, duration);

        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        event.call();

        if (!event.isCancelled()) {
            boolean addCooldown = true;
            List<Cooldown> cooldownList = cooldownMap.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());

            for (Cooldown search : cooldownList) {
                if (search.getName().equalsIgnoreCase(cooldown.getName())) {
                    search.update(cooldown.getDuration(), cooldown.getStartTime());
                    addCooldown = false;
                }
            }

            if (addCooldown)
                cooldownList.add(cooldown);
        }
    }

    public void removeCooldown(Player player, String cooldownName) {
        if (cooldownMap.containsKey(player.getUniqueId())) {
            List<Cooldown> cooldownList = cooldownMap.get(player.getUniqueId());

            Iterator<Cooldown> cooldownIterator = cooldownList.iterator();
            while (cooldownIterator.hasNext()) {
                Cooldown cooldown = cooldownIterator.next();

                if (cooldown.getName().equalsIgnoreCase(cooldownName)) {
                    cooldownIterator.remove();
                    new CooldownEndEvent(player, cooldown).call();
                    return;
                }
            }
        }
    }

    public boolean hasCooldown(Player player, String cooldownName) {
        if (cooldownMap.containsKey(player.getUniqueId())) {
            List<Cooldown> cooldownList = cooldownMap.get(player.getUniqueId());

            for (Cooldown cooldown : cooldownList) {
                if (cooldown.expired())
                    removeCooldown(player, cooldownName);

                if (cooldown.getName().equalsIgnoreCase(cooldownName))
                    return true;
            }
        }

        return false;
    }
}