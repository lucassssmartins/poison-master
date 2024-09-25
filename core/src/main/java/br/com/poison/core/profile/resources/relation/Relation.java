package br.com.poison.core.profile.resources.relation;

import br.com.poison.core.profile.resources.auth.Auth;
import br.com.poison.core.profile.resources.clan.ClanInfo;
import br.com.poison.core.profile.resources.medal.Medal;
import br.com.poison.core.profile.resources.medal.info.MedalInfo;
import br.com.poison.core.profile.resources.rank.tag.info.TagInfo;
import br.com.poison.core.profile.resources.relation.fake.Fake;
import br.com.poison.core.profile.resources.route.Route;
import br.com.poison.core.profile.resources.skin.SkinInfo;
import br.com.poison.core.Constant;
import br.com.poison.core.profile.resources.permission.Permission;
import br.com.poison.core.profile.resources.punishment.PunishmentInfo;
import br.com.poison.core.profile.resources.relation.type.ProfileType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@ToString
public class Relation {

    private Auth auth = new Auth();
    private Route route = new Route();

    private SkinInfo skin = new SkinInfo();
    private ClanInfo clan = new ClanInfo();
    private TagInfo tag = new TagInfo();

    private Fake fake = new Fake();

    private PunishmentInfo punishment = new PunishmentInfo();


    private final Set<Permission> permissions = new HashSet<>();
    private final Set<MedalInfo> medals = new HashSet<>();

    private ProfileType type = ProfileType.PIRATE;
    private Medal medal = Medal.VOID;

    private String ipAddress = Constant.DEFAULT_ADDRESS;

    private UUID lastMessage = Constant.CONSOLE_UUID;

    private boolean online = true;

    private final long firstEntry = System.currentTimeMillis();
    private long lastEntry = System.currentTimeMillis();
}
