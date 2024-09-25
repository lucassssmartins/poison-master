package br.com.poison.core.profile;

import br.com.poison.core.Constant;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.arcade.route.GameRouteContext;
import br.com.poison.core.profile.field.FieldCategory;
import br.com.poison.core.profile.resources.auth.Auth;
import br.com.poison.core.profile.resources.clan.exhibition.ClanExhibition;
import br.com.poison.core.profile.resources.medal.Medal;
import br.com.poison.core.profile.resources.medal.info.MedalInfo;
import br.com.poison.core.profile.resources.permission.Permission;
import br.com.poison.core.profile.resources.preference.Preference;
import br.com.poison.core.profile.resources.rank.Rank;
import br.com.poison.core.profile.resources.rank.assignment.Assignment;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.profile.resources.rank.tag.Tag;
import br.com.poison.core.profile.resources.relation.Relation;
import br.com.poison.core.profile.resources.relation.type.ProfileType;
import br.com.poison.core.profile.resources.route.Route;
import br.com.poison.core.profile.resources.skin.SkinInfo;
import br.com.poison.core.resources.clan.Clan;
import br.com.poison.core.resources.clan.associate.ClanAssociate;
import br.com.poison.core.resources.clan.office.ClanOffice;
import br.com.poison.core.resources.skin.Skin;
import br.com.poison.core.resources.skin.category.SkinCategory;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.resources.punishment.Punishment;
import br.com.poison.core.resources.punishment.category.PunishmentCategory;
import br.com.poison.core.Core;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.github.paperspigot.Title;
import redis.clients.jedis.Jedis;

import java.util.*;

@Getter
@ToString
public class Profile {

    private final UUID id;
    private String name;

    private Rank rank;

    private Relation relation;
    private Preference preference;

    private final Map<String, Long> cooldown;

    private int cash;

    public Profile(UUID id, String name) {
        this.id = id;
        this.name = name;

        this.relation = new Relation();

        if (Core.isDeveloper(id) || Constant.CEO_IDS.contains(id)) {
            this.rank = new Rank(RankCategory.OWNER,
                    Assignment.AUTO, Constant.CONSOLE_UUID, -1L);

            relation.getPunishment().setByPass(true);
        } else
            this.rank = new Rank(RankCategory.PLAYER, Assignment.AUTO, Constant.CONSOLE_UUID, -1L);

        this.preference = new Preference();

        this.cooldown = new HashMap<>();

        this.cash = 0;
    }

    public void load() {
        // Carregando o clan do jogador
        if (hasClan()) {
            Clan clan = getClan();

            if (clan == null)
                clan = Core.getClanData().read(relation.getClan().getClanId());

            if (clan != null) {
                Core.getClanManager().save(clan);

                Core.getLogger().info("O clan " + clan.getName() + " foi adicionado no cache.");
            }
        }
    }

    public void unload() {
        if (hasClan()) {
            Clan clan = getClan();

            if (clan != null && clan.getActiveMembers().isEmpty()) {
                Core.getClanManager().remove(clan.getId());

                Core.getLogger().info("O clan " + clan.getName() + " foi removido do cache.");
            }
        }
    }

    /**
     * Salvar os campos do perfíl.
     *
     * @param categories Campos para salvar.
     */
    protected void save(FieldCategory... categories) {
        for (FieldCategory category : categories) {
            Core.getProfileData().update(this, category.name().toLowerCase());
        }
    }

    @Override
    public boolean equals(Object profileObject) {
        if (this == profileObject) return true;
        if (profileObject == null || getClass() != profileObject.getClass()) return false;

        Profile profile = (Profile) profileObject;

        return profile.getId().equals(id);
    }

    public Player player() {
        return Core.getMultiService().getPlayer(id, Player.class);
    }

    public ProxiedPlayer proxiedPlayer() {
        return Core.getMultiService().getPlayer(id, ProxiedPlayer.class);
    }

    /**
     * Enviar o jogador para um servidor.
     *
     * @param category Servidor de envio.
     */
    public void redirect(ServerCategory category) {
        if (category == null) {
            sendMessage("§cO servidor solicitado não foi encontrado.");
            return;
        }

        if (inServer(category) && Core.getServerCategory().equals(category)) {
            sendMessage("§cVocê já está conectado no " + category.getName() + "!");
            return;
        }

        JsonObject message = new JsonObject();

        message.addProperty("id", getId().toString());
        message.addProperty("category", category.name());

        Core.getRedisDatabase().publish(Constant.REDIS_SERVER_SENDER_CHANNEL, message.toString());
    }

