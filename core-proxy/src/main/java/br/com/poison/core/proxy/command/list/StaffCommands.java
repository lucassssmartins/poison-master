package br.com.poison.core.proxy.command.list;

import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.preference.Preference;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.proxy.command.structure.ProxyCommandContext;

public class StaffCommands implements CommandInheritor {

    @Command(name = "broadcast", aliases = {"bc", "anuncio", "aviso"}, rank = RankCategory.OWNER, onlyPlayer = false)
    public void broadcast(ProxyCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.sendMessage("§cUso: /" + context.getLabel() + " <message>.");
            return;
        }

        String message = context.getMessage(0, args);

        broadcast(message, true);

        log(sender, sender.getName() + " enviou uma mensagem global");
    }

    @Command(name = "staffchat", aliases = {"sc"}, rank = RankCategory.PLUS_MEDIA)
    public void staffChat(ProxyCommandContext context) {
        Profile profile = context.getProfile();

        Preference preference = profile.getPreference();

        preference.setInStaffChat(!preference.isInStaffChat());
        profile.savePreference(preference);

        profile.sendMessage((preference.isInStaffChat() ? "§aVocê entrou no" : "§cVocê saiu do")
                + " bate-papo da equipe.");
    }
}
