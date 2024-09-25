package br.com.poison.core.bukkit.command.list;

import br.com.poison.core.Constant;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.inventory.profile.stats.StatsInventory;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.command.inheritor.CommandInheritor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class NormalCommands implements CommandInheritor {

    @Command(name = "discord", aliases = {"dc"})
    public void discord(BukkitCommandContext context) {
        Player player = context.getPlayer();

        TextComponent message = new TextComponent("§eDeseja entrar no servidor de §9Discord§e? Clique ");

        TextComponent click = new TextComponent("§b§lAQUI");
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/4G8MqRrwAr"));

        message.addExtra(click);
        message.addExtra("§e para acessar o link!");

        player.sendMessage(message);
    }

    @Command(name = "cash")
    public void cash(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§7Seu cash: §2" + Util.formatNumber(profile.getCash()) + "§7.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null || profile.equals(target)) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        profile.sendMessage("§7O cash atual de §e" + target.getName() + "§7 é §2" + Util.formatNumber(target.getCash()) + "§7.");
    }

    @Command(name = "ping", aliases = {"ms"})
    public void ping(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§aO seu ping atual é de §e" + Util.formatNumber(player.getPing()) + "ms§a.");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        player.sendMessage("§aO ping atual do jogador §e" + target.getName() + "§a é de §e" + Util.formatNumber(target.getPing()) + "ms§a.");
    }

    @Command(name = "stats", aliases = {"estatisticas", "status"})
    public void stats(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (args.length == 0) {
            new StatsInventory(context.getPlayer(), profile, null).init();
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        new StatsInventory(context.getPlayer(), target, null).init();
    }
}
