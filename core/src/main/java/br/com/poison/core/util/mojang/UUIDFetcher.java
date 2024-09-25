package br.com.poison.core.util.mojang;

import br.com.poison.core.Core;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UUIDFetcher {

    private static final Cache<String, String> nameCache = CacheBuilder.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(@NonNull String name) {
                    return name;
                }
            });

    private static final List<String> APIS = Arrays.asList(
            "https://api.mojang.com/users/profiles/minecraft/%s",
            "https://api.minetools.eu/uuid/%s",
            "https://api.mcuuid.com/v1/uuid/%s"
    );

    public static UUID request(String name) {
        return request(0, APIS.get(0), name);
    }

    public static boolean isOnlineMode(String name) {
        boolean mode = false;

        try {
            HttpURLConnection conn = getConnection(String.format(APIS.get(0), name));

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine();

                if (line != null && !line.equals("null"))
                    mode = true;
            }
        } catch (Exception ignored) {
        }

        return mode;
    }

    private static UUID request(int idx, String api, String name) {
        try {
            URLConnection connection = new URL(String.format(api, name)).openConnection();
            JsonElement element = Core.PARSER.parse(new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)));

            if (element instanceof JsonObject) {
                JsonObject json = (JsonObject) element;

                if (json.has("error") && json.has("errorMessage"))
                    throw new Exception(json.get("errorMessage").getAsString());
                else if (json.has("id"))
                    return parse(json.get("id"));
                else if (json.has("uuid")) {
                    JsonObject uniqueId = json.getAsJsonObject("uuid");
                    if (uniqueId.has("formatted"))
                        return parse(json.get("formatted"));
                }
            }

        } catch (Exception e) {
            idx++;
            if (APIS.size() > idx) {
                api = APIS.get(idx);
                return request(idx, api, name);
            }
        }

        return null;
    }


    public static String convertUUIDtoName(String uniqueId) {
        try {
            if (nameCache.asMap().get(uniqueId) != null)
                return nameCache.asMap().get(uniqueId);

            URL uniqueIdURL = new URL("https://api.mcuuid.com/v1/profile/" + uniqueId + "/name");
            Scanner scanner = new Scanner(uniqueIdURL.openStream(), "UTF-8");

            String name = null;
            for (int i = 0; i < 3; i++)
                name = scanner.next();

            char ch = '"';
            name = name.replace(Character.toString(ch), "");
            name = name.replace(",", "");

            scanner.close();
            nameCache.asMap().put(uniqueId, name);
            return name;
        } catch (Exception ex) {
            return "Error";
        }
    }


    private static UUID parse(JsonElement element) {
        return parse(element.getAsString());
    }

    private static UUID parse(String string) {
        if (string != null && !string.isEmpty()) {
            return string.matches("[0-9a-fA-F]{32}")
                    ? UUID.fromString(string.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"))
                    : string.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
                    ? UUID.fromString(string) : null;
        }

        return null;
    }

    private static HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Premium-Checker");
        return connection;
    }
}