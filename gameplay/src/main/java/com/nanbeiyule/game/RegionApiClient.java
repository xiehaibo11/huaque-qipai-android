package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class RegionApiClient {
    interface Callback<T> {
        void onSuccess(T result);

        default void onUnauthorized() {
            onError("登录状态已失效");
        }

        void onError(String message);
    }

    record Lobby(long lobbyId, String areaName, int sortOrder) {}

    record City(
            String code,
            String name,
            int sortOrder,
            int mapX,
            int mapY,
            String secondaryMap,
            List<Lobby> lobbies) {
        City {
            lobbies = List.copyOf(lobbies);
        }
    }

    record Catalog(long defaultLobbyId, List<City> cities) {
        Catalog {
            cities = List.copyOf(cities);
        }

        List<Lobby> allLobbies() {
            List<Lobby> result = new ArrayList<>();
            for (City city : cities) {
                result.addAll(city.lobbies());
            }
            Collections.sort(
                    result,
                    new Comparator<Lobby>() {
                        @Override
                        public int compare(Lobby left, Lobby right) {
                            return Integer.compare(left.sortOrder(), right.sortOrder());
                        }
                    });
            return result;
        }

        Lobby findLobby(long lobbyId) {
            for (City city : cities) {
                for (Lobby lobby : city.lobbies()) {
                    if (lobby.lobbyId() == lobbyId) {
                        return lobby;
                    }
                }
            }
            return null;
        }

        City findCity(long lobbyId) {
            for (City city : cities) {
                for (Lobby lobby : city.lobbies()) {
                    if (lobby.lobbyId() == lobbyId) {
                        return city;
                    }
                }
            }
            return null;
        }
    }

    private interface ResponseParser<T> {
        T parse(JSONObject body) throws JSONException;
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    RegionApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    RegionApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadCatalog(Callback<Catalog> callback) {
        request(
                "GET",
                "/api/v1/regions",
                null,
                null,
                RegionApiClient::parseCatalog,
                callback);
    }

    void saveSelection(
            long lobbyId, String accessToken, Callback<Long> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("lobbyId", lobbyId);
        } catch (JSONException impossible) {
            callback.onError("无法创建地区选择请求");
            return;
        }
        request(
                "PUT",
                "/api/v1/regions/selection",
                body,
                accessToken,
                response -> response.getLong("lobbyId"),
                callback);
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            JSONObject requestBody,
            String accessToken,
            ResponseParser<T> parser,
            Callback<T> callback) {
        if (baseUrl.isEmpty()) {
            callback.onError("区域服务地址尚未配置");
            return;
        }
        executor.execute(
                () -> {
                    HttpURLConnection connection = null;
                    try {
                        connection =
                                (HttpURLConnection)
                                        new URL(baseUrl + path).openConnection();
                        connection.setRequestMethod(method);
                        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                        connection.setReadTimeout(READ_TIMEOUT_MS);
                        connection.setRequestProperty("Accept", "application/json");
                        if (accessToken != null && !accessToken.isBlank()) {
                            connection.setRequestProperty(
                                    "Authorization", "Bearer " + accessToken);
                        }
                        if (requestBody != null) {
                            connection.setDoOutput(true);
                            connection.setRequestProperty(
                                    "Content-Type",
                                    "application/json; charset=UTF-8");
                            byte[] payload =
                                    requestBody
                                            .toString()
                                            .getBytes(StandardCharsets.UTF_8);
                            connection.setFixedLengthStreamingMode(payload.length);
                            try (OutputStream output = connection.getOutputStream()) {
                                output.write(payload);
                            }
                        }
                        int statusCode = connection.getResponseCode();
                        String responseText =
                                readBody(
                                        statusCode >= 200 && statusCode < 300
                                                ? connection.getInputStream()
                                                : connection.getErrorStream());
                        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            postUnauthorized(callback);
                            return;
                        }
                        if (statusCode < 200 || statusCode >= 300) {
                            postError(callback, parseError(responseText));
                            return;
                        }
                        postSuccess(
                                callback,
                                parser.parse(new JSONObject(responseText)));
                    } catch (Exception exception) {
                        postError(callback, "无法连接区域服务，请检查网络后重试");
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                });
    }

    private static Catalog parseCatalog(JSONObject body) throws JSONException {
        long defaultLobbyId = body.getLong("defaultLobbyId");
        JSONArray cityArray = body.getJSONArray("cities");
        List<City> cities = new ArrayList<>(cityArray.length());
        for (int cityIndex = 0; cityIndex < cityArray.length(); cityIndex++) {
            JSONObject cityBody = cityArray.getJSONObject(cityIndex);
            JSONArray lobbyArray = cityBody.getJSONArray("lobbies");
            List<Lobby> lobbies = new ArrayList<>(lobbyArray.length());
            for (int lobbyIndex = 0;
                    lobbyIndex < lobbyArray.length();
                    lobbyIndex++) {
                JSONObject lobbyBody = lobbyArray.getJSONObject(lobbyIndex);
                lobbies.add(
                        new Lobby(
                                lobbyBody.getLong("lobbyId"),
                                lobbyBody.getString("areaName"),
                                lobbyBody.getInt("sortOrder")));
            }
            cities.add(
                    new City(
                            cityBody.getString("code"),
                            cityBody.getString("name"),
                            cityBody.getInt("sortOrder"),
                            cityBody.getInt("mapX"),
                            cityBody.getInt("mapY"),
                            cityBody.optString("secondaryMap", ""),
                            lobbies));
        }
        Collections.sort(
                cities,
                new Comparator<City>() {
                    @Override
                    public int compare(City left, City right) {
                        return Integer.compare(left.sortOrder(), right.sortOrder());
                    }
                });
        return new Catalog(defaultLobbyId, cities);
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static String parseError(String responseText) {
        if (!responseText.isBlank()) {
            try {
                String detail = new JSONObject(responseText).optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall through to the stable public message.
            }
        }
        return "地区服务请求失败，请稍后重试";
    }

    private <T> void postSuccess(Callback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postUnauthorized(Callback<?> callback) {
        mainHandler.post(callback::onUnauthorized);
    }

    private void postError(Callback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
