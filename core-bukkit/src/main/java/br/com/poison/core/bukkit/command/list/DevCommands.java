package br.com.poison.core.bukkit.command.list;

import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.bukkit.SerializeInventory;
import br.com.poison.core.util.extra.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class DevCommands implements CommandInheritor {

    protected final String MEMORY_FORMAT = ChatColor.GRAY + "[%sMB/ %sMB/ %sMB/ %sMB] %s";

    @Command(name = "arcadedev", aliases = {"ad"}, rank = RankCategory.DEV)
    public void test(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (args.length <= 3) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <server> <arcade> <slot> <input> [Opcional: <arenaId> <mapId>]");
            return;
        }

        ServerCategory server = ServerCategory.fetch(args[0]);

        if (server == null || !server.isAllowArcade()) {
            profile.sendMessage("§cO servidor informado não existe ou não suporta jogos.");
            return;
        }

        ArcadeCategory arcade = ArcadeCategory.fetch(args[1]);

        if (arcade == null || !arcade.getServer().equals(server)) {
            profile.sendMessage("§cO jogo informado não existe ou não é compatível com o servidor.");
            return;
        }

        SlotCategory slot = Arrays.stream(SlotCategory.values()).filter(s -> s.getName().equalsIgnoreCase(args[2]))
                .findFirst()
                .orElse(null);

        if (slot == null || Arrays.stream(arcade.getSlots()).noneMatch(category -> category.equals(slot))) {
            profile.sendMessage("§cO slot informado não existe ou não é suportado neste jogo.");
            return;
        }

        InputMode input = Arrays.stream(InputMode.values()).filter(mode -> mode.name().equalsIgnoreCase(args[3]))
                .findFirst()
                .orElse(null);

        if (input == null) {
            profile.sendMessage("§cA entrada informada não existe.");
            return;
        }

        GameRouteContext route = GameRouteContext.builder()
                .arcade(arcade)
                .slot(slot)
                .input(input)
                .build();

        profile.sendMessage("§aEnviando....");

        profile.redirect(route);
    }

    @Command(name = "serialize", rank = RankCategory.DEV)
    public void serializeCommand(BukkitCommandContext context) {
        Player player = context.getPlayer();

        List<ItemStack> items = Arrays.asList(player.getInventory().getContents());

        List<ItemStack> hotBar = items.subList(0, 9);

        String hotbarSerialized = SerializeInventory.itemStackArrayToBase64(hotBar);

        List<ItemStack> internalItems = items.subList(9, 36); // Ajustado o índice inicial para 9

        String internalSerialized = SerializeInventory.itemStackArrayToBase64(internalItems);

        String inventorySerialized = internalSerialized + " @ " + hotbarSerialized;

        System.out.println("Serialize -> " + inventorySerialized);

        player.sendMessage("§aInventário serializado! Verifique o terminal do servidor.");
    }

    @Command(name = "debug", aliases = {"dbg"}, rank = RankCategory.DEV)
    public void debugCommand(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        int tasks = Bukkit.getScheduler().getPendingTasks().size();
        int actives = Bukkit.getScheduler().getActiveWorkers().size();

        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        Runtime runTime = Runtime.getRuntime();

        if (args.length == 0) {
            sender.sendMessage("§6§lINFO: §eInformações gerais do servidor:");
            sender.sendMessage("");
            sender.sendMessage("§fNome do servidor: §7" + Bukkit.getServer().getServerName());
            sender.sendMessage("§fJogadores online: §7" + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
            sender.sendMessage("");
            sender.sendMessage("§fTPS: " + UtilTPS.getFromMinutesTPS());
            sender.sendMessage(" §fMédia: " + UtilTPS.getMedia());
            sender.sendMessage("");
            sender.sendMessage("§fMemória: " + String.format(MEMORY_FORMAT, DevCommands.findUsedMemory(runTime),
                    DevCommands.findFreeMemory(runTime), DevCommands.findTotalMemory(runTime), DevCommands.findMaxMemory(runTime), ChatColor.DARK_GRAY + "(" + Util.percent((int) DevCommands.findUsedMemory(runTime), (int) DevCommands.findTotalMemory(runTime)) + "%)"));
            sender.sendMessage("");
            sender.sendMessage("§fTarefas pendentes: §c" + tasks);
            sender.sendMessage("§fTarefas em andamento: §a" + actives);
            sender.sendMessage("§fIniciado há: §7" + TimeUtil.formatTime(uptime, TimeUtil.TimeFormat.SHORT));
            sender.sendMessage("");
            return;
        }
        switch (args[0]) {
            case "lag":
            case "clear": {
                System.gc();
                sender.sendMessage("§c§lAVISO: §cSistema limpo!");
                break;
            }
            case "tps":
            case "ticks": {
                sender.sendMessage("§6§lINFO: §eInformações de TPS:");
                sender.sendMessage("");
                sender.sendMessage("§fTPS Atual: " + UtilTPS.getActualyTPS());
                sender.sendMessage("");
                sender.sendMessage("§fTPS: " + UtilTPS.getFromMinutesTPS());
                sender.sendMessage(" §fMédia: " + UtilTPS.getMedia());
                sender.sendMessage("");
                sender.sendMessage("§fTPS §7(Real)§f: " + UtilTPS.getRealMinutesTPS());
                sender.sendMessage(" §fMédia §7(Real)§f: " + UtilTPS.getAverage());
                sender.sendMessage("");
                break;
            }
            case "entity":
            case "entities": {
                World world = Bukkit.getWorlds().get(0);

                sender.sendMessage("§6§lINFO: §eInformações de Entidades:");
                sender.sendMessage("");

                int entities = Bukkit.getWorlds().stream().mapToInt(earth -> earth.getEntities().size()).sum();

                sender.sendMessage("§fTotal de Entidades: §e" + entities);

                Bukkit.getWorlds().forEach(earth -> {
                    sender.sendMessage("§fEntitades em '" + earth.getName() + "': §e" + earth.getEntities().size());
                });

                sender.sendMessage("");
                break;
            }
            default: {
                sender.sendMessage("§cModo de uso: /" + context.getLabel() + " <clear:entities:ticks>.");
                break;
            }
        }
    }

    private static final class UtilTPS {

        public static double[] findTPS() {
            return MinecraftServer.getServer().recentTps;
        }

        public static String getFromMinutesTPS() {
            return format(findTPS()[0]) + "§7(1m), " + format(findTPS()[1]) + "§7(5m), " + format(findTPS()[2]) + "§7(15m)";
        }

        public static String getRealMinutesTPS() {
            return real(findTPS()[0]) + "§7(1m), " + real(findTPS()[1]) + "§7(5m), " + real(findTPS()[2]) + "§7(15m)";
        }

        public static String getActualyTPS() {
            return real(findTPS()[0]);
        }

        public static String getMedia() {
            return format(((findTPS()[0]) + findTPS()[1] + findTPS()[2]) / 3);
        }

        public static String getAverage() {
            return real(((findTPS()[0]) + findTPS()[1] + findTPS()[2]) / 3);
        }

        private static String format(double tps) {
            return String.valueOf(
                    ((tps > 18.0) ? org.bukkit.ChatColor.GREEN : ((tps > 16.0) ? org.bukkit.ChatColor.YELLOW : org.bukkit.ChatColor.RED)).toString())
                    + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
        }

        private static String real(double tps) {
            return String.valueOf(
                    ((tps > 18.0) ? org.bukkit.ChatColor.GREEN : ((tps > 16.0) ? org.bukkit.ChatColor.YELLOW : org.bukkit.ChatColor.RED)).toString())
                    + ((tps > 40.0) ? "*" : "") + new DecimalFormat("##.#").format(tps);
        }

    }

    protected static long findUsedMemory(Runtime runTime) {
        return (runTime.totalMemory() - runTime.freeMemory()) / 1048576L;
    }

    protected static long findFreeMemory(Runtime runTime) {
        return runTime.freeMemory() / 1048576L;
    }

    protected static long findTotalMemory(Runtime runTime) {
        return runTime.totalMemory() / 1048576L;
    }

    protected static long findMaxMemory(Runtime runTime) {
        return runTime.maxMemory() / 1048576L;
    }
}
