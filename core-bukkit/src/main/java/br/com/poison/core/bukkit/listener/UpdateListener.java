package br.com.poison.core.bukkit.listener;

import br.com.poison.core.bukkit.event.list.profile.field.ProfileFieldEvent;
import br.com.poison.core.bukkit.event.list.profile.rank.ProfileRankEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.AsyncUpdateEvent;
import br.com.poison.core.bukkit.manager.PermissionManager;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.item.book.BookItem;
import br.com.poison.core.bukkit.api.mechanics.sidebar.Sidebar;
import br.com.poison.core.bukkit.api.user.tag.TagController;
import br.com.poison.core.bukkit.manager.PlayerManager;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.Rank;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.profile.resources.relation.Relation;
import br.com.poison.core.util.extra.TimeUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class UpdateListener implements Listener {

    @EventHandler
    public void onUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND))
            Core.getServerManager().update(Bukkit.getOnlinePlayers().size());
    }

    @EventHandler
    public void onField(ProfileFieldEvent event) {
        Profile profile = event.getProfile();

        String fieldName = event.getField().getName().toLowerCase();

        switch (event.getField().getName().toLowerCase()) {
            case "rank": {
                new ProfileRankEvent(profile, profile.getRank()).call();
                break;
            }

            case "relation": {
                Relation relation = profile.getRelation();

                // Clan

                break;
            }
        }


        Core.getProfileManager().save(profile);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRank(ProfileRankEvent event) {
        Profile profile = event.getProfile();
        Rank rank = event.getRank();

        Player player = profile.player();

        profile.setTag(Tag.fetch(rank.getCategory()));

        if (rank.isPayment()) {
            TextComponent message = new TextComponent("Sua compra foi realizada!\n\n");

            message.addExtra("Você adquiriu os seguintes produtos: " + rank.getColor() + "Rank " + rank.getName() + "\n\n");
            message.addExtra("Se você estiver com algum problema, contate-nos em:\n");

            TextComponent discordComponent = new TextComponent("§9§lDISCORD");

            discordComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para acessar nosso Discord!")));
            discordComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www." + Constant.DISCORD));

            message.addExtra(discordComponent);

            PlayerManager.openBook(new BookItem().pageComponents(message), player);
        }

        PermissionManager.loadPermissions(player);
        TagController.updateTag(player);

        profile.sendMessage(rank.isPayment() ?
                "§aA sua compra foi realizada! O rank " + rank.getPrefix() + "§a foi aplicado em sua conta por "
                        + (rank.isPermanent() ? "tempo ilimitado" : TimeUtil.formatTime(rank.getExpiresAt(), TimeUtil.TimeFormat.SHORT)) + "."
                : "§aO rank " + rank.getPrefix() + "§a foi aplicado em sua conta.");

        profile.sendSound(Sound.NOTE_PLING);
    }
}
