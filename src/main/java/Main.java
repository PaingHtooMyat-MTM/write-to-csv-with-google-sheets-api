import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.opencsv.CSVWriter;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.InputStream;
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

        // Use Sheets API
        String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        String range = "'Class Data'";
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();

        System.out.println("Values: " + response.getValues());
        List<List<Object>> data = response.getValues();

        if (data == null || data.isEmpty()) {
            System.out.println("No data found.");
        } else {
            try (CSVWriter writer = new CSVWriter(new FileWriter("data.csv"))) {
                for (List<Object> row : data) {
                    String[] csvRow = row.stream()
                            .map(Object::toString)
                            .toArray(String[]::new);
                    writer.writeNext(csvRow);
                }
                System.out.println("Data written to data.csv successfully!");
            }
        }
    }
}
