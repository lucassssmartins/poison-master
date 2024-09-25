package br.com.poison.core.bukkit.api.user.medal;

import br.com.poison.core.Constant;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.medal.Medal;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MedalController {

    protected static List<Medal> getAvailableMedals(Profile profile) {
        return Arrays.stream(Medal.values())
                .filter(profile::hasMedal)
                .collect(Collectors.toList());
    }

    protected static TextComponent create(Profile profile, Medal medal, boolean end) {
        TextComponent message = new TextComponent(medal.getColoredSymbol() + "§r" + (end ? "." : ", "));

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                medal.getColoredName()
                + "\n§7" + medal.getLore()
                + "\n\n" + (profile.isUsingMedal(medal) ? "§cEm uso!" : "§aClique para usar!")
        )));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/medalha " + medal.getName().toLowerCase()));

        return message;
    }

    public static void sendMedals(Profile profile) {
        if (profile.getRelation().getMedals().isEmpty()) {
            profile.sendMessage("§cVocê não tem medalhas para usar ;(",
                    "§cCompre medalhas em nossa loja: §e" + Constant.STORE);
            return;
        }

        List<Medal> medals = getAvailableMedals(profile);

        TextComponent message = new TextComponent("§aSuas medalhas: ");

        boolean end = false;

        int index = 1;
        for (Medal medal : medals) {
            if (index >= medals.size())
                end = true;

            message.addExtra(create(profile, medal, end));
            index++;
        }

        profile.sendMessage(message);
    }
}
