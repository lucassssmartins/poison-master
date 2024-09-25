package br.com.poison.core.backend.database.redis.message.type;

import br.com.poison.core.backend.database.redis.message.RedisMessage;
import com.google.gson.JsonObject;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import lombok.Getter;

@Getter
public class ClanDeleteMessage extends RedisMessage {

    private final Profile profile;

    public ClanDeleteMessage(Profile profile) {
        super(MessageType.BUKKIT, Constant.REDIS_CLAN_DELETE_CHANNEL);

        this.profile = profile;
    }

    @Override
    public void publish() {
        JsonObject json = new JsonObject();

        json.addProperty("id", profile.getId().toString());

        Core.getRedisDatabase().publish(getChannel(), json.toString());
    }
}
