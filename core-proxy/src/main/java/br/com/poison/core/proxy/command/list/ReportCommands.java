package br.com.poison.core.proxy.command.list;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.proxy.command.structure.ProxyCommandContext;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.resources.report.Report;

import java.util.concurrent.TimeUnit;

public class ReportCommands implements CommandInheritor {

    @Command(name = "report", aliases = {"denunciar", "rp"})
    public void report(ProxyCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (profile.hasCooldown(Constant.REPORT_COOLDOWN_KEY)) {
            profile.sendMessage("§cAguarde " + TimeUtil.newFormatTime(profile.getCooldown(Constant.REPORT_COOLDOWN_KEY))
                    + " para denunciar novamente.");
            return;
        }

        if (args.length <= 1) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <jogador> <motivo>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null || target.proxiedPlayer() == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (target.equals(profile)) {
            profile.sendMessage("§cVocê não pode reportar a si mesmo.");
            return;
        }

        String reason = context.getMessage(1, args);

        if (reason == null || reason.isEmpty()) {
            profile.sendMessage("§cVocê precisa inserir o motivo da denúncia!");
            return;
        }

        broadcast(RankCategory.TRIAL, "",
                " §6§lNOVA DENÚNCIA ",
                "",
                "§7* §fAcusado: §e" + target.getName(),
                "§7* §fVítima: §e" + profile.getName(),
                "§7* §fMotivo: §7" + reason,
                "§7* §fServidor: §e" + target.getServer().getName(),
                "");

        profile.setCooldown(Constant.REPORT_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(30));

        profile.sendMessage("§aSua denúncia foi enviada com sucesso. Obrigado por nós ajudar a manter o servidor seguro!");
    }
}
