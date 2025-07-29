package com.rest_api;

import com.libraries.ReadFromCsv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewMain {
    public static void main(String[] args) throws Exception {
        // ==== Manual CLI Argument Parsing ====
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("-")) {
                argMap.put(args[i], args[i + 1]);
                i++; // Skip value
            }
        }

        final String CLIENT_ID = argMap.get("-i");
        final String CLIENT_SECRET = argMap.get("-s");
        final String REFRESH_TOKEN = argMap.get("-r");
        final String SHEET_NAME = argMap.get("-n");
        final String SHEET_URL = argMap.get("-u");
        final String CSV_FILE_PATH = argMap.get("-f");

        if (CLIENT_ID == null || CLIENT_SECRET == null || REFRESH_TOKEN == null || SHEET_NAME == null || SHEET_URL == null || CSV_FILE_PATH == null) {
            System.err.println("Missing required arguments:");
            System.err.println("Usage: java -jar myapp.jar -i <client_id> -s <client_secret> -r <refresh_token> -n <sheet_name> -u <sheet_url> -f <csv_file_path>");
            System.exit(1);
        }

        final String SHEET_ID = extractSpreadsheetId(SHEET_URL);
        System.out.println(SHEET_ID);

        // === Refresh access token using refreshToken from CLI ===
        String accessToken = OAuthHelper.refreshAccessToken(REFRESH_TOKEN, CLIENT_ID, CLIENT_SECRET);

        SheetsAPI.clearAllData(accessToken, SHEET_ID, SHEET_NAME);

        List<List<Object>> dataFromCsv = ReadFromCsv.readDataFromCsv(CSV_FILE_PATH);
        SheetsAPI.appendRow(accessToken, SHEET_ID, SHEET_NAME, dataFromCsv);
    }

    public static String extractSpreadsheetId(String url) {
        String pattern = "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)(/.*)?";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid Google Sheets URL: " + url);
        }
    }
}

