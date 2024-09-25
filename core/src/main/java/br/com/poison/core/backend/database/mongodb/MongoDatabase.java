package br.com.poison.core.backend.database.mongodb;

import br.com.poison.core.Constant;
import br.com.poison.core.backend.database.Database;
import br.com.poison.core.backend.database.DatabaseCredentials;
import br.com.poison.core.util.Util;
import br.com.poison.core.Core;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;

import java.util.logging.Level;
import java.util.regex.Pattern;

@Getter
public class MongoDatabase implements Database {

    private static final String PATTERN = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";

    private static final Pattern IP_PATTERN = Pattern
            .compile(PATTERN + "\\." + PATTERN + "\\." + PATTERN + "\\." + PATTERN);

    private final String url;

    private final DatabaseCredentials credentials;

    private MongoClient client;
    private com.mongodb.client.MongoDatabase database;

    public MongoDatabase(String url, String database) {
        this.url = url;

        this.credentials = DatabaseCredentials.builder().database(database).build();
    }

    public MongoDatabase(DatabaseCredentials credential) {
        this.url = (IP_PATTERN.matcher(credential.getHost()).matches()
                ? "mongodb://" + (credential.getUser() == null ? "" : credential.getUser() + ":" + credential.getPassword() + "@")
                + credential.getHost() + ":" + credential.getPort() + "/" + credential.getDatabase()
                + "?retryWrites=true&w=majority"
                : "mongodb+srv://" + ((credential.getUser() == null || credential.getUser().isEmpty()) ? "" : credential.getUser() + ":" + credential.getPassword() + "@")
                + credential.getHost() + ":" + credential.getPort() + "/"
                + credential.getDatabase() + "?retryWrites=true&w=majority");

        this.credentials = credential;
    }

    public MongoDatabase(boolean local) {
        this(local
                // Local
                ? DatabaseCredentials.builder()
                .host("127.0.0.1")
                .port(27017)
                .database(Constant.SERVER_NAME.toLowerCase())
                .build()
                // Server
                : DatabaseCredentials.builder().build());
    }

    @Override
    public void init() {
        long start = System.currentTimeMillis();

        Core.getLogger().info("Conectando ao MongoDB...");

        try {
            ConnectionString connectionString = new ConnectionString(url);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();

            client = MongoClients.create(settings);
            database = client.getDatabase(credentials.getDatabase());

            Core.getLogger().info("Conexão com o banco de dados MongoDB efetuada com sucesso. (Tempo: " + Util.formatMS(start) + "ms)");
        } catch (Exception e) {
            Core.getLogger().log(Level.SEVERE, "Não foi possível conectar ao banco de dados MongoDB...", e);
        }
    }

    @Override
    public void end() {
        if (hasConnection())
            client.close();
    }

    @Override
    public boolean hasConnection() {
        return client != null;
    }
}
