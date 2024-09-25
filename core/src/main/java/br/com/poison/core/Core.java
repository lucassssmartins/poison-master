package br.com.poison.core;

import br.com.poison.core.backend.data.*;
import br.com.poison.core.backend.data.member.DuelData;
import br.com.poison.core.backend.data.member.PvPData;
import br.com.poison.core.backend.database.mongodb.MongoDatabase;
import br.com.poison.core.backend.database.redis.RedisDatabase;
import br.com.poison.core.manager.*;
import br.com.poison.core.manager.member.DuelManager;
import br.com.poison.core.manager.member.PvPManager;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.mojang.MojangAPI;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

public class Core {

    public static Gson GSON = new Gson();
    public static JsonParser PARSER = new JsonParser();
    public static Random RANDOM = new Random();
    public static MojangAPI MOJANG_API = new MojangAPI();

    @Getter
    @Setter
    protected static MultiService multiService;

    @Getter
    @Setter
    protected static Logger logger;

    @Getter
    @Setter
    protected static MongoDatabase mongoDatabase;

    @Getter
    @Setter
    protected static RedisDatabase redisDatabase;

    @Getter
    @Setter
    protected static ProfileData profileData;

    @Getter
    @Setter
    protected static PunishmentData punishmentData;

    @Getter
    @Setter
    protected static ClanData clanData;

    @Getter
    @Setter
    protected static ReportData reportData;

    @Getter
    @Setter
    protected static DuelData duelData;

    @Getter
    @Setter
    protected static PvPData pvpData;

    @Getter
    @Setter
    protected static ServerManager serverManager;

    @Getter
    @Setter
    protected static ServerCategory serverCategory = ServerCategory.PROXY;

    @Getter
    protected static ProfileManager profileManager = new ProfileManager();

    @Getter
    protected static SkinManager skinManager = new SkinManager();

    @Getter
    protected static ClanManager clanManager = new ClanManager();

    @Getter
    protected static DuelManager duelManager = new DuelManager();

    @Getter
    protected static PvPManager pvpManager = new PvPManager();

    @Getter
    protected static InvitationManager invitationManager = new InvitationManager();

    public static void init(MultiService multiService, Logger logger, ServerManager serverManager) {
        logger.info("Loading resources...");

        System.setProperty("console.encoding", "UTF-8");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"));

        setMultiService(multiService);
        setLogger(logger);

        // Database Connections

        /* MongoDB */
        MongoDatabase mongoDB = new MongoDatabase(true);
        mongoDB.init();

        /* Redis */
        RedisDatabase redis = new RedisDatabase(true);
        redis.init();

        setMongoDatabase(mongoDB);
        setRedisDatabase(redis);

        setProfileData(new ProfileData(mongoDB, redis));
        setPunishmentData(new PunishmentData(mongoDB));
        setClanData(new ClanData(mongoDB, redis));
        setReportData(new ReportData(redis));
        setDuelData(new DuelData(mongoDB, redis));
        setPvpData(new PvPData(mongoDB, redis));

        setServerManager(serverManager);

        logger.info("Resources loaded successfully.");
    }

    public static void closeup() {
        logger.info("Closing resources...");

        serverManager.end();

        profileManager.clearAll();

        setMultiService(null);
        setServerManager(null);

        logger.info("Resources closed successfully.");
    }

    public static boolean isDeveloper(UUID uuid) {
        return uuid.equals(UUID.fromString("0300ef8b-2dbc-4dc7-bf34-e37b70a04fde"));
    }
}