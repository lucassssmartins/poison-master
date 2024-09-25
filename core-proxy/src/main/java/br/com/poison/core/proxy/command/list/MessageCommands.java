package br.com.poison.core.proxy.command.list;

import br.com.poison.core.Constant;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.proxy.command.structure.ProxyCommandContext;

public class MessageCommands implements CommandInheritor {

    @Command(name = "message", aliases = {"msg", "tell", "w", "whisper"})
    public void message(ProxyCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (!profile.getPreference().isAllowDirectMessages()) {
            profile.sendMessage("§cAs suas mensagens diretas estão desativadas. Ative-as novamente no menu de preferências!");
            return;
        }

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <player> <message>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null || target.proxiedPlayer() == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (profile.equals(target)) {
            profile.sendMessage("§cVocê não pode enviar mensagens para você mesmo.");
            return;
        }

        if (!target.getPreference().isAllowDirectMessages()) {
            profile.sendMessage("§cEste jogador não está aceitando mensagens diretas.");
            return;
        }

        String content = context.getMessage(1, args);

        if (content == null || content.isEmpty()) {
            profile.sendMessage("§cA sua mensagem não pode ser vázia.");
            return;
        }

        profile.setLastMessage(target.getId());
        target.setLastMessage(profile.getId());

        profile.sendMessage("§8[§7Mensagem para " + target.getRank().getColor() + target.getName() + "§8]: §f" + content);
        target.sendMessage("§8[§7Mensagem de " + profile.getRank().getColor() + profile.getName() + "§8]: §f" + content);
    }

    @Command(name = "reply", aliases = {"responder", "r"})
    public void reply(ProxyCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        if (!profile.hasLastMessage()) {
            profile.sendMessage("§cVocê não tem mensagens para responder!");
            return;
        }

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getLabel() + " <message>.");
            return;
        }

        Profile target = context.getProfile(profile.getRelation().getLastMessage());

        profile.setLastMessage(Constant.CONSOLE_UUID);

        if (target == null || target.proxiedPlayer() == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        String content = context.getMessage(0, args);

        if (content == null || content.isEmpty()) {
            profile.sendMessage("§cA sua mensagem não pode ser vázia.");
            return;
        }

        target.setLastMessage(profile.getId());

        profile.sendMessage("§8[§7Resposta para " + target.getRank().getColor() + target.getName() + "§8]: §f" + content);
        target.sendMessage("§8[§7Resposta de " + profile.getRank().getColor() + profile.getName() + "§8]: §f" + content);
    }
}
