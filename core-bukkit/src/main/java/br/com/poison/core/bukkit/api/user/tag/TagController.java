package br.com.poison.core.bukkit.api.user.tag;

import br.com.poison.core.profile.resources.clan.exhibition.ClanExhibition;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class TagController {

    public static List<Tag> getAvailableTags(Profile profile) {
        List<Tag> tags = new ArrayList<>();

        for (Tag tag : Tag.values()) {
            if (tag.isColored()) continue;

            if (tag.isExclusive()) {
                if (!profile.hasRank(RankCategory.OWNER) && !profile.hasOnlyRank(tag.getCategory()))
                    continue;
            }

            if (profile.hasTag(tag))
                tags.add(tag);
        }

        return tags;
    }

    protected static TextComponent create(Profile profile, Tag tag, boolean end) {
        TextComponent message = new TextComponent(tag.getColoredName() + "§r" + (end ? "." : ", "));

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                "§7Prévia:"
                        + "\n§8* " + tag.getPrefix() + profile.getName()
                        + "\n\n" + (profile.isUsingTag(tag) ? "§cEm uso!" : "§aClique para usar!")
        )));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag " + tag.getName().toLowerCase()));

        return message;
    }

    public static void sendTags(Profile profile) {
        List<Tag> tags = getAvailableTags(profile);

        TextComponent message = new TextComponent("§aSuas tags: ");

        boolean end = false;

        int index = 1;
        for (Tag tag : tags) {
            if (index >= tags.size())
                end = true;

            message.addExtra(create(profile, tag, end));
            index++;
        }

        profile.sendMessage(message);
    }

    public static void updateNickname(Player player, String nickname) {
        Profile profile = Core.getProfileManager().read(player.getUniqueId());
        if (profile == null) return;

        profile.setFake(nickname);

        EntityPlayer handle = ((CraftPlayer) player).getHandle();

        for (Player online : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) online).getHandle().playerConnection;

            connection.sendPacket(new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, handle
            ));

            connection.sendPacket(new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, handle
            ));

            if (!online.equals(player)) {
                connection.sendPacket(new PacketPlayOutEntityDestroy(handle.getId()));
                connection.sendPacket(new PacketPlayOutNamedEntitySpawn(handle));
            }
        }
    }

    public static void updateTag(Player player) {
        Profile profile = Core.getProfileManager().read(player.getUniqueId());

        if (profile != null)
            updateTag(profile, profile.getTag());
    }

    public static void updateTag(Profile profile, Tag tag) {
        Player player = profile.player();

        if (tag == null) tag = Tag.PLAYER;

        String order = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        String prefix = tag.getPrefix();
        if (prefix.length() > 16) prefix = prefix.substring(0, 16);

        String suffix = "";

        // Adicionando a Tag do Clan do jogador.
        if (profile.hasClan() && !profile.isClanExhibition(ClanExhibition.UNUSED) && !Core.getServerCategory().equals(ServerCategory.DUELS)) {
            suffix = profile.getClan().getColor() + "[" + profile.getClan().getTag() + "]";
        }

        // Removendo times antigos
        for (Team old : player.getScoreboard().getTeams()) {
            if (old.getName().startsWith("tag:") && old.hasEntry(player.getName()))
                old.unregister();
        }

        Team team = createTeamIfNotExists(order, player, player.getName(), prefix, !suffix.isEmpty() ? " " + suffix : suffix);

        Scoreboard scoreboard = team.getScoreboard();

        player.setDisplayName(team.getPrefix() + player.getName() + team.getSuffix());
        player.setPlayerListName(team.getPrefix() + player.getName() + team.getSuffix());

        player.setScoreboard(scoreboard);

        // Agora, vamos adicionar para todos os outros jogadores.
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            Profile target = Core.getProfileManager().read(onlinePlayer.getUniqueId());
            if (target == null) continue;

            Tag profileTag = target.getTag();
            if (profileTag == null) profileTag = Tag.PLAYER;

            String profileOrder = "tag:" + profileTag.getOrder() + ":" + onlinePlayer.getEntityId();

            String profilePrefix = profileTag.getPrefix();
            if (profilePrefix.length() > 16) profilePrefix = profilePrefix.substring(0, 16);

            String profileSuffix = "";

            // Adicionando a Tag do Clan do jogador.
            if (target.hasClan() && !target.isClanExhibition(ClanExhibition.UNUSED) && !Core.getServerCategory().equals(ServerCategory.DUELS)) {
                profileSuffix = target.getClan().getColor() + "[" + target.getClan().getTag() + "]";
            }

            // Removendo times antigos
            for (Team old : scoreboard.getTeams()) {
                if (old.getName().startsWith("tag:") && old.hasEntry(onlinePlayer.getName()))
                    old.unregister();
            }

            // Aplicando times
            createTeamIfNotExists(profileOrder, player, onlinePlayer.getName(),
                    profilePrefix, !profileSuffix.isEmpty() ? " " + profileSuffix : profileSuffix);

            createTeamIfNotExists(order, onlinePlayer, player.getName(), team.getPrefix(), team.getSuffix());
        }
    }

    private static Team createTeamIfNotExists(String order, Player player, String entry, String prefix, String suffix) {
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard()))
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team team = scoreboard.getTeam(order);
        if (team == null) team = scoreboard.registerNewTeam(order);

        team.setCanSeeFriendlyInvisibles(false);

        team.setPrefix(prefix);
        team.setSuffix(suffix);

        if (!team.hasEntry(entry))
            team.addEntry(entry);

        return team;
    }

    public static void removeTag(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());

        player.getScoreboard().getTeams().stream()
                .filter(team -> team.getName().startsWith("tag:"))
                .forEach(Team::unregister);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player) || onlinePlayer.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard()))
                continue;

            onlinePlayer.getScoreboard().getTeams().forEach(team -> removeEntries(team, player.getName()));
        }
    }

    private static void removeEntries(Team team, String entry) {
        if (team.getName().startsWith("tag:")) {
            team.removeEntry(entry);

            if (team.getEntries().isEmpty())
                team.unregister();
        }
    }
}
