package com.rest_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class SheetsAPI {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String createSpreadsheet(String accessToken, String title, String sheetName) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put("title", title);
        requestBody.put("properties", properties);

        Map<String, Object> sheet = new HashMap<>();
        Map<String, Object> sheetProps = new HashMap<>();
        sheetProps.put("title", sheetName);
        sheet.put("properties", sheetProps);
        requestBody.put("sheets", List.of(sheet));

        String json = gson.toJson(requestBody);

        HttpURLConnection conn = postRequest("https://sheets.googleapis.com/v4/spreadsheets", accessToken, json);
        String response = readResponse(conn);

        Map<?, ?> result = gson.fromJson(response, Map.class);
        return (String) result.get("spreadsheetId");
    }

    public static void appendRow(String token, String sheetId, String sheetName, List<List<String>> values) throws IOException {
        String url = String.format(
                "https://sheets.googleapis.com/v4/spreadsheets/%s/values/%s!A1:append?valueInputOption=USER_ENTERED",
                sheetId, sheetName
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("values", values);
        String json = gson.toJson(requestBody);

        HttpURLConnection conn = postRequest(url, token, json);
        readResponse(conn);
    }

    public static void readSheet(String token, String sheetId, String sheetName, String range) throws IOException {
        String url = String.format("https://sheets.googleapis.com/v4/spreadsheets/%s/values/%s!%s", sheetId, sheetName, range);
        HttpURLConnection conn = getRequest(url, token);
        String response = readResponse(conn);

        Object jsonObject = gson.fromJson(response, Object.class);
        System.out.println(gson.toJson(jsonObject));
    }

    public static void updateRow(String token, String sheetId, String sheetName, String range, List<List<String>> values) throws IOException {
        String url = String.format("https://sheets.googleapis.com/v4/spreadsheets/%s/values/%s!%s?valueInputOption=USER_ENTERED", sheetId, sheetName, range);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("values", values);
        String json = gson.toJson(requestBody);

        HttpURLConnection conn = putRequest(url, token, json);
        readResponse(conn);
    }

    public static void clearRow(String token, String sheetId, String sheetName, String range) throws IOException {
        String url = String.format("https://sheets.googleapis.com/v4/spreadsheets/%s/values/%s!%s:clear", sheetId, sheetName, range);
        HttpURLConnection conn = postRequest(url, token, "{}");
        readResponse(conn);
    }

    private static HttpURLConnection getRequest(String url, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        return conn;
    }

    private static HttpURLConnection postRequest(String url, String token, String json) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        return conn;
    }

    private static HttpURLConnection putRequest(String url, String token, String json) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        System.out.println(response);
        return response.toString();
    }
}
