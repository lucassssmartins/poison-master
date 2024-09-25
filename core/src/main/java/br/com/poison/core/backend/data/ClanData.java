package br.com.poison.core.backend.data;

import br.com.poison.core.backend.data.async.DataAsync;
import br.com.poison.core.util.extra.Validator;
import br.com.poison.core.util.json.JsonUtil;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.backend.database.mongodb.MongoDatabase;
import br.com.poison.core.backend.database.redis.RedisDatabase;
import br.com.poison.core.manager.ClanManager;
import br.com.poison.core.resources.clan.Clan;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClanData implements DataAsync {

    private final MongoCollection<Document> collection;

    private final RedisDatabase redis;

    private final ClanManager manager;

    private final String CLAN_KEY = "clan:";

    public ClanData(MongoDatabase mongoDB, RedisDatabase redis) {
        this.collection = mongoDB.getDatabase().getCollection("clans");
        this.redis = redis;

        this.manager = Core.getClanManager();

        // Creating indexes
        collection.createIndexes(Arrays.asList(
                new IndexModel(new Document("id", 1)),
                new IndexModel(new Document("name", 1)),
                new IndexModel(new Document("tag", 1))
        ));
    }

    public void save(Clan clan) {
        Document element = collection.find(Filters.eq("id", clan.getId().toString())).first();

        if (element == null) {
            collection.insertOne(Document.parse(Core.GSON.toJson(clan)));

            redis.save(CLAN_KEY + clan.getId(), clan);

            manager.save(clan);
        }
    }

    public void delete(UUID id) {
        Document element = collection.find(Filters.eq("id", id.toString())).first();

        if (element != null) {
            collection.deleteOne(element);

            redis.delete(CLAN_KEY + id);

            manager.remove(id);
        }
    }

    public Clan read(UUID id) {
        Clan clan = manager.read(id);

        if (clan == null) {
            clan = redis.load(CLAN_KEY + id, Clan.class);

            if (clan == null) {
                Document element = collection.find(Filters.eq("id", id.toString())).first();

                if (element != null)
                    clan = Core.GSON.fromJson(element.toJson(), Clan.class);
            }
        }

        return clan;
    }

    public Clan readByName(String name) {
        Clan clan = manager.fetchByName(name);

        if (clan == null) {
            Document element = collection.find(Filters.regex("name", Validator.caseInsensitive(name))).first();

            if (element != null)
                clan = Core.GSON.fromJson(element.toJson(), Clan.class);
        }

        return clan;
    }

    public Clan readByTag(String tag) {
        Clan clan = manager.fetchByTag(tag);

        if (clan == null) {
            Document element = collection.find(Filters.regex("tag", Validator.caseInsensitive(tag))).first();

            if (element != null)
                clan = Core.GSON.fromJson(element.toJson(), Clan.class);
        }

        return clan;
    }

    public void update(Clan clan, String field) {
        runAsync(() -> {
            JsonObject tree = JsonUtil.jsonTree(clan);
            Object value = tree.has(field) ? JsonUtil.elementToBson(tree.get(field)) : null;

            Document element = collection.find(Filters.eq("id", clan.getId().toString())).first();

            if (element != null) {
                collection.updateOne(element, new Document(value != null ? "$set" : "$unset", new Document(field, value)));

                JsonObject message = new JsonObject();

                message.addProperty("id", clan.getId().toString());
                message.addProperty("field", field);

                message.add("value", tree.get(field));

                if (tree.get(field) == null) {
                    redis.delete(CLAN_KEY + clan.getId(), field);
                }

                redis.update(CLAN_KEY + clan.getId(), clan);

                redis.publish(Constant.REDIS_CLAN_CHANNEL, message.toString());
            }
        });
    }

    public List<Clan> ranking(String field, int limit) {
        List<Document> documentList = collection
                .find()
                .sort(Sorts.descending(field))
                .limit(limit)
                .into(new ArrayList<>());

        List<Clan> clans = new ArrayList<>();

        documentList.forEach(document -> clans.add(Core.GSON.fromJson(document.toJson(), Clan.class)));

        return clans;
    }
}
