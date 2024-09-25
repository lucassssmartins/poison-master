package br.com.poison.core.bukkit.command.list.special;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.medal.Medal;
import br.com.poison.core.profile.resources.medal.info.MedalInfo;
import br.com.poison.core.profile.resources.permission.Permission;
import br.com.poison.core.profile.resources.rank.Rank;
import br.com.poison.core.profile.resources.rank.assignment.Assignment;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.DateUtil;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.command.annotation.Command;
import org.bukkit.entity.Player;

import java.util.Set;

public class ProfileCommand implements CommandInheritor {

    @Command(name = "profile", aliases = {"account", "acc", "conta", "perfil"}, onlyPlayer = false, runAsync = true)
    public void profile(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        Profile profile = context.getProfile();
        String[] args = context.getArgs();

        if (args.length == 0) {
            if (!sender.isPlayer())
                sender.sendMessage("§cUso do /" + context.getLabel() + ":",
                        "§c* /" + context.getLabel() + " <player> - §eVer as informações de um jogador.",
                        "§c* /" + context.getLabel() + " <player> rank <set/info> - §eSetar ou ver as informações do rank de um jogador.",
                        "§c* /" + context.getLabel() + " <player> medal <set/remove/list> - §eSetar, remover ou listar as medalhas do jogador.",
                        "§c* /" + context.getLabel() + " <player> tag <set/remove> - §eSetar ou remover a tag do jogador.",
                        "§c* /" + context.getLabel() + " <player> permission <set/remove/list> - §eSetar, remover ou listar as permissões do jogador.",
                        "§c* /" + context.getLabel() + " <Player> cash <add/remove> - §eAdicionar ou remover cash do jogador.");
            else
                sendProfileInfo(context, profile);

            return;
        }

        if (sender.isPlayer() && !profile.hasRank(RankCategory.MOD)) {
            sender.sendMessage("§cVocê não pode ver o perfíl de outros jogadores.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            sender.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (args.length == 1) {
            sendProfileInfo(context, target);
            return;
        }

        if (args.length == 2) {
            sender.sendMessage("§cUso do /" + context.getLabel() + ":",
                    "§c* /" + context.getLabel() + " <player> rank <set/info>.",
                    "§c* /" + context.getLabel() + " <player> medal <set/remove/list>.",
                    "§c* /" + context.getLabel() + " <player> tag <set/remove>.",
                    "§c* /" + context.getLabel() + " <player> permission <set/remove/list>.");
            return;
        }

        String request = args[1], search = args[2];

        if (request.equalsIgnoreCase("rank")) {
            switch (search.toLowerCase()) {
                case "set": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> rank set <rank> [time]");
                        return;
                    }

                    RankCategory category = RankCategory.fetch(args[3]);

                    if (category == null || (sender.isPlayer() && !profile.hasRank(category))) {
                        sender.sendMessage("§cO rank informado não existe ou é superior ao seu.");
                        return;
                    }

                    if (category.equals(RankCategory.CM) && !Core.isDeveloper(target.getId())) {
                        sender.sendMessage("§cHaha! O rank " + RankCategory.CM.getPrefix() + "§c só pode ser setado na conta de §6CassioMartim§c.");
                        return;
                    }

                    if (target.hasOnlyRank(category)) {
                        sender.sendMessage("§cO jogador já possui este rank!");
                        return;
                    }

                    long expiresAt = -1L;
                    if (args.length >= 5)
                        expiresAt = TimeUtil.getTime(args[4]);

                    Rank rank = new Rank(category, (sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE), sender.getUuid(), expiresAt);

                    target.setRank(rank);

                    sender.sendMessage("§aO rank " + rank.getPrefix() + "§a foi atribuído a conta de §e" + target.getName()
                            + "§a com duração " + (rank.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");
                    break;
                }

                case "info": {
                    Rank rank = target.getRank();

                    sender.sendMessage("§aRank: " + rank.getPrefix(),
                            "§aAtribuído por: §f" + rank.getAuthorName(),
                            "§8* §7§o" + rank.getAssignment().getMessage(),
                            "",
                            "§aExpira em: §f" + (rank.isPermanent() ? "Nunca" : TimeUtil.formatTime(rank.getExpiresAt())),
                            "§aÚltima atualização: §f" + DateUtil.getDate(rank.getAssignedAt()));
                    break;
                }
            }

            return;
        }

        if (request.equalsIgnoreCase("medal")) {
            switch (search.toLowerCase()) {
                case "set": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> medal set <medal> [time]");
                        return;
                    }

                    Medal medal = Medal.fetch(args[3]);

                    if (medal == null || medal.equals(Medal.VOID) || target.hasMedal(medal)) {
                        sender.sendMessage("§cA medalha informada não existe ou o jogador já possui.");
                        return;
                    }

                    long expiresAt = -1L;
                    if (args.length >= 5)
                        expiresAt = TimeUtil.getTime(args[4]);

                    MedalInfo info = new MedalInfo(medal, sender.getUuid(), expiresAt);

                    target.addMedals(info);

                    target.sendMessage("§aA medalha " + medal.getColoredName() + "§a foi atribuída a sua conta por §e" + sender.getName()
                            + "§a com duração " + (info.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");

                    sender.sendMessage("§aVocê atribuiu a medalha " + medal.getColoredName() + "§a na conta de §e" + target.getName()
                            + "§a com duração " + (info.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");
                    break;
                }

                case "remove": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> medal remove <medal>");
                        return;
                    }

                    Medal medal = Medal.fetch(args[3]);

                    if (medal == null || medal.equals(Medal.VOID) || !target.hasMedal(medal)) {
                        sender.sendMessage("§cA medalha informada não existe ou o jogador não possui.");
                        return;
                    }

                    target.removeMedals(medal);

                    target.sendMessage("§cA medalha " + medal.getColoredName() + "§c foi removida da sua conta por " + sender.getName() + ".");
                    sender.sendMessage("§aA medalha " + medal.getColoredName() + "§a foi removida da conta de §e" + target.getName() + "§a.");
                    break;
                }

                case "list": {
                    Set<MedalInfo> medals = target.getRelation().getMedals();

                    if (medals.isEmpty()) {
                        sender.sendMessage("§cEste jogador ainda não possui medalhas ;(");
                        return;
                    }

                    sender.sendMessage("§aMedalhas disponíveis (" + medals.size() + "):");
                    for (MedalInfo info : medals) {
                        if (info == null) continue;

                        Medal medal = info.getMedal();

                        sender.sendMessage("",
                                "§aMedalha: §f" + medal.getName(),
                                "§aSímbolo: " + medal.getColoredSymbol(),
                                "",
                                "§aAtribuído por: §f" + info.getAuthorName(),
                                "§aAtribuído em: §f" + DateUtil.getDate(info.getAssignedAt()),
                                "",
                                "§aExpira em: §f" + (info.isPermanent() ? "Nunca" : TimeUtil.formatTime(info.getExpiresAt())));
                    }

                    break;
                }
            }
        }

        if (request.equalsIgnoreCase("tag")) {
            switch (search.toLowerCase()) {
                case "set": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> tag set <tag> [time]");
                        return;
                    }

                    Tag tag = Tag.fetch(args[3]);

                    if (tag == null || !tag.isReward()) {
                        sender.sendMessage("§cA tag informada não existe ou não é uma tag de recompensa.");
                        return;
                    }

                    if (target.hasPermission(tag.getPermission())) {
                        sender.sendMessage("§cO jogador já tem a tag " + tag.getColoredName() + "§c.");
                        return;
                    }

                    long expiresAt = -1L;
                    if (args.length >= 5)
                        expiresAt = TimeUtil.getTime(args[4]);

                    Permission permission = new Permission(tag.getPermission(), sender.getUuid(), expiresAt);

                    target.setPermission(permission);

                    target.sendMessage("§aA tag " + tag.getColoredName() + "§a foi atribuída em sua conta por §e" + sender.getName()
                            + "§a com duração " + (permission.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");

                    sender.sendMessage("§aVocê atribuiu a tag " + tag.getColoredName() + "§a na conta de §e" + target.getName()
                            + "§a com duração " + (permission.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");
                    break;
                }

                case "remove": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> tag remove <tag>");
                        return;
                    }

                    Tag tag = Tag.fetch(args[3]);

                    if (tag == null || !tag.isReward()) {
                        sender.sendMessage("§cA tag informada não existe ou não é uma tag de recompensa.");
                        return;
                    }

                    if (!target.hasPermission(tag.getPermission())) {
                        sender.sendMessage("§cO jogador não tem a tag " + tag.getColoredName() + "§c.");
                        return;
                    }

                    target.removeCooldown(tag.getPermission());

                    target.sendMessage("§cA tag " + tag.getColoredName() + "§a foi removida da sua conta por §e" + sender.getName() + "§a.");
                    sender.sendMessage("§cVocê removeu a tag " + tag.getColoredName() + "§a da conta do jogador §e" + target.getName() + "§a.");
                    break;
                }
            }
        }

        if (request.equalsIgnoreCase("permission")) {
            switch (search.toLowerCase()) {
                case "set": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> permission set <key> [time]");
                        return;
                    }

                    String key = args[3];

                    if (target.hasPermission(key)) {
                        sender.sendMessage("§cO jogador já possui essa permissão!");
                        return;
                    }

                    long expiresAt = -1L;
                    if (args.length >= 5)
                        expiresAt = TimeUtil.getTime(args[4]);

                    Permission permission = new Permission(key, sender.getUuid(), expiresAt);

                    target.setPermission(permission);

                    target.sendMessage("§aA permissão " + key + " foi atribuída em sua conta por §e" + sender.getName()
                            + "§a com duração " + (permission.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");

                    sender.sendMessage("§aVocê atribuiu a permissão " + key + " na conta de §e" + target.getName()
                            + "§a com duração " + (permission.isPermanent() ? "vitalícia" : "de " + TimeUtil.formatTime(expiresAt)) + ".");
                    break;
                }

                case "remove": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> permission remove <key>");
                        return;
                    }

                    String key = args[3];

                    if (!target.hasPermission(key)) {
                        sender.sendMessage("§cO jogador não possui essa permissão!");
                        return;
                    }

                    target.removePermission(key);

                    target.sendMessage("§aA permissão " + key + " foi removida da sua conta por §e" + sender.getName() + "§a.");
                    sender.sendMessage("§aVocê removeu a permissão " + key + " da conta do jogador §e" + target.getName() + "§a.");
                    break;
                }

                case "list": {
                    Set<Permission> permissions = target.getRelation().getPermissions();

                    if (permissions.isEmpty()) {
                        sender.sendMessage("§cEste jogador não possui permissões.");
                        return;
                    }

                    StringBuilder builder = new StringBuilder();

                    int index = 1;
                    for (Permission permission : permissions) {
                        builder.append(permission.getKey());

                        if (index >= permissions.size())
                            builder.append(".");
                        else
                            builder.append(", ");

                        index++;
                    }

                    sender.sendMessage("§aPermissões (" + permissions.size() + "): §f" + builder);
                    break;
                }
            }
        }

