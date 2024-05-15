package com.example.dijkstra;


import android.content.Context;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class LocationUtils {

    private String selectedStartLocation, selectedDestinationLocation;

    // 파일에서 선택된 위치의 좌표를 읽어 리스트로 반환합니다.
    public static List<LatLng> getCoordinatesForLocation(Context context, String selectedLocation) {
        List<LatLng> coordinates = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("1층.csv")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                if (row[0].trim().equals(selectedLocation)) {
                    // 각 줄의 좌표 데이터를 읽어옵니다.
                    for (int i = 1; i < row.length; i += 2) {
                        double longitude = Double.parseDouble(row[i].trim());
                        double latitude = Double.parseDouble(row[i + 1].trim());
                        coordinates.add(new LatLng(latitude, longitude));
                    }
                    break; // 일치하는 첫 번째 행을 찾으면 반복 종료
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coordinates;
    }
}

