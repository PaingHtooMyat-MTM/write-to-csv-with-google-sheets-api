package com.libraries;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReadFromCsv {
    // Read data from CSV and convert to List<List<Object>>
    public static List<List<Object>> readDataFromCsv(String filePath) {
        List<List<Object>> result = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();

            for (String[] row : rows) {
                List<Object> objectRow = new ArrayList<>();
                for (String cell : row) {
                    objectRow.add(cell); // Or parse types if needed (Integer, Double, etc.)
                }
                result.add(objectRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
