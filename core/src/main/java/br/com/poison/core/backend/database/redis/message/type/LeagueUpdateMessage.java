package br.com.poison.core.backend.database.redis.message.type;

import br.com.poison.core.backend.database.redis.message.RedisMessage;
import com.google.gson.JsonObject;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.resources.league.League;
import br.com.poison.core.resources.member.Member;
import lombok.Getter;

@Getter
public class LeagueUpdateMessage extends RedisMessage {

    private final Member member;
    private final League league;

    public LeagueUpdateMessage(Member member, League league) {
        super(MessageType.BUKKIT, Constant.REDIS_LEAGUE_UPDATE_CHANNEL);

        this.member = member;
        this.league = league;
    }

    @Override
    public void publish() {
        JsonObject json = new JsonObject();

        json.addProperty("member", Core.GSON.toJson(member));
        json.addProperty("league", Core.GSON.toJson(league));

        Core.getRedisDatabase().publish(getChannel(), json.toString());
    }
}
