package br.com.poison.lobby.command;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.mode.InputMode;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;

import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.manager.InvitationManager;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.invitation.Invitation;
import br.com.poison.core.resources.invitation.type.DuelInvitation;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.lobby.inventory.tracker.mode.ModeTrackerInventory;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

public class DuelCommand implements CommandInheritor {

    private final InvitationManager manager;

    public DuelCommand() {
        manager = Core.getInvitationManager();
    }

    @Command(name = "duel", aliases = {"duelar"})
    public void duel(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = Lobby.getUserManager().read(player.getUniqueId());
        Profile account = user.getProfile();

        String[] args = context.getArgs();

        if (!user.inArcade(ArcadeCategory.DUELS)) return;

        if (args.length == 0) {
            account.sendMessage("§cUso: /" + context.getLabel() + " <jogador> <modo>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null || !target.isOnline() || target.equals(account)) {
            account.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (manager.hasPendentRequest(account.getId(), target.getId())) {
            Invitation invitation = manager.read(account.getId(), target.getId());

            account.sendMessage("§cAguarde " + TimeUtil.newFormatTime(invitation.getExpiresAt())
                    + " para convidar este jogador novamente!");
            return;
        }

        if (args.length == 1) {
            new ModeTrackerInventory(player, target.player()).init();
            return;
        }

        if (args[1].equalsIgnoreCase("aceitar")) {
            DuelInvitation invite = (DuelInvitation) manager.read(target.getId(), account.getId());

            if (invite == null || invite.getRoute() == null || !manager.hasPendentRequest(target.getId(), account.getId())) {
                account.sendMessage("§cNenhum convite foi encontrado!");
                return;
            }

            target.sendMessage("§aO jogador " + account.getName() + " aceitou o seu convite!");
            account.sendMessage("§eVocê aceitou o convite do jogador §b" + target.getName() + "§e.");

            GameRouteContext route = invite.getRoute();

            account.redirect(route);
            target.redirect(route);

            manager.remove(invite);
            return;
        }

        if (args[1].equalsIgnoreCase("recusar")) {
            DuelInvitation invite = (DuelInvitation) manager.read(target.getId(), account.getId());

            if (invite == null || !manager.hasPendentRequest(target.getId(), account.getId())) {
                account.sendMessage("§cNenhum convite foi encontrado!");
                return;
            }

            manager.remove(invite);

            target.sendMessage("§cO jogador " + account.getName() + " rejeitou o seu convite!");
            account.sendMessage("§eVocê recusou o convite do jogador §b" + target.getName() + "§e.");

            return;
        }

        ArcadeCategory arcade = ArcadeCategory.fetch(args[1]);

        if (arcade == null || !arcade.getServer().equals(ServerCategory.DUELS)) {
            account.sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        /* Criando Convite */
        new DuelInvitation(GameRouteContext.builder()
                .arcade(arcade)
                .slot(SlotCategory.SOLO)
                .input(InputMode.PLAYER)
                .link(new HashSet<>(Arrays.asList(player.getUniqueId(), target.getId())))
                .build(),
                player.getUniqueId(), target.getId(), 30);

        account.sendMessage("§aVocê convidou " + target.getName() + " para um duelo de " + arcade.getName() + "!");

        // Mensagem de convite
        TextComponent message = new TextComponent("§eVocê recebeu um convite do jogador §b" + account.getName() + "§e para duelar, em uma partida de §6" + arcade.getName()
                + "§e, você deseja ");

        TextComponent accept = new TextComponent("§a§lACEITAR");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel " + player.getName() + " aceitar"));

        TextComponent refuse = new TextComponent("§c§lRECUSAR");
        refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel " + player.getName() + " recusar"));

        message.addExtra(accept);
        message.addExtra("§e ou ");

        message.addExtra(refuse);
        message.addExtra("§e o convite?");

        target.sendMessage(message);
    }
}
