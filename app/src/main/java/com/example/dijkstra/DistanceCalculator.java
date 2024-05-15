package com.example.dijkstra;
import java.util.ArrayList;
import java.util.List;
public class DistanceCalculator {

    private static final double RADIUS_OF_EARTH_KM = 6371.0; // 지구의 반지름 (km)
    private static final double M_PER_KM = 1000.0; // 1 km = 1000 m

    // 위경도를 라디안으로 변환하는 메서드
    private static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    // 두 위경도 지점 사이의 거리를 구하는 메서드
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceKm = RADIUS_OF_EARTH_KM * c; // 거리 (km)
        double distanceM = distanceKm * M_PER_KM; // 거리 (m)

        return distanceM;
    }
}
