package br.com.poison.core.bukkit.command.list.special;

import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.annotation.Completer;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.Constant;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.profile.Profile;

import java.util.ArrayList;
import java.util.List;

public class TagCommand implements CommandInheritor {

    @Command(name = "tag")
    public void tag(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        String[] args = context.getArgs();

        List<Tag> availableTags = TagController.getAvailableTags(profile);

        if (availableTags.stream().allMatch(tag -> tag.equals(Tag.PLAYER))) {
            profile.sendMessage("§cVocê não tem tags para usar ;(",
                    "§cCompre tags em nossa loja: §e" + Constant.STORE);
            return;
        }

        if (args.length == 0) {
            TagController.sendTags(profile);
            return;
        }

        Tag tag = Tag.fetch(args[0]);

        if (tag == null || !profile.hasTag(tag)) {
            profile.sendMessage("§cA tag solicitada não existe ou você não a possui.");
            return;
        }

        if (profile.isUsingTag(tag)) {
            profile.sendMessage("§cVocê já está usando a tag " + tag.getColoredName() + "§c.");
            return;
        }

        profile.setTag(tag);
        TagController.updateTag(context.getPlayer());

        profile.sendMessage("§aVocê selecionou a tag " + tag.getColoredName() + "§a.");
    }

    @Completer(name = "tag")
    public List<String> tagCompleter(BukkitCommandContext context) {
        List<String> tagList = new ArrayList<>();

        String[] args = context.getArgs();

        Profile profile = context.getProfile();

        List<Tag> availableTags = TagController.getAvailableTags(profile);

        if (args.length > 0 && !args[0].isEmpty()) {
            String filter = args[0].toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

            for (Tag tag : availableTags) {
                String name = tag.getName().toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

                if (name.startsWith(filter))
                    tagList.add(tag.getName());
            }
        } else {
            for (Tag tag : availableTags) {
                tagList.add(tag.getName());
            }
        }

        return tagList;
    }
}
