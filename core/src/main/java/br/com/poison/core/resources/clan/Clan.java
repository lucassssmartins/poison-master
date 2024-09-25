package br.com.poison.core.resources.clan;

import br.com.poison.core.backend.database.redis.message.type.ClanDeleteMessage;
import br.com.poison.core.profile.resources.clan.exhibition.ClanExhibition;
import br.com.poison.core.resources.clan.rank.ClanRank;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.clan.associate.ClanAssociate;
import br.com.poison.core.resources.clan.office.ClanOffice;
import br.com.poison.core.resources.clan.option.ClanOptions;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Clan {

    private final UUID id;

    private UUID ownerId;

    private String name, tag;
    private ChatColor color;

    private ClanRank rank;
    private ClanOptions options;

    private final Map<UUID, ClanAssociate> associates;

    private int elo;

    private final long createdAt;

    public Clan(Profile owner, String name, String tag) {
        this.id = UUID.randomUUID();

        this.ownerId = owner.getId();

        this.name = name;
        this.tag = tag.toUpperCase();

        this.color = ChatColor.GOLD;

        this.rank = ClanRank.INITIAL;
        this.options = new ClanOptions();

        this.associates = new HashMap<>();

        owner.setClan(id);
        owner.setClanExhibition(ClanExhibition.NETWORK);

        associates.put(owner.getId(), new ClanAssociate(owner, ClanOffice.OWNER));

        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Salvar os campos da clan.
     *
     * @param fields Campos para salvar.
     */
    protected void save(String... fields) {
        for (String field : fields)
            Core.getClanData().update(this, field);
    }

    public void disband() {
        // Removendo membros do clan
        for (UUID uuid : associates.keySet()) {
            Profile profile = Core.getProfileData().read(uuid, false);

            if (profile == null) continue;

            profile.setClan(Constant.CONSOLE_UUID);
            profile.setClanExhibition(ClanExhibition.UNUSED);

            new ClanDeleteMessage(profile).publish();
        }

        sendMessage("§cO clan foi desfeito!");

        // Apagando o clan
        Core.getClanData().delete(id);
    }

    /* Message Methods */
    public String getColoredName() {
        return color + name;
    }

    public String getPrefix() {
        return color + "[" + tag.toUpperCase() + "]";
    }

    public List<Profile> getActiveMembers() {
        return Core.getProfileManager().documents(profile -> associates.containsKey(profile.getId())).collect(Collectors.toList());
    }

    public void sendMessage(String message) {
        getActiveMembers().stream().filter(profile -> profile.getPreference().isAllowClanInteractions())
                .forEach(profile -> profile.sendMessage(getPrefix() + " §r" + message));
    }

    public void playSound(Sound sound) {
        getActiveMembers().stream().filter(profile -> profile.getPreference().isAllowClanInteractions())
                .forEach(profile -> {
                    Player player = profile.player();

                    if (player != null)
                        player.playSound(player.getLocation(), sound, 2.0f, 2.0f);
                });
    }

    /* Associate Methods */
    public ClanAssociate getAssociate(UUID associateId) {
        return associates.get(associateId);
    }

    public ClanAssociate getAssociate(String name) {
        return associates.values().stream().filter(associate -> associate.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void setOffice(ClanAssociate associate, ClanOffice office) {
        associate.setOffice(office);
        save("associates");
    }

    public boolean isAssociated(UUID associateId) {
        return associates.containsKey(associateId);
    }

    public void join(ClanAssociate associate) {
        if (isAssociated(associate.getId())) return;

        sendMessage("§7O §a" + associate.getName() + "§7 se tornou um associado do clan.");

        associates.put(associate.getId(), associate);
        save("associates");
    }

    public void leave(UUID associateId) {
        if (!isAssociated(associateId)) return;

        ClanAssociate associate = getAssociate(associateId);

        sendMessage("§7O §c" + associate.getName() + "§7 não é mais um associado do clan.");

        associates.remove(associateId);
        save("associates");
    }

    public void kick(UUID senderId, UUID associateId) {
        if (!isAssociated(associateId)) return;

        ClanAssociate sender = getAssociate(senderId), associate = getAssociate(associateId);

        sendMessage("§c" + sender.getName() + "§7 expulsou o jogador §e" + associate.getName() + "§7 do clan.");

        associates.remove(associateId);
        save("associates");
    }

    /* Useful Methods */
    public void setRank(ClanRank rank) {
        this.rank = rank;
        save("rank");
    }

    public void checkRank() {
    }

    public boolean isOwner(UUID uuid) {
        return ownerId.equals(uuid);
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
        save("ownerId");
    }

    public void setName(String name) {
        this.name = name;
        save("name");
    }

    public void setTag(String tag) {
        this.tag = tag;
        save("tag");
    }

    public void setColor(ChatColor color) {
        this.color = color;
        save("color");
    }

    public void addColor(ChatColor color) {
        options.getColors().add(color);
        saveOptions(options);
    }

    public boolean hasColor(ChatColor color) {
        return options.getColors().contains(color);
    }

    public void saveOptions(ClanOptions options) {
        this.options = options;
        save("options");
    }

    public void setElo(int elo) {
        this.elo = elo;
        save("elo");
    }

    public void addElo(int elo) {
        if (elo > 0)
            setElo(getElo() + elo);
    }

    public void removeElo(int elo) {
        if (elo > 0 && getElo() >= elo)
            setElo(getElo() - elo);
    }
}
