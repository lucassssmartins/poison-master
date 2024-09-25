package br.com.poison.core.backend.data.member;

import br.com.poison.core.backend.data.async.DataAsync;
import br.com.poison.core.util.extra.Validator;
import br.com.poison.core.util.json.JsonUtil;
import com.google.gson.JsonObject;
import br.com.poison.core.Core;
import br.com.poison.core.backend.database.mongodb.MongoDatabase;
import br.com.poison.core.backend.database.redis.RedisDatabase;
import br.com.poison.core.manager.member.DuelManager;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.member.type.duels.DuelMember;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DuelData implements DataAsync {

    private final MongoCollection<Document> collection;

    private final RedisDatabase redis;

    private final DuelManager manager;

    private final String DUEL_KEY = "duels:";

    public DuelData(MongoDatabase mongoDB, RedisDatabase redis) {
        this.collection = mongoDB.getDatabase().getCollection("duels");
        this.redis = redis;

        this.manager = Core.getDuelManager();

        collection.createIndexes(Arrays.asList(
                new IndexModel(new Document("id", 1)),
                new IndexModel(new Document("name", 1)),
                new IndexModel(new Document("xp", 1))
        ));
    }

    public DuelMember input(Profile profile) {
        DuelMember member = new DuelMember(profile);

        Document element = collection.find(Filters.eq("id", profile.getId().toString())).first();

        if (element == null) {
            collection.insertOne(Document.parse(Core.GSON.toJson(member)));

            redis.save(DUEL_KEY + profile.getId(), member);
        }

        return member;
    }

    public DuelMember fetch(UUID id, boolean saveInRedis) {
        DuelMember member = manager.read(id);

        if (member == null) {
            member = redis.load(DUEL_KEY + id, DuelMember.class);

            if (member == null) {
                Document element = collection.find(Filters.eq("id", id.toString())).first();

                if (element != null) {
                    member = Core.GSON.fromJson(element.toJson(), DuelMember.class);

                    if (saveInRedis)
                        redis.save(DUEL_KEY + id, member);
                }
            }
        }

        return member;
    }

    public DuelMember fetch(String name) {
        DuelMember member = manager.fetch(name);

        if (member == null) {
            Document element = collection.find(Filters.regex("name", Validator.caseInsensitive(name))).first();

            if (element != null)
                member = Core.GSON.fromJson(element.toJson(), DuelMember.class);
        }

        return member;
    }

    public void cache(UUID id) {
        redis.cache(DUEL_KEY + id, 200);

        manager.remove(id);
    }

    public void persist(UUID id) {
        redis.persist(DUEL_KEY + id);
    }

    public void update(DuelMember member, String field) {
        runAsync(() -> {
            JsonObject tree = JsonUtil.jsonTree(member);
            Object value = tree.has(field) ? JsonUtil.elementToBson(tree.get(field)) : null;

            Document element = collection.find(Filters.eq("id", member.getId().toString())).first();

            if (element != null) {
                collection.updateOne(element, new Document(value != null ? "$set" : "$unset", new Document(field, value)));

                redis.update(DUEL_KEY + member.getId(), member);

                manager.save(member);
            }
        });
    }

    public List<DuelMember> ranking(String field, int limit) {
        List<Document> documentList = collection.find()
                .sort(Sorts.descending(field))
                .limit(limit)
                .into(new ArrayList<>());

        List<DuelMember> members = new ArrayList<>();

        documentList.forEach(document -> members.add(Core.GSON.fromJson(document.toJson(), DuelMember.class)));

        return members;
    }
}
