package br.com.poison.core.proxy.command.list;

import br.com.poison.core.Constant;
import br.com.poison.core.backend.data.PunishmentData;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.proxy.command.structure.ProxyCommandContext;
import br.com.poison.core.resources.punishment.reason.PunishmentReason;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.Core;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.punishment.Punishment;
import br.com.poison.core.resources.punishment.category.PunishmentCategory;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.List;

public class PunishmentCommands implements CommandInheritor {

    private final PunishmentData data = Core.getPunishmentData();

    protected void handlePunishment(ProxyCommandContext context, PunishmentCategory category) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            sender.sendMessage("§cUso: /" + context.getLabel() + " <player> <motivo>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            sender.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (sender.equals(target.getId())) {
            sender.sendMessage("§cVocê não pode punir a si mesmo.");
            return;
        }

        if (target.getRelation().getPunishment().isByPass()) {
            sender.sendMessage("§cVocê não pode punir este jogador.");
            return;
        }

        if (category.equals(PunishmentCategory.BAN) && data.hasActiveBan(target) != null) {
            sender.sendMessage("§cEste jogador já está banido no servidor!");
            return;
        } else if (category.equals(PunishmentCategory.MUTE) && data.hasActiveMute(target.getId())) {
            sender.sendMessage("§cEste jogador já está mutado no servidor!");
            return;
        }

        PunishmentReason reason = PunishmentReason.fetch(args[1], category);

        if (reason == null) {
            StringBuilder builder = new StringBuilder();

            List<PunishmentReason> reasons = PunishmentReason.list(category);

            int index = 1;
            boolean end = false;
            for (PunishmentReason punishment : reasons) {
                builder.append(punishment.name()).append(end ? "." : ", ");

                if (index >= reasons.size())
                    end = true;

                index++;
            }

            sender.sendMessage("§cOs motivos disponíveis são: " + builder);
            return;
        }

        Punishment punishment = new Punishment(target,
                sender.getUuid(), sender.getName(), target.getIpAddress(), category, reason);

