package br.com.poison.core.backend.data;

import br.com.poison.core.Core;
import br.com.poison.core.backend.database.mongodb.MongoDatabase;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.punishment.Punishment;
import br.com.poison.core.resources.punishment.category.PunishmentCategory;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentData {

    private final MongoCollection<Document> collection;

    public PunishmentData(MongoDatabase mongoDB) {
        this.collection = mongoDB.getDatabase().getCollection("punishments");

        // Criando index
        collection.createIndexes(Arrays.asList(
                new IndexModel(new Document("code", 1)),
                new IndexModel(new Document("punished", 1)),
                new IndexModel(new Document("ipAddress", 1))
        ));
    }

    public void input(Punishment punishment) {
        Document element = collection.find(Filters.eq("code", punishment.getCode())).first();

        if (element == null)
            collection.insertOne(Document.parse(Core.GSON.toJson(punishment)));
    }

    public void delete(Punishment punishment) {
        Document element = collection.find(Filters.eq("code", punishment.getCode())).first();

        if (element != null)
            collection.deleteOne(element);
    }

    public Punishment fetchByCode(String code) {
        Punishment punishment = null;

        Document element = collection.find(Filters.eq("code", code)).first();

        if (element != null)
            punishment = Core.GSON.fromJson(element.toJson(), Punishment.class);

        return punishment;
    }

    public Punishment fetchByAddress(String ipAddress) {
        Punishment punishment = listAll(PunishmentCategory.BAN)
                .stream()
                .filter(ban -> ban.getIpAddress().equalsIgnoreCase(ipAddress) && ban.isValid())
                .findFirst()
                .orElse(null);

        if (punishment == null) {
            Document element = collection.find(Filters.eq("ipAddress", ipAddress)).first();

            if (element != null)
                punishment = Core.GSON.fromJson(element.toJson(), Punishment.class);
        }

        return punishment;
    }

    public Punishment getActivePunishment(UUID userId, PunishmentCategory category) {
        return listAll(userId, category)
                .stream()
                .filter(Punishment::isValid)
                .findFirst()
                .orElse(null);
    }

    public Punishment hasActiveBan(Profile profile) {
        return listAll(PunishmentCategory.BAN).stream()
                .filter(punishment -> punishment.getPunished().getName().equalsIgnoreCase(profile.getName())
                        && punishment.getPunished().getUuid().equals(profile.getId())
                        || punishment.getIpAddress().equalsIgnoreCase(profile.getIpAddress()))
                .findFirst()
                .orElse(null);
    }

    public boolean hasActiveMute(UUID userId) {
        return listAll(userId, PunishmentCategory.MUTE)
                .stream()
                .anyMatch(Punishment::isValid);
    }

    public List<Punishment> listAll() {
        List<Document> documentList = collection
                .find()
                .into(new ArrayList<>());

        List<Punishment> punishments = new ArrayList<>();

        documentList.forEach(document -> punishments.add(Core.GSON.fromJson(document.toJson(), Punishment.class)));

        return punishments;
    }

    public List<Punishment> listAll(PunishmentCategory category) {
        return listAll().stream().filter(punishment -> punishment.getCategory().equals(category)).collect(Collectors.toList());
    }


    public List<Punishment> listAll(UUID punished, PunishmentCategory category) {
        return listAll(category).stream().filter(punishment -> punishment.getPunished().getUuid().equals(punished)).collect(Collectors.toList());
    }
}
