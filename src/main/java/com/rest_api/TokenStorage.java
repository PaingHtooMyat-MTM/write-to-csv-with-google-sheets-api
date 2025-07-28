package com.rest_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TokenStorage {
    private static final String FILE_PATH = "data/tokens.json";
    private static final Gson gson = new Gson();

    public static void saveTokens(String accessToken, String refreshToken) throws IOException {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tokens, writer);
        }
    }

    public static Map<String, String> loadTokens() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new HashMap<>();

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            return gson.fromJson(reader, type);
        }
    }
}
