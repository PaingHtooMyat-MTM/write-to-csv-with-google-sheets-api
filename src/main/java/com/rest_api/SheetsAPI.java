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
        return sendWithRetry(url, token, "GET", null);
    }

    private static HttpURLConnection postRequest(String url, String token, String json) throws IOException {
        return sendWithRetry(url, token, "POST", json);
    }

    private static HttpURLConnection putRequest(String url, String token, String json) throws IOException {
        return sendWithRetry(url, token, "PUT", json);
    }

    private static HttpURLConnection sendWithRetry(String url, String token, String method, String bodyJson) throws IOException {
        int[] retryDelays = {5, 10, 20}; // in seconds
        int attempts = 0;

        while (true) {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");

            if (!method.equals("GET") && bodyJson != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyJson.getBytes());
                }
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 429) {
                return conn;
            }

            if (attempts >= retryDelays.length) {
                throw new IOException("Exceeded max retries for 429 Too Many Requests.");
            }

            // Handle 429 retry logic
            int waitTime = retryDelays[Math.min(attempts, retryDelays.length - 1)];
            System.out.println("Retry attempts: " + attempts + " retry Delays: " + waitTime);

            System.out.println("Received 429 Too Many Requests. Retrying in " + waitTime + " seconds...");
            try {
                Thread.sleep(waitTime * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Retry interrupted", e);
            }
            attempts++;
        }
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (InputStream stream = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            System.out.println(response);
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }
}
