package com.rest_api;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class OAuthHelper {

    public static String[] getAccessTokenInteractive(String clientId, String clientSecret, String redirectUri) throws IOException {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?scope=https://www.googleapis.com/auth/spreadsheets" +
                "&access_type=offline&include_granted_scopes=true&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&client_id=" + clientId;

        System.out.println("Go to the following URL and authorize:");
        System.out.println(authUrl);
        System.out.print("Paste the authorization code here: ");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        scanner.close();

        String body = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                URLEncoder.encode(code, "UTF-8"),
                clientId,
                clientSecret,
                URLEncoder.encode(redirectUri, "UTF-8")
        );

        HttpURLConnection conn = (HttpURLConnection) new URL("https://oauth2.googleapis.com/token").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) resp.append(line);

        String access = resp.toString().split("\"access_token\"\\s*:\\s*\"")[1].split("\"")[0];
        String refresh = resp.toString().split("\"refresh_token\"\\s*:\\s*\"")[1].split("\"")[0];
        return new String[]{access, refresh};
    }

    public static String refreshAccessToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        String body = String.format(
                "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                clientId, clientSecret, refreshToken
        );

        HttpURLConnection conn = (HttpURLConnection) new URL("https://oauth2.googleapis.com/token").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) resp.append(line);

        return resp.toString().split("\"access_token\"\\s*:\\s*\"")[1].split("\"")[0];
    }
}
