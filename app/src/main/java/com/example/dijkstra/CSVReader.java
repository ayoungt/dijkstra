package com.example.dijkstra;

//path12345.csv 파일의 내용을 이중리스트로 저장하고 반환하는 클래스. [[a,b,c,d],[e,f,g,h]]
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static List<List<String>> pathCSV(Context context, String fileName) throws IOException {
        List<List<String>> data = new ArrayList<>();

        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        CSVParser csvParser = CSVFormat.DEFAULT.parse(reader);

        for (CSVRecord csvRecord : csvParser) {
            List<String> row = new ArrayList<>();
            for (String value : csvRecord) {
                row.add(value);
            }
            data.add(row);
        }

        inputStream.close();

        return data;
    }

}
