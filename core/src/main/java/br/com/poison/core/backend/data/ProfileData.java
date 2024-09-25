package br.com.poison.core.backend.data;

import br.com.poison.core.util.Util;
import br.com.poison.core.util.extra.Validator;
import br.com.poison.core.util.json.JsonUtil;
import br.com.poison.core.backend.data.async.DataAsync;
import br.com.poison.core.backend.database.redis.RedisDatabase;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.backend.database.mongodb.MongoDatabase;
import br.com.poison.core.manager.ProfileManager;
import br.com.poison.core.profile.Profile;
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
import java.util.logging.Level;

public class ProfileData implements DataAsync {

    private final MongoCollection<Document> collection;

    private final RedisDatabase redis;

    private final ProfileManager manager;

    private final String PROFILE_KEY = "profile:";

    public ProfileData(MongoDatabase mongoDB, RedisDatabase redis) {
        this.collection = mongoDB.getDatabase().getCollection("profiles");
        this.redis = redis;

        this.manager = Core.getProfileManager();

        // Creating indexes
        collection.createIndexes(Arrays.asList(
                new IndexModel(new Document("id", 1)),
                new IndexModel(new Document("name", 1))
        ));

        Core.getLogger().info("[Profiles] " + Util.formatNumber(size()) + " documentos registrados.");
    }

    public Profile save(UUID id, String name) {
        Profile profile = new Profile(id, name);

        String json = Core.GSON.toJson(profile);

        if (json != null)
            collection.insertOne(Document.parse(json));

        redis.save(PROFILE_KEY + id, profile);

        return profile;
    }

    public boolean exists(UUID id) {
        return collection.find(Filters.eq("id", id.toString())).first() != null;
    }

    public Profile read(UUID id, boolean saveInRedis) {
        Profile profile = manager.read(id);

        if (profile == null) {
            profile = redis.load(PROFILE_KEY + id, Profile.class);

            if (profile == null) {
                Document document = collection.find(Filters.eq("id", id.toString())).first();

                if (document != null) {
                    profile = Core.GSON.fromJson(document.toJson(), Profile.class);

                    if (saveInRedis)
                        redis.save(PROFILE_KEY + id, profile);
                }
            }
        }

        return profile;
    }

    public Profile read(String name) {
        Profile profile = manager.read(name);

        if (profile == null) {
            Document document = collection.find(Filters.eq("name", Validator.caseInsensitive(name))).first();

            if (document != null)
                profile = Core.GSON.fromJson(document.toJson(), Profile.class);
        }

        return profile;
    }

    public void update(Profile profile, String field) {
        runAsync(() -> {
            try {
                JsonObject tree = JsonUtil.jsonTree(profile);
                Object value = tree.has(field) ? JsonUtil.elementToBson(tree.get(field)) : null;

                Document element = collection.find(Filters.eq("id", profile.getId().toString())).first();

                if (element != null)
                    collection.updateOne(element, new Document(value != null ? "$set" : "$unset", new Document(field, value)));

                JsonObject message = new JsonObject();

                message.addProperty("id", profile.getId().toString());
                message.addProperty("field", field);
                message.add("value", tree.get(field));

                redis.publish(Constant.REDIS_PROFILE_CHANNEL, message.toString());

                if (value == null)
                    redis.delete(PROFILE_KEY + profile.getId(), field);

                redis.update(PROFILE_KEY + profile.getId(), profile);
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "[" + e.getClass().getSimpleName() + "] Não foi possível salvar os dados!", e);
            }
        });
    }

    public void cache(UUID id) {
        redis.cache(PROFILE_KEY + id, 300);

        manager.remove(id);
    }

    public void persist(UUID id) {
        redis.persist(PROFILE_KEY + id);
    }

    public List<Profile> ranking(String field, int limit) {
        List<Document> documents = collection
                .find()
                .sort(Sorts.descending(field))
                .limit(limit)
                .into(new ArrayList<>());

        List<Profile> profiles = new ArrayList<>();

        documents.forEach(document -> profiles.add(Core.GSON.fromJson(document.toJson(), Profile.class)));

        return profiles;
    }

    public int size() {
        return (int) collection.estimatedDocumentCount();
    }
}
