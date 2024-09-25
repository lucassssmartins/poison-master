package br.com.poison.arcade.pvp.command;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class KitCommand implements CommandInheritor {

    protected void handleKitCommand(BukkitCommandContext context, KitType type) {
        Player player = context.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (!user.getGame().getCategory().equals(ArcadeCategory.ARENA)) return;

        if (!user.isProtected()) {
            player.sendMessage("§cNão é possível selecionar kits neste momento!");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso correto: /" + context.getLabel() + " <kit>.");
            return;
        }

        Kit kit = PvP.getKitManager().fetch(type, args[0]);

        if (kit == null) {
            player.sendMessage("§cO kit solicitado não existe!");
            return;
        }

        if (!kit.hasPermission(player.getUniqueId())) {
            player.sendMessage(new String[]{
                    "§cVocê não possui este kit ;(",
                    "§cCompre-o em nossa §eLoja de Kits§c!"});
            return;
        }

        Kit selected = type.equals(KitType.PRIMARY) ? user.getSecondaryKit() : user.getPrimaryKit();

        if (selected != null && !kit.getProhibitedCombinations().isEmpty()
                && kit.getProhibitedCombinations().stream().anyMatch(name -> selected.getName().equalsIgnoreCase(name))) {
            player.sendMessage("§cA combinação entre os kits " + selected.getName() + " e " + kit.getName() + " é proibida!");
            return;
        }

        if (user.isUsingKit(kit.getName())) {
            player.sendMessage("§cVocê já está usando este kit!");
            return;
        }

        if (type.equals(KitType.PRIMARY))
            user.setPrimaryKit(kit);
        else
            user.setSecondaryKit(kit);

        Core.getMultiService().async(() -> user.getGame().sendSidebar(user));

        player.sendMessage("§aO kit " + kit.getName() + " foi selecionado.");
    }

    @Command(name = "kit")
    public void kitCommand(BukkitCommandContext context) {
        handleKitCommand(context, KitType.PRIMARY);
    }

    @Command(name = "kit2")
    public void kit2Command(BukkitCommandContext context) {
        handleKitCommand(context, KitType.SECONDARY);
    }
}
