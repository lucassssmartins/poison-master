package br.com.poison.core.bukkit.command.list.special;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.bukkit.manager.PlayerManager;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.resources.skin.Skin;
import br.com.poison.core.resources.skin.category.SkinCategory;
import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.util.extra.Validator;
import br.com.poison.core.util.mojang.UUIDFetcher;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SkinCommand implements CommandInheritor {

    @Command(name = "skin", runAsync = true)
    public void skinCommand(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " <nome>.");
            return;
        }

        String skinName = args[0];

        if (skinName.equalsIgnoreCase("reset") || skinName.equalsIgnoreCase("#")) {
            if (profile.hasSkin(profile.getName())) {
                profile.sendMessage("§cVocê precisa estar usando uma skin para restaurar!");
                return;
            }

            profile.sendMessage("§eAguarde...");

            try {
                profile.resetSkin();

                PlayerManager.changePlayerSkin(player, profile.getSkin());

                profile.sendMessage("§cA sua skin foi restaurada.");
            } catch (Exception e) {
                profile.sendMessage("§cNão foi possível restaurar sua skin ;(");
                e.printStackTrace();
            }

            return;
        }

        if (!profile.isVIP()) {
            profile.sendMessage("§cVocê não pode customizar a sua skin! Adquira o rank "
                    + RankCategory.VENOM.getPrefix() + "§c em §e" + Constant.STORE + "§c.");
            return;
        }

        if (profile.hasCooldown(Constant.SKIN_COOLDOWN_KEY)) {
            profile.sendMessage("§cAguarde " + TimeUtil.formatTime(profile.getCooldown(Constant.SKIN_COOLDOWN_KEY), TimeUtil.TimeFormat.SHORT)
                    + " para trocar de skin novamente.");
            return;
        }

        if (skinName.isEmpty() || !Validator.isNickname(skinName)) {
            profile.sendMessage("§cO nome informado é inválido.");
            return;
        }

        if (profile.hasSkin(skinName)) {
            profile.sendMessage("§cVocê já está usando essa skin!");
            return;
        }

        UUID skinId = UUIDFetcher.request(skinName);

        if (skinId == null) {
            profile.sendMessage("§cNão foi possível encontrar a skin desejada ;(");
            return;
        }

        if (profile.hasSkin(skinId)) {
            profile.sendMessage("§cVocê já está usando essa skin!");
            return;
        }

        profile.sendMessage("§eAguarde...");

        try {
            Skin skin = Core.getSkinManager().fetch(skinId, skinName, SkinCategory.CUSTOM);

            PlayerManager.changePlayerSkin(player, skin);

            profile.setSkin(skin);
            profile.sendMessage("§aA sua skin foi alterada com sucesso.");

            if (!profile.isStaffer())
                profile.setCooldown(Constant.SKIN_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(90));

        } catch (Exception e) {
            profile.sendMessage("§cNão foi possível alterar a sua skin, tente novamente mais tarde ;(");
            e.printStackTrace();
        }
    }
}
