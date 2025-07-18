import com.google.api.services.sheets.v4.Sheets;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();

        InputStream inputStream = Main.class
                .getClassLoader()
                .getResourceAsStream("application.yml");

        Credentials credentials = yaml.loadAs(inputStream, Credentials.class);

        String clientId = credentials.getInstalled().getClient_id();
        String clientSecret = credentials.getInstalled().getClient_secret();

        String refreshToken = SheetsServiceUtil.getRefreshToken(clientId, clientSecret);

        String accessToken = SheetsServiceUtil.getAccessTokenFromRefreshToken(clientId, clientSecret, refreshToken);

        Sheets service = SheetsServiceUtil.getSheetsServiceWithAccessToken(accessToken);

        // Sheets API

        // read Example Sheet (from Google)
        String exampleSpreadSheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        SheetManager exampleSheetManager = new SheetManager(service, exampleSpreadSheetId);
        String range = "'Class Data'";
        List<List<Object>> readDataFromExample = exampleSheetManager.readRange(range);

        // Write to CSV
        WriteToCsv.WriteDataToCsv(readDataFromExample);


        // Create a new spreadsheet
        String newSpreadsheetId = SheetManager.createNewSpreadsheet(service, "My New Spreadsheet");
        SheetManager manager = new SheetManager(service, newSpreadsheetId);

        // Create a sheet within spreadsheet
        manager.createSheet("MyData");

        // Write data
        List<List<Object>> data = Arrays.asList(
                Arrays.asList("Name", "Age"),
                Arrays.asList("Alice", "30"),
                Arrays.asList("Bob", "25")
        );
        manager.updateRange("MyData!A1", data);

        // Read data
        List<List<Object>> readData = manager.readRange("MyData!A1:B3");
        readData.forEach(row -> System.out.println(row));

        // Create a new sheet within spreadsheet and upload data from CSV to the new Sheet
        manager.createSheet("DataFromCsv");
        List<List<Object>> DataFromCsv = ReadFromCsv.readDataFromCsv("data.csv");

        manager.updateRange("DataFromCsv", DataFromCsv);

        // Clear data
//        manager.clearRange("MyData!A2:B3");

        // Delete sheet
//        manager.deleteSheetByName("MyData");
    }
}
