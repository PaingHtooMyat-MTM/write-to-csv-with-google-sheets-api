package com.rest_api;

import com.libraries.Credentials;
import com.libraries.YamlConfigLoader;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        YamlConfigLoader configLoader = new YamlConfigLoader();
        Credentials credentials = configLoader.loadCredentials();

        String CLIENT_ID = credentials.getClient_id();
        String CLIENT_SECRET = credentials.getClient_secret();
        String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
        String SHEET_NAME = "Sheet1";

        Map<String, String> tokens = TokenStorage.loadTokens();

        String accessToken;
        String refreshToken;

        if (tokens.containsKey("access_token") && tokens.containsKey("refresh_token")) {
            refreshToken = tokens.get("refresh_token");
            accessToken = OAuthHelper.refreshAccessToken(refreshToken, CLIENT_ID, CLIENT_SECRET);
            TokenStorage.saveTokens(accessToken, refreshToken);
        } else {
            String[] result = OAuthHelper.getAccessTokenInteractive(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
            accessToken = result[0];
            refreshToken = result[1];
            TokenStorage.saveTokens(accessToken, refreshToken);
        }

        // Create spreadsheet
        String spreadsheetId = SheetsAPI.createSpreadsheet(accessToken, "Java API Sheet", SHEET_NAME);

        // row data and range for CRUD
        List<List<String>> rowToInsert = List.of(
                List.of("Alice", "alice@example.com", "25", "ABC", "100"),
                List.of("Bob", "bob@example.com", "30")
        );
        List<List<String>> rowToUpdate = List.of(List.of("Updated Alice", "updated@example.com", "30"));
        String updateRange = "A3:C3";
        String readRange = "A1:C5";
        String clearRange = "A1:C1";

        // CRUD
        SheetsAPI.appendRow(accessToken, spreadsheetId, SHEET_NAME, rowToInsert);
        SheetsAPI.readSheet(accessToken, spreadsheetId, SHEET_NAME, readRange);
        SheetsAPI.updateRow(accessToken, spreadsheetId, SHEET_NAME, updateRange, rowToUpdate);
        SheetsAPI.clearRow(accessToken, spreadsheetId, SHEET_NAME, clearRange);
    }
}