    public void redirect(GameRouteContext route) {
        if (!route.isValid()) {
            sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        GameRouteContext game = getGame();

        if (game.isValid() && game.getArcade().equals(route.getArcade()) && inServer(game.getServer())) {
            sendMessage("§cVocê já está conectado na sala.");
            return;
        }

        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            jedis.setex(Constant.ROUTE_KEY + getId(), 5, Core.GSON.toJson(route));
        }

        redirect(route.getServer());
    }

    public void redirect(ArcadeCategory category, SlotCategory slot) {
        if (category == null) {
            sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        GameRouteContext game = getGame();

        if (game.isValid() && game.getArcade().equals(category) && getServer().equals(game.getServer())) {
            sendMessage("§cVocê já está conectado na sala.");
            return;
        }

        GameRouteContext route = GameRouteContext.builder()
                .arcade(category)
                .slot(slot)
                .build();

        try (Jedis jedis = Core.getRedisDatabase().getPool().getResource()) {
            jedis.setex(Constant.ROUTE_KEY + getId(), 5, Core.GSON.toJson(route));
        }

        redirect(category.getServer());
    }

    /* Message Sender */
    public void sendMessage(String message) {
        Core.getMultiService().sendMessage(id, message);
    }

    public void sendMessage(String... message) {
        Core.getMultiService().sendMessage(id, message);
    }

    public void sendMessage(BaseComponent message) {
        Core.getMultiService().sendMessage(id, message);
    }

    public void sendMessage(BaseComponent... message) {
        Core.getMultiService().sendMessage(id, message);
    }

    public void sendTitle(String title, String subTitle) {
        player().sendTitle(new Title(title, subTitle, 0, 20, 60));
    }

    public void sendTitle(String title) {
        sendTitle(title, "");
    }

    public void sendSound(Sound sound) {
        Player player = player();

        if (player != null)
            player.playSound(sound);
    }

    /* Profile Methods */
    public void changeName(String name) {
        this.name = name;
        save(FieldCategory.NAME);
    }

    public void checkAll() {
        checkRank();
        checkMedals();
        checkPermissions();
    }

    /* Auth Methods */
    public Auth getAuth() {
        return relation.getAuth();
    }

    public void setPassword(String password) {
        getAuth().update(password);
        saveRelation(relation);
    }

    public String getProfileType() {
        return isAuthentic() ? "Premium" : "Cracked";
    }

    public boolean isAuthentic() {
        return relation.getType().equals(ProfileType.AUTHENTIC);
    }

    /* Rank Methods */
    public void setRank(Rank rank) {
        this.rank = rank;
        save(FieldCategory.RANK);
    }

    protected void checkRank() {
        boolean save = false;

        if (!rank.isPermanent() && rank.hasExpired()) {
            sendMessage("§cO seu rank " + rank.getPrefix() + "§c expirou! Adquira-o novamente em: §e" + Constant.STORE);

            rank = new Rank(RankCategory.PLAYER, Assignment.AUTO, Constant.CONSOLE_UUID, -1L);
            save = true;
        }

        if (save)
            save(FieldCategory.RANK);
    }

    public boolean hasRank(RankCategory category) {
        return rank.getCategory().ordinal() >= category.ordinal();
    }

    public boolean hasOnlyRank(RankCategory category) {
        return rank.getCategory().equals(category);
    }

    public boolean isVIP() {
        return hasRank(RankCategory.VENOM);
    }

    public boolean isStaffer() {
        return hasRank(RankCategory.BUILDER);
    }

    /* Relation Methods */
    protected void saveRelation(Relation relation) {
        this.relation = relation;
        save(FieldCategory.RELATION);
    }

    public void setProfileType(ProfileType type) {
        relation.setType(type);
        saveRelation(relation);
    }

    // Route Methods
    public Route getRoute() {
        return relation.getRoute();
    }

    public ServerCategory getServer() {
        return getRoute().getServer();
    }

    public ServerCategory getLastServer() {
        return getRoute().getLastServer();
    }

    public boolean inGame() {
        return getGame() != null && getGame().isValid();
    }

    public GameRouteContext getGame() {
        return getRoute().getGame();
    }

    public void setGame(GameRouteContext game) {
        if (!game.isValid()) return;

        Relation relation = this.relation;

        Route route = relation.getRoute();

        route.setLastServer(route.getServer());
        route.setServer(game.getServer());

        route.setGame(game);
        route.setEntryAt(System.currentTimeMillis());

        Core.getLogger().info("[Route] " + name + " está jogando " + game.getArcade().getName() + " na sala " + game.getArenaId() + ".");

        saveRelation(relation);
    }

    public void setServer(ServerCategory category, GameRouteContext context) {
        getRoute().update(category, context);
        saveRelation(relation);
    }

    public void setServer(ServerCategory category) {
        setServer(category, GameRouteContext.builder().build());
    }

    public boolean inServer(ServerCategory category) {
        return getServer().equals(category);
    }

    // Clan
    public void setClan(UUID clanId) {
        relation.getClan().update(clanId);
        saveRelation(relation);
    }

    public void setClanExhibition(ClanExhibition exhibition) {
        relation.getClan().setExhibition(exhibition);
        saveRelation(relation);
    }

    public boolean isClanExhibition(ClanExhibition exhibition) {
        return getClanExhibition().equals(exhibition);
    }

    public ClanExhibition getClanExhibition() {
        return relation.getClan().getExhibition();
    }

    public Clan getClan() {
        return Core.getClanData().read(relation.getClan().getClanId());
    }

    public boolean hasClan() {
        if (getClan() == null && !relation.getClan().getClanId().equals(Constant.CONSOLE_UUID)) {
            setClan(Constant.CONSOLE_UUID);
            return false;
        }

        return relation.getClan().getClanId() != null && !relation.getClan().getClanId().equals(Constant.CONSOLE_UUID);
    }

    public ClanAssociate getAssociate() {
        if (!hasClan()) return null;

        return getClan().getAssociate(id);
    }

    public boolean isNotOffice(ClanOffice office) {
        if (getAssociate() == null) return true;

        return getAssociate().getOffice().ordinal() < office.ordinal();
    }

    // Fake
    public void setFake(String nickname) {
        relation.getFake().update(nickname);
        saveRelation(relation);
    }

    public void resetFake() {
        setFake("");
    }

    public boolean isUsingFake() {
        return !relation.getFake().getNickname().isEmpty();
    }

    public boolean isUsingFake(String nickname) {
        return relation.getFake().getNickname().equalsIgnoreCase(nickname);
    }

    public String getNickname() {
        return relation.getFake().getNickname();
    }

    public String getLastNickname() {
        return relation.getFake().getLastNickname();
    }

    // Skin
    public SkinInfo getSkinInfo() {
        return relation.getSkin();
    }

    public void setSkin(Skin skin) {
        relation.getSkin().update(skin);
        saveRelation(relation);
    }

    public boolean hasSkin() {
        return getSkin() != null;
    }

    public boolean hasSkin(String skinName) {
        return hasSkin() && getSkin().getName().equalsIgnoreCase(skinName);
    }

    public boolean hasSkin(UUID skinId) {
        return hasSkin() && getSkin().getId().equals(skinId);
    }

    public Skin getSkin() {
        return relation.getSkin().getSkin();
    }

    public Skin getLastSkin() {
        return relation.getSkin().getLastSkin();
    }

    public boolean isUsingSkin(Skin skin) {
        return getSkin().equals(skin);
    }

    public boolean isUsingSkin(String name) {
        return getSkin().getName().equalsIgnoreCase(name);
    }

    public void resetSkin() {
        Skin skin = Core.getSkinManager().fetch(id, name, SkinCategory.PROFILE);

        if (skin == null) {
            sendMessage("§cNão foi possível restaurar a sua skin!");
            return;
        }

        setSkin(skin);
    }

    // Ip Address
    public String getIpAddress() {
        return relation.getIpAddress();
    }

    public void setIpAddress(String ipAddress) {
        relation.setIpAddress(ipAddress);
        saveRelation(relation);
    }

    public boolean isOnline() {
        return relation.isOnline();
    }

    public void setOnline(boolean online) {
        relation.setOnline(online);
        saveRelation(relation);
    }

    // Last Message
    public void setLastMessage(UUID lastMessage) {
        relation.setLastMessage(lastMessage);
        saveRelation(relation);
    }

    public boolean hasLastMessage() {
        return !relation.getLastMessage().equals(Constant.CONSOLE_UUID);
    }

    // Permission
    public void setPermission(Permission permission) {
        if (hasPermission(permission.getKey())) return;

        relation.getPermissions().add(permission);
        saveRelation(relation);
    }

    public void setPermission(String key, UUID author) {
        setPermission(new Permission(key.toLowerCase(), author, -1L));
    }

    public void removePermission(String key) {
        if (!hasPermission(key)) return;

        relation.getPermissions().removeIf(permission -> permission.getKey().equalsIgnoreCase(key));
        saveRelation(relation);
    }

    public boolean hasPermission(String key) {
        return relation.getPermissions().stream().anyMatch(permission -> permission.getKey().equalsIgnoreCase(key));
    }

    protected void checkPermissions() {
        boolean save = false;

        Iterator<Permission> iterator = relation.getPermissions().iterator();

        while (iterator.hasNext()) {
            Permission permission = iterator.next();

            if (permission == null) {
                iterator.remove();
                continue;
            }

            if (!permission.isPermanent() && permission.hasExpired()) {
                iterator.remove();
                save = true;
            }
        }

        if (save)
            saveRelation(relation);
    }

    // Medal
    public void setMedal(Medal medal) {
        relation.setMedal(medal);
        saveRelation(relation);
    }

    public Medal getMedal() {
        return relation.getMedal();
    }

    public boolean isUsingMedal(Medal medal) {
        return getMedal().equals(medal);
    }

    public boolean hasMedal(Medal medal) {
        return relation.getMedals().stream().anyMatch(info -> info.getMedal().equals(medal));
    }

    public void addMedals(MedalInfo... medals) {
        relation.getMedals().addAll(Arrays.asList(medals));
        saveRelation(relation);
    }

    public void removeMedals(Medal... medals) {
        for (Medal medal : medals)
            relation.getMedals().removeIf(info -> info.getMedal().equals(medal));

        saveRelation(relation);
    }

    protected void checkMedals() {
        boolean save = false;

        Iterator<MedalInfo> iterator = relation.getMedals().iterator();

        while (iterator.hasNext()) {
            MedalInfo info = iterator.next();

            if (info == null) {
                iterator.remove();
                continue;
            }

            if (!info.isPermanent() && info.hasExpired()) {
                iterator.remove();
                save = true;
            }
        }

        if (save)
            saveRelation(relation);
    }

    // Tag
    public void setTag(Tag tag) {
        relation.getTag().update(tag);
        saveRelation(relation);
    }

    public Tag getDefaultTag() {
        return Tag.fetch(rank.getCategory());
    }

    public Tag getTag() {
        return relation.getTag().getTag();
    }

    public Tag getLastTag() {
        return relation.getTag().getLastTag();
    }

    public boolean hasTag(Tag tag) {
        if (hasRank(RankCategory.OWNER)) return true;
        if (tag.equals(Tag.DEV) && !hasRank(RankCategory.DEV)) return false;

        return tag.isReward() ? hasPermission(tag.getPermission()) : hasRank(tag.getCategory());
    }

    public boolean isUsingTag(Tag tag) {
        return getTag().equals(tag);
    }

    // Auth
    public void register(String password) {
        relation.getAuth().update(password);
        saveRelation(relation);
    }

    public void changePassword(String password) {
        relation.getAuth().setPassword(password);
        saveRelation(relation);
    }

    public String getPassword() {
        return relation.getAuth().getPassword();
    }

    public boolean isRegistered() {
        return relation.getAuth().isRegistered();
    }

    // Punishment
    public void setBuyForgiveness(boolean buyForgiveness) {
        relation.getPunishment().setBuyForgiveness(buyForgiveness);
        saveRelation(relation);
    }

    public List<Punishment> getPunishments(PunishmentCategory category) {
        return Core.getPunishmentData().listAll(id, category);
    }

    // Login
    public void updateLastEntry() {
        relation.setLastEntry(System.currentTimeMillis());
        saveRelation(relation);
    }

    /* Preference System */
    public void savePreference(Preference preference) {
        this.preference = preference;
        save(FieldCategory.PREFERENCE);
    }

    public void resetPresence() {
        savePreference(new Preference());
    }

    /* Cooldown System */
    public long getCooldown(String key) {
        return cooldown.get(key.toLowerCase());
    }

    public void setCooldown(String key, long time) {
        cooldown.put(key.toLowerCase(), (System.currentTimeMillis() + time));
        save(FieldCategory.COOLDOWN);
    }

    public void removeCooldown(String key) {
        cooldown.remove(key.toLowerCase());
        save(FieldCategory.COOLDOWN);
    }

    public boolean hasCooldown(String key) {
        key = key.toLowerCase();

        if (cooldown.containsKey(key)) {
            if (getCooldown(key) >= System.currentTimeMillis())
                return true;

            removeCooldown(key);
            return false;
        }

        return false;
    }

    /* Integer System */
    public void setCash(int cash) {
        this.cash = cash;
        save(FieldCategory.CASH);
    }

    public void addCash(int cash) {
        if (cash > 0)
            setCash(getCash() + cash);
    }

    public void removeCash(int cash) {
        if (cash > 0 && getCash() >= cash)
            setCash(getCash() - cash);
    }
}
