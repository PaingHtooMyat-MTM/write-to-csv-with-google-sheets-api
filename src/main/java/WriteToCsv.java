import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.util.List;

public class WriteToCsv {

    public static void WriteDataToCsv(List<List<Object>> data) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
