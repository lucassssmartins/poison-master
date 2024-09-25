package br.com.poison.core.bukkit.command.list.special;

import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanEnterEvent;
import br.com.poison.core.bukkit.event.list.profile.clan.list.ProfileClanLeaveEvent;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.clan.exhibition.ClanExhibition;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.backend.data.ClanData;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.manager.ClanManager;
import br.com.poison.core.resources.clan.Clan;
import br.com.poison.core.resources.clan.associate.ClanAssociate;
import br.com.poison.core.resources.clan.invitation.ClanInvitation;
import br.com.poison.core.resources.clan.office.ClanOffice;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ClanCommand implements CommandInheritor {

    protected final ClanData data = Core.getClanData();
    protected final ClanManager manager = Core.getClanManager();

    protected final Map<UUID, Map<UUID, ClanInvitation>> invitationCache = new ConcurrentHashMap<>();

    protected final String PART_OF_CLAN = "§cVocê já faz parte de um clan.",
            NOT_PART_OF_CLAN = "§cVocê não faz parte de um clan.",
            WITHOUT_PERMISSION = "§cVocê precisa ser um %s§c ou superior para fazer isso.",
            CLAN_NOT_EXISTS = "§cNenhum clan foi encontrado!";

    protected final Pattern NAME_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{3,16}$"),
            TAG_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{4,6}$");

    protected final int MIN_CASH = 5;

    @Command(name = "clan")
    public void clan(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        profile.sendMessage("§cUso do /" + context.getLabel() + ":",
                "§c* /" + context.getLabel() + " criar <nome> <tag>.",
                "§c* /" + context.getLabel() + " ver <nome/tag>.",
                "§c* /" + context.getLabel() + " convidar <jogador>.",
                "§c* /" + context.getLabel() + " entrar <nome/tag>.",
                "§c* /" + context.getLabel() + " recusar <nome/tag>.",
                "§c* /" + context.getLabel() + " sair.",
                "§c* /" + context.getLabel() + " apagar.",
                "§c* /" + context.getLabel() + " expulsar <jogador>.",
                "§c* /" + context.getLabel() + " promover <jogador>.",
                "§c* /" + context.getLabel() + " rebaixar <jogador>.");
    }

    @Command(name = "clan.criar", runAsync = true)
    public void clanCreate(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (profile.hasClan()) {
            profile.sendMessage(PART_OF_CLAN);
            return;
        }

        String[] args = context.getArgs();

        if (args.length <= 1) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <nome> <tag>.");
            return;
        }

        if (profile.getCash() < MIN_CASH && !profile.hasRank(RankCategory.POISON)) {
            profile.sendMessage("§cVocê precisa de mais §2" + (MIN_CASH - profile.getCash()) +
                    " cash§c para criar um clan! Adquira o rank " + RankCategory.POISON.getPrefix()
                    + "§c em §e" + Constant.STORE +
                    "§c para criar clans de forma gratuita.");
            return;
        }

        String name = args[0], tag = args[1];

        if (!NAME_VALIDATOR.matcher(name).matches()) {
            profile.sendMessage("§cO nome do seu clan é inválido!");
            return;
        }

        if (data.readByName(name) != null) {
            profile.sendMessage("§cUm clan com esse nome já existe!");
            return;
        }

        if (!TAG_VALIDATOR.matcher(tag).matches()) {
            profile.sendMessage("§cA tag do seu clan é inválida!");
            return;
        }

        if (data.readByTag(tag) != null) {
            profile.sendMessage("§cUm clan com essa tag já existe!");
            return;
        }

        Clan clan = new Clan(profile, name, tag);

        data.save(clan);

        profile.sendMessage("§aO seu clan foi criado com sucesso.");

        if (!profile.isVIP())
            profile.removeCash(MIN_CASH);

        new ProfileClanEnterEvent(profile, clan).call();

        log(context.getSender(), profile.getName() + " criou o clan " + clan.getName());
    }

    @Command(name = "clan.ver")
    public void clanSee(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Profile profile = context.getProfile();
        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <nome/tag>.");
            return;
        }

        if (!profile.hasRank(RankCategory.POISON)) {
            profile.sendMessage("§cVocê não pode ver outros §6clans§c ;(",
                    "§cAdquira o §6rank " + RankCategory.POISON.getPrefix() + "§c para ver outros clans.");
            return;
        }

        Clan clan = data.readByName(args[0]);

        if (clan == null) {
            clan = data.readByTag(args[0]);

            if (clan == null) {
                profile.sendMessage(CLAN_NOT_EXISTS);
                return;
            }
        }

        profile.sendMessage("§a[CLAN]: §8" + clan.getName(),
                "§fNome: §7" + clan.getName(),
                 "§fTag: §7" + clan.getTag());
    }

    @Command(name = "clan.apagar", runAsync = true)
    public void clanShutdown(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (!profile.hasClan()) {
            profile.sendMessage(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = profile.getClan();

        if (!clan.isOwner(profile.getId())) {
            profile.sendMessage("§cVocê não possui a posse do clan.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " confirmar.");
            return;
        }

        if (args[0].equalsIgnoreCase("confirmar")) {
            clan.disband();
        }
    }

    @Command(name = "clan.sair")
    public void clanLeave(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (!profile.hasClan()) {
            profile.sendMessage(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = profile.getClan();

        if (clan.isOwner(profile.getId())) {
            profile.sendMessage("§cVocê não pode sair do seu clan!");
            return;
        }

        profile.setClan(null);
        profile.setClanExhibition(ClanExhibition.UNUSED);

        clan.leave(profile.getId());

        profile.sendMessage("§cVocê saiu do seu clan atual.");

        new ProfileClanLeaveEvent(profile).call();
    }

    @Command(name = "clan.convidar")
    public void clanInvitation(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (!profile.hasClan()) {
            profile.sendMessage(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = profile.getClan();

        if (profile.isNotOffice(ClanOffice.RECRUITER)) {
            profile.sendMessage(String.format(WITHOUT_PERMISSION, ClanOffice.RECRUITER.getPrefix()));
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <jogador>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null || target.player() == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (target.hasClan()) {
            profile.sendMessage("§cEste jogador já faz parte de um clan!");
            return;
        }

        Map<UUID, ClanInvitation> map = invitationCache.computeIfAbsent(profile.getClan().getId(), v -> new HashMap<>());

        if (map.containsKey(target.getId()) && !map.get(target.getId()).hasExpired()) {
            profile.sendMessage("§cEste jogador já foi convidado para o clan!");
            return;
        }

        if (map.values().stream().filter(invitation -> !invitation.hasExpired()).count() >= clan.getOptions().getMaxParticipants()) {
            profile.sendMessage("§cExistem muitos convites simultâneos!");
            return;
        }

        map.put(target.getId(), new ClanInvitation(clan.getId(), profile, target));

        clan.sendMessage("§7" + profile.getName() + " convidou " + target.getName() + " para o clan.");

        /* Invitation Message */
        TextComponent message = new TextComponent("§eVocê recebeu um convite para entrar no clan §6" + clan.getTag()
                + "§e, deseja ");

        TextComponent join = new TextComponent("§a§lENTRAR");
        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan entrar " + clan.getName()));
        join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para entrar no clan!")));

        TextComponent refuse = new TextComponent("§c§lRECUSAR");
        refuse.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan recusar " + clan.getName()));
        refuse.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Clique para recusar o convite!")));

        message.addExtra(join);
        message.addExtra("§e ou ");
        message.addExtra(refuse);
        message.addExtra("§e o convite?");

        target.sendMessage(message);

        profile.sendMessage("§aO seu convite foi enviado para " + target.getName() + ".");
    }

    @Command(name = "clan.entrar")
    public void clanJoin(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (profile.hasClan()) {
            profile.sendMessage(PART_OF_CLAN);
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <nome/tag>.");
            return;
        }

        Clan clan = data.readByName(args[0]);

        if (clan == null) {
            clan = data.readByTag(args[0]);

            if (clan == null) {
                profile.sendMessage(CLAN_NOT_EXISTS);
                return;
            }
        }

        if (!invitationCache.containsKey(clan.getId())) {
            profile.sendMessage("§cEste clan não possui convites pendentes.");
            return;
        }

        ClanInvitation invitation = invitationCache.get(clan.getId()).get(profile.getId());

        if (invitation == null) {
            profile.sendMessage("§cNenhum convite foi encontrado para você!");
            return;
        }

        invitationCache.get(clan.getId()).remove(profile.getId());

        profile.setClan(clan.getId());
        profile.setClanExhibition(ClanExhibition.NETWORK);

        clan.join(new ClanAssociate(profile, ClanOffice.ASSOCIATE));

        new ProfileClanEnterEvent(profile, clan).call();

        profile.sendMessage("§aVocê aceitou o convite para o clan " + clan.getName() + ".");
    }

    @Command(name = "clan.recusar")
    public void clanRefuse(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (profile.hasClan()) {
            profile.sendMessage(PART_OF_CLAN);
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <nome/tag>.");
            return;
        }

        Clan clan = data.readByName(args[0]);

        if (clan == null) {
            clan = data.readByTag(args[0]);

            if (clan == null) {
                profile.sendMessage(CLAN_NOT_EXISTS);
                return;
            }
        }

        if (!invitationCache.containsKey(clan.getId())) {
            profile.sendMessage("§cEste clan não possui convites pendentes.");
            return;
        }

        ClanInvitation invitation = invitationCache.get(clan.getId()).get(profile.getId());

        if (invitation == null) {
            profile.sendMessage("§cNenhum convite foi encontrado para você!");
            return;
        }

        invitationCache.get(clan.getId()).remove(profile.getId());

        clan.sendMessage("§c" + profile.getName() + " recusou o convite para o clan.");

        profile.sendMessage("§cVocê recusou o convite para o clan " + clan.getName() + ".");
    }

    @Command(name = "clan.expulsar")
    public void clanKick(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (!profile.hasClan()) {
            profile.sendMessage(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = profile.getClan();

        if (profile.isNotOffice(ClanOffice.MOD)) {
            profile.sendMessage(String.format(WITHOUT_PERMISSION, ClanOffice.MOD.getPrefix()));
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <jogador>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (!target.hasClan()) {
            profile.sendMessage("§cEste jogador não faz parte de um clan!");
            return;
        }

        if (!target.getClan().getId().equals(clan.getId())) {
            profile.sendMessage("§cEste jogador não faz parte do seu clan.");
            return;
        }

        ClanAssociate associate = target.getAssociate();

        if (profile.getAssociate().getOffice().ordinal() <= associate.getOffice().ordinal()) {
            profile.sendMessage("§cEste jogador possui uma função igual ou superior a sua.");
            return;
        }

        target.setClan(null);
        target.setClanExhibition(ClanExhibition.UNUSED);

        clan.kick(profile.getId(), target.getId());

        new ProfileClanLeaveEvent(target).call();
    }

    @Command(name = "clan.promover")
    public void clanPromote(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (!profile.hasClan()) {
            profile.sendMessage(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = profile.getClan();

        if (profile.isNotOffice(ClanOffice.ADMIN)) {
            profile.sendMessage(String.format(WITHOUT_PERMISSION, ClanOffice.ADMIN.getPrefix()));
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <jogador>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (!target.hasClan()) {
            profile.sendMessage("§cEste jogador não faz parte de um clan!");
            return;
        }

        if (!target.getClan().getId().equals(clan.getId())) {
            profile.sendMessage("§cEste jogador não faz parte do seu clan.");
            return;
        }

        ClanAssociate associate = target.getAssociate();

        ClanOffice current = associate.getOffice();

        if (current.equals(ClanOffice.OWNER)) {
            profile.sendMessage("§cNão é possível promover o " + ClanOffice.OWNER.getPrefix() + "§c da clan.");
            return;
        }

        ClanOffice next = ClanOffice.values()[current.ordinal() + 1];

        if (next.ordinal() >= profile.getAssociate().getOffice().ordinal()) {
            profile.sendMessage("§cA função " + next.getPrefix() + "§c é maior ou igual a sua.");
            return;
        }

        clan.setOffice(associate, next);

        clan.sendMessage("§7O membro §e" + target.getName() + "§7 foi promovido a "
                + next.getPrefix() + "§7 por §e" + profile.getName() + "§7.");
    }

    @Command(name = "clan.rebaixar")
    public void clanDemote(BukkitCommandContext context) {
        Profile profile = context.getProfile();

        if (!profile.hasClan()) {
            profile.sendMessage(NOT_PART_OF_CLAN);
            return;
        }

        Clan clan = profile.getClan();

        if (profile.isNotOffice(ClanOffice.ADMIN)) {
            profile.sendMessage(String.format(WITHOUT_PERMISSION, ClanOffice.ADMIN.getPrefix()));
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            profile.sendMessage("§cUso: /" + context.getCommandLabel() + " <jogador>.");
            return;
        }

        Profile target = context.getProfile(args[0]);

        if (target == null) {
            profile.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (!target.hasClan()) {
            profile.sendMessage("§cEste jogador não faz parte de um clan!");
            return;
        }

        if (!target.getClan().getId().equals(clan.getId())) {
            profile.sendMessage("§cEste jogador não faz parte do seu clan.");
            return;
        }

        ClanAssociate associate = target.getAssociate();

        ClanOffice current = associate.getOffice();

        if (current.equals(ClanOffice.ASSOCIATE)) {
            profile.sendMessage("§cNão é possível rebaixar um associado.");
            return;
        }

        if (current.equals(ClanOffice.OWNER) && !clan.isOwner(profile.getId())) {
            profile.sendMessage("§cNão é possível rebaixar o " + ClanOffice.OWNER.getPrefix() + "§c da clan.");
            return;
        }

        if (current.ordinal() >= profile.getAssociate().getOffice().ordinal()) {
            profile.sendMessage("§cO membro " + target.getName() + " possui uma função maior ou igual a sua.");
            return;
        }

        ClanOffice below = ClanOffice.values()[current.ordinal() - 1];

        clan.setOffice(associate, below);

        clan.sendMessage("§7O membro §e" + target.getName() + "§7 foi rebaixado a "
                + below.getPrefix() + "§7 por §e" + profile.getName() + "§7.");
    }
}
