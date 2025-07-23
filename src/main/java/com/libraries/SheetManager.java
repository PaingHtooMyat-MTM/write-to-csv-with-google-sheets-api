package com.libraries;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SheetManager {
    private final Sheets service;
    private final String spreadsheetId;

    public SheetManager(Sheets service, String spreadsheetId) {
        this.service = service;
        this.spreadsheetId = spreadsheetId;
    }

    // CREATE: Create a new spreadsheet
    public static String createNewSpreadsheet(Sheets service, String title) {
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(title));

        try {
            Spreadsheet result = service.spreadsheets().create(spreadsheet).execute();
            String newSpreadsheetId = result.getSpreadsheetId();
            System.out.println("Created spreadsheet: " + title);
            System.out.println("Spreadsheet ID: " + newSpreadsheetId);
            return newSpreadsheetId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // CREATE: Add a new sheet
    public int createSheet(String sheetName) {
        AddSheetRequest addSheetRequest = new AddSheetRequest()
                .setProperties(new SheetProperties().setTitle(sheetName));

        Request request = new Request().setAddSheet(addSheetRequest);
        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(request));

        try {
            BatchUpdateSpreadsheetResponse response = service.spreadsheets()
                    .batchUpdate(spreadsheetId, batchRequest)
                    .execute();

            int newSheetId = response.getReplies().get(0).getAddSheet().getProperties().getSheetId();
            System.out.println("Created sheet: " + sheetName);
            return newSheetId;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // READ: Read data from a range
    public List<List<Object>> readRange(String range) {
        try {
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            return values != null ? values : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // UPDATE: Overwrite data in a range
    public void updateRange(String range, List<List<Object>> data) {
        ValueRange body = new ValueRange().setValues(data);
        try {
            service.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.println("Updated range: " + range);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // DELETE: Clear data in a range
    public void clearRange(String range) {
        try {
            ClearValuesRequest request = new ClearValuesRequest();
            service.spreadsheets().values()
                    .clear(spreadsheetId, range, request)
                    .execute();
            System.out.println("Cleared range: " + range);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // DELETE: Delete a sheet by name
    public void deleteSheetByName(String sheetName) {
        try {
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
            Integer sheetId = spreadsheet.getSheets().stream()
                    .filter(s -> s.getProperties().getTitle().equals(sheetName))
                    .map(s -> s.getProperties().getSheetId())
                    .findFirst()
                    .orElse(null);

            if (sheetId != null) {
                DeleteSheetRequest deleteRequest = new DeleteSheetRequest().setSheetId(sheetId);
                BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                        .setRequests(Collections.singletonList(new Request().setDeleteSheet(deleteRequest)));

                service.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute();
                System.out.println("Deleted sheet: " + sheetName);
            } else {
                System.out.println("Sheet not found: " + sheetName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
