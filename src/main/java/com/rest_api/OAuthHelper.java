package com.rest_api;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Scanner;

public class OAuthHelper {

    public static String[] getAccessTokenInteractive(String clientId, String clientSecret, String redirectUri) throws IOException {
        String authUrl = buildAuthUrl(clientId, redirectUri);

        System.out.println("Go to the following URL and authorize:");
        System.out.println(authUrl);
        System.out.print("Paste the authorization code here: ");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        scanner.close();

        String body = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                URLEncoder.encode(code, "UTF-8"),
                URLEncoder.encode(clientId, "UTF-8"),
                URLEncoder.encode(clientSecret, "UTF-8"),
                URLEncoder.encode(redirectUri, "UTF-8")
        );

        String response = postRequest("https://oauth2.googleapis.com/token", body);
        String access = extractJsonValue(response, "access_token");
        String refresh = extractJsonValue(response, "refresh_token");
        return new String[]{access, refresh};
    }

    public static String refreshAccessToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        String body = String.format(
                "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                URLEncoder.encode(clientId, "UTF-8"),
                URLEncoder.encode(clientSecret, "UTF-8"),
                URLEncoder.encode(refreshToken, "UTF-8")
        );

        String response = postRequest("https://oauth2.googleapis.com/token", body);
        return extractJsonValue(response, "access_token");
    }

    private static String buildAuthUrl(String clientId, String redirectUri) {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?scope=https://www.googleapis.com/auth/spreadsheets" +
                "&access_type=offline&include_granted_scopes=true&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&client_id=" + clientId;
    }

    private static String postRequest(String urlString, String body) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            int responseCode = conn.getResponseCode();
            boolean isError = responseCode >= 400;

            try (InputStream stream = isError ? conn.getErrorStream() : conn.getInputStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {

                StringBuilder resp = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }

                if (isError) {
                    throw new IOException("HTTP error " + responseCode + ": " + resp);
                }

                return resp.toString();
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String extractJsonValue(String json, String key) {
        Map<String, Object> map = new Gson().fromJson(json, Map.class);
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalStateException("Key \"" + key + "\" not found in JSON response: " + json);
        }
        return value.toString();
    }
}