        if (category.equals(PunishmentCategory.BAN)) {
            ProxiedPlayer player = context.getProxied(target.getId());

            if (player != null)
                player.disconnect(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                        punishment.isPermanent() ? "permanentemente" : "temporariamente",
                        punishment.getReason().getInfo(),
                        (!punishment.isPermanent() ? "§cExpira em: " + TimeUtil.formatTime(punishment.getExpiresAt()) : "") + "\n",
                        punishment.getCode()
                )));
        } else if (category.equals(PunishmentCategory.MUTE))
            target.sendMessage("§cVocê foi silenciado " + (punishment.isPermanent() ? "permanentemente" : "temporariamente") + " no servidor.");

        sender.sendMessage("§aPunição em §e" + target.getName() + "§a aplicada com sucesso.");

        data.input(punishment);

        log(sender, sender.getName() + " " + (category.equals(PunishmentCategory.BAN) ? "baniu" : "silenciou")
                + " o jogador " + target.getName() + " por " + reason.getInfo());
    }

    @Command(name = "kick", aliases = {"expulsar"}, rank = RankCategory.TRIAL, onlyPlayer = false)
    public void kick(ProxyCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            sender.sendMessage("§cUso: /" + context.getLabel() + " <jogador> <motivo>.");
            return;
        }

        Profile profile = context.getProfile(args[0]);

        if (profile == null || profile.proxiedPlayer() == null) {
            sender.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (sender.equals(profile.getId())) {
            sender.sendMessage("§cVocê não pode expulsar a si mesmo.");
            return;
        }

        if (sender.isPlayer() && !context.getProfile().hasRank(profile.getRank().getCategory())) {
            sender.sendMessage("§cVocê precisa ser " + profile.getRank().getPrefix()
                    + "§c ou superior para expulsar este jogador.");
            return;
        }

        String reason = context.getMessage(1, args);

        if (reason == null || reason.isEmpty()) {
            sender.sendMessage("§cO motivo precisa ser informado!");
            return;
        }

        ProxiedPlayer proxied = profile.proxiedPlayer();

        proxied.disconnect(TextComponent.fromLegacyText(
                "§cVocê foi expulso do servidor.\n\n"
                        + "§cMotivo da expulsão: " + reason + "\n\n"
                        + "§cExpulsão injusta? Contate-nos em: §e" + Constant.DISCORD + "\n"
        ));

        sender.sendMessage("§aO jogador " + proxied.getName() + " foi expulso do servidor.");

        log(sender, sender.getName() + " expulsou o jogador " + proxied.getName() + " por: " + reason);
    }

    @Command(name = "ban", aliases = {"banir"}, rank = RankCategory.TRIAL, onlyPlayer = false)
    public void ban(ProxyCommandContext context) {
        handlePunishment(context, PunishmentCategory.BAN);
    }

    @Command(name = "cban", rank = RankCategory.TRIAL, onlyPlayer = false)
    public void cheatingBan(ProxyCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.sendMessage("§cUso: /" + context.getLabel() + " <player>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            sender.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (sender.equals(target.getId())) {
            sender.sendMessage("§cVocê não pode punir a si mesmo.");
            return;
        }

        if (target.getRelation().getPunishment().isByPass()) {
            sender.sendMessage("§cVocê não pode banir este jogador.");
            return;
        }

        if (data.hasActiveBan(target) != null) {
            sender.sendMessage("§cEste jogador já está banido no servidor!");
            return;
        }

        Punishment ban = new Punishment(target, sender.getUuid(), sender.getName(),
                target.getIpAddress(), PunishmentCategory.BAN, PunishmentReason.CHEATING);

        ProxiedPlayer player = target.proxiedPlayer();

        if (player != null)
            player.disconnect(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                    ban.isPermanent() ? "permanentemente" : "temporariamente",
                    ban.getReason().getInfo(),
                    (!ban.isPermanent() ? "§cExpira em: " + TimeUtil.formatTime(ban.getExpiresAt()) : "") + "\n",
                    ban.getCode()
            )));

        sender.sendMessage("§cO jogador " + target.getName() + " foi banido por " + PunishmentReason.CHEATING.getInfo() + ".");

        data.input(ban);

        log(sender, sender.getName() + " baniu o jogador " + target.getName() + " por " + PunishmentReason.CHEATING.getInfo());
    }

    @Command(name = "mute", aliases = {"mutar", "silenciar"}, rank = RankCategory.TRIAL, onlyPlayer = false)
    public void mute(ProxyCommandContext context) {
        handlePunishment(context, PunishmentCategory.MUTE);
    }

    @Command(name = "pardon", rank = RankCategory.TRIAL, onlyPlayer = false)
    public void pardon(ProxyCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            sender.sendMessage("§cUso: /" + context.getLabel() + " <player> <category>");
            return;
        }

        Profile profile = context.getProfile(args[0]);

        if (profile == null) {
            sender.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        PunishmentCategory category = Arrays.stream(PunishmentCategory.values())
                .filter(c -> c.name().equalsIgnoreCase(args[1]))
                .findFirst()
                .orElse(null);

        if (category == null) {
            sender.sendMessage("§cTipo de categoria não encontrada!");
            return;
        }

        if (category.equals(PunishmentCategory.BAN) && data.hasActiveBan(profile) == null) {
            sender.sendMessage("§cO jogador não possui banimentos ativos.");
            return;
        } else if (category.equals(PunishmentCategory.MUTE) && !data.hasActiveMute(profile.getId())) {
            sender.sendMessage("§cO jogador não possui silenciamentos ativos.");
            return;
        }

        Punishment punishment = data.getActivePunishment(profile.getId(), category);

        if (punishment == null) {
            sender.sendMessage("§cNenhuma punição foi encontrada!");
            return;
        }

        data.delete(punishment);

        sender.sendMessage("§cPunição de " + profile.getName() + " perdoada com sucesso.");

        log(sender, sender.getName() + " perdoou o " + category.name() + " de " + profile.getName());
    }
}
