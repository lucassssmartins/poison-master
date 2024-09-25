package br.com.poison.core.command.inheritor;

import br.com.poison.core.Constant;
import br.com.poison.core.command.sender.CommandSender;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.Core;

public interface CommandInheritor {

    String TARGET_NOT_FOUND = "§cO alvo solicitado não foi encontrado.";

    default void broadcast(String message) {
        broadcast(message, false);
    }

    default void broadcast(String message, boolean prefix) {
        Core.getProfileManager().documents().forEach(profile -> profile.sendMessage((prefix ? Constant.SERVER_PREFIX : "") + message));
    }

    default void broadcast(RankCategory rank, String... message) {
        Core.getProfileManager()
                .documents(profile -> profile.hasRank(rank))
                .forEach(profile -> profile.sendMessage(message));
    }

    default void log(CommandSender sender, String message) {
        Core.getProfileManager().log(sender, message);
    }
}