        if (request.equalsIgnoreCase("cash")) {
            switch (search.toLowerCase()) {
                case "add": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> cash add <value>");
                        return;
                    }

                    if (!Util.isNumber(args[3])) {
                        sender.sendMessage("§cSomente números são válidos para dar o cash!");
                        return;
                    }

                    int cash = Integer.parseInt(args[3]);

                    if (cash <= 0) {
                        sender.sendMessage("§cO valor de cash não pode ser 0 ou negativo.");
                        return;
                    }

                    target.addCash(cash);

                    target.sendMessage("§eO §a" + sender.getName() + "§e adicionou §2" + Util.formatNumber(cash) + " cash§e em sua conta.");
                    sender.sendMessage("§eVocê adicionou §2" + Util.formatNumber(cash) + " cash§e na conta de §e" + target.getName() + "§e.");
                    break;
                }

                case "remove": {
                    if (args.length == 3) {
                        sender.sendMessage("§cUso: /" + context.getLabel() + " <player> cash remove <value>");
                        return;
                    }

                    if (!Util.isNumber(args[3])) {
                        sender.sendMessage("§cSomente números são válidos para remover o cash!");
                        return;
                    }

                    int cash = Integer.parseInt(args[3]);

                    if (cash <= 0) {
                        sender.sendMessage("§cO valor de cash não pode ser 0 ou negativo.");
                        return;
                    }

                    target.removeCash(cash);

                    target.sendMessage("§eO §a" + sender.getName() + "§e removeu §2" + Util.formatNumber(cash) + " cash§e da sua conta.");
                    sender.sendMessage("§eVocê removeu §2" + Util.formatNumber(cash) + " cash§e da conta de §e" + target.getName() + "§e.");
                    break;
                }
            }
        }
    }


    protected void sendProfileInfo(BukkitCommandContext context, Profile target) {
        Player player = context.getPlayer();
        CommandSender sender = context.getSender();

        sender.sendMessage("§fUsuário: §a" + target.getName(),
                "§fTipo de conta: §a" + target.getProfileType(),
                "",
                "§fRank: " + target.getRank().getColoredName(),
                "§7- §e§o" + target.getRank().getAssignment().getMessage(),
                "",
                "§fPrimeira vez visto em §7" + DateUtil.getDate(target.getRelation().getFirstEntry()),
                "§fVisto por último em §7" + DateUtil.getDate(target.getRelation().getLastEntry()));
    }
}
