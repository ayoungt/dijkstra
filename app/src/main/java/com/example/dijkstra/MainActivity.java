package com.example.dijkstra;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.kml.KmlLayer;


import org.apache.commons.csv.CSVParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.example.dijkstra.DijkstraAlgorithm;
import com.google.maps.android.data.kml.KmlLineString;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPoint;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DistanceCalculation";
    private static final String API_KEY = "AIzaSyDIvLg-ud4ghDuyhoz7Tm_oLzUO5Gfu9aA";
    private MapView mapView;

    private Polyline polyline;

    private ImageButton bt_search;
    private ImageButton bt_searchOff;
    private GoogleMap googleMap;
    private Spinner spinner;
    private KmlLayer kmlLayer;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    int currentLevel = -1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private UndirectedGraph graph;
    public String selectedStartLocation; // 선택된 출발지
    public String selectedDestinationLocation; // 선택된 도착지

    private void showMapForFloor(int floor) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.level_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        switch (floor) {
            case 1:
                spinner.setAdapter(adapter);
                spinner.setSelection(adapter.getPosition("F1"));
                showFeaturesForLevel(0); // 레벨에 따른 특징을 보여주는 메서드 호출
                break;
            case 2:
                spinner.setAdapter(adapter);
                spinner.setSelection(adapter.getPosition("F2"));
                showFeaturesForLevel(1);
                break;
            // 필요한 만큼 층에 대한 case를 추가할 수 있습니다.
            case 3:
                spinner.setAdapter(adapter);
                spinner.setSelection(adapter.getPosition("F3"));
                showFeaturesForLevel(2);
                break;
            case 4:
                spinner.setAdapter(adapter);
                spinner.setSelection(adapter.getPosition("F4"));
                showFeaturesForLevel(3);
                break;
            case 5:
                spinner.setAdapter(adapter);
                spinner.setSelection(adapter.getPosition("F5"));
                showFeaturesForLevel(4);
                break;
        }
    }

    public List<List<String>> MarkerFromCSV(Context context, String SpinnerName) {
        List<List<String>> MarkerLocation = new ArrayList<>();
        try {
            // CSV 파일을 읽어오기 위해 CSVReader 클래스 사용
            List<List<String>> csvData = CSVReader.pathCSV(context, "path12345-좌표최종2.csv");

            // selectedStartLocation 변수의 값과 CSV 파일의 1열 또는 2열과 비교하여 일치하는 경우 해당 행의 6번째 열 값을 추가
            for (List<String> row : csvData) {
                if (row.size() >= 2 && (row.get(0).equals(SpinnerName) || row.get(1).equals(SpinnerName))) {
                    // 선택한 시작 위치와 CSV 파일의 1열 또는 2열이 일치하는 경우
                    if (row.size() >= 6) {
                        // 6번째 열 값이 있는 경우 해당 값을 추가
                        MarkerLocation.add(Collections.singletonList(row.get(5)));
                    } else {
                        // 6번째 열 값이 없는 경우 빈 값을 추가
                        MarkerLocation.add(Collections.singletonList(""));
                    }
                    // 선택한 시작 위치와 일치하는 행을 찾았으므로 더 이상 반복할 필요 없음
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 예외 처리 로직 추가
        }
        return MarkerLocation;
    }


    public List<List<String>> retrieveValuesFromCSV(Context context, List<String> edgeNames) {
        List<List<String>> valuesList = new ArrayList<>();
        try {
            // CSV 파일을 읽어오기 위해 CSVReader 클래스 사용
            List<List<String>> csvData = CSVReader.pathCSV(context, "path12345-좌표최종2.csv");

            // edgeNames 리스트를 순회하며 각 값과 CSV 파일의 3번째 열 값을 비교
            for (String edgeName : edgeNames) {
                for (List<String> row : csvData) {
                    // CSV 파일의 3번째 열 값과 edgeName이 일치하는 경우
                    if (row.size() >= 3 && row.get(2).equals(edgeName)) {
                        // 이차원 리스트에 해당 행의 5번째 열 값을 추가
                        if (row.size() >= 5) {
                            valuesList.add(Collections.singletonList(row.get(4)));
                        } else {
                            // 5번째 열 값이 없는 경우 빈 값을 추가
                            valuesList.add(Collections.singletonList(""));
                        }
                        break; // 일치하는 행을 찾았으므로 더 이상 반복할 필요 없음
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 예외 처리 로직 추가
        }
        Log.d("위경도 추가","위경도 추가"+valuesList);
        return valuesList;
    }

    private void readGraphDataFromCSV(Context context, UndirectedGraph graph, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open("path12345-좌표최종2.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length >= 4) {
                    String startNode = columns[0].trim();
                    String endNode = columns[1].trim();
                    String edgeName = columns[2].trim();
                    double length = Double.parseDouble(columns[3].trim());

                    if (startNode != null && endNode != null && edgeName != null) {
                        graph.addEdge(startNode, endNode, edgeName, length);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.level_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 스피너가 초기에 "X"를 선택하도록 설정
        spinner.setSelection(adapter.getPosition("X"));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 5) {
                    // 이전에 선택한 레벨과 현재 선택한 레벨이 다를 때만 실행합니다.
                    int selectedLevel = position + 1;
                    if (selectedLevel != currentLevel) {
                        currentLevel = selectedLevel;
                        showFeaturesForLevel(selectedLevel);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                googleMap.clear();
            }
        });

        Spinner spinnerStartLocation = findViewById(R.id.spinnerStartLocation);
        Spinner spinnerDestinationLocation = findViewById(R.id.spinnerDestinationLocation);
        ImageButton btSearch = findViewById(R.id.bt_search);

        // 출발지 스피너 설정
        ArrayAdapter<CharSequence> adapterStart = ArrayAdapter.createFromResource(this,
                R.array.room_start, android.R.layout.simple_spinner_item);
        adapterStart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartLocation.setAdapter(adapterStart);

        // 도착지 스피너 설정
        ArrayAdapter<CharSequence> adapterDestination = ArrayAdapter.createFromResource(this,
                R.array.room_destination, android.R.layout.simple_spinner_item);
        adapterDestination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestinationLocation.setAdapter(adapterDestination);

        AdapterView.OnItemSelectedListener startItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                selectedStartLocation = selected;
                Log.d("OnItemSelected", "Selected start location: " + selectedStartLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 선택된 것이 없을 때의 동작을 정의합니다.
            }
        };

// 출발지 스피너에 선택 이벤트 핸들러 설정
        spinnerStartLocation.setOnItemSelectedListener(startItemSelectedListener);

// 도착지 스피너의 선택 이벤트 핸들러
        AdapterView.OnItemSelectedListener destinationItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                selectedDestinationLocation = selected;
                Log.d("OnItemSelected", "Selected destination location: " + selectedDestinationLocation);
                MarkerFromCSV(MainActivity.this,selectedDestinationLocation);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 선택된 것이 없을 때의 동작을 정의합니다.
            }
        };

// 도착지 스피너에 선택 이벤트 핸들러 설정
        spinnerDestinationLocation.setOnItemSelectedListener(destinationItemSelectedListener);


        //여기에 다익스트라 넣기.
        ImageButton bt_Search = findViewById(R.id.bt_search);
        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedStartLocation == null || selectedDestinationLocation == null) {
                    Toast.makeText(MainActivity.this, "Please select both start and destination locations", Toast.LENGTH_SHORT).show();
                    return;
                }

                googleMap.clear();

                // 선택된 출발지의 층을 가져옵니다.
                int selectedFloorStart = Integer.parseInt(selectedStartLocation.substring(0, 1));
                int selectedFloorDestination = Integer.parseInt(selectedDestinationLocation.substring(0, 1));

                // 선택된 층에 따라 지도를 보여줍니다.
                showMapForFloor(selectedFloorStart);


                UndirectedGraph graph = new UndirectedGraph();
                readGraphDataFromCSV(MainActivity.this, graph, "path12345-좌표최종2.csv");

                //Log.d("Graph", graph.getGraphInfo());
                // getVertexCount 메서드 호출하여 노드의 수 가져오기

                DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph, MainActivity.this);

                //버튼

                List<String> shortestPath = dijkstra.findShortestPath(selectedStartLocation, selectedDestinationLocation);
                // 최단 경로 출력
                Log.d("ShortestPath", "Shortest Path: " + shortestPath);

                int splitIndex = -1;
                List<String> firstHalf = new ArrayList<>();
                List<String> secondHalf = new ArrayList<>();
                List<Integer> splitIndices = new ArrayList<>(); // 모든 "계단입구"의 인덱스를 저장할 리스트

                for (int i = 0; i < shortestPath.size(); i++) {
                    if (shortestPath.get(i) instanceof String && ((String) shortestPath.get(i)).startsWith("계단입구")) {
                        splitIndices.add(i); // "계단입구"의 인덱스를 리스트에 추가
                    }
                }
                ;

                if (selectedFloorStart != selectedFloorDestination) {

                    // 첫 번째 "계단입구"를 기준으로 배열을 분리
                    int startIndex = 0;
                    int endIndex = splitIndices.get(0);
                    firstHalf = shortestPath.subList(startIndex, endIndex + 1);

// 두 번째 배열은 첫 번째 "계단입구" 다음부터 마지막 요소까지
                    startIndex = endIndex + 1;
                    endIndex = shortestPath.size() - 1;
                    secondHalf = shortestPath.subList(startIndex, endIndex + 1);


                    List<String> FirstEdgeName = dijkstra.GetEdgeName(firstHalf);
                    List<String> SecondEdgeName = dijkstra.GetEdgeName(secondHalf);

                    List<List<String>> FirstvaluesList = retrieveValuesFromCSV(MainActivity.this, FirstEdgeName);
                    List<List<String>> SecondvaluesList = retrieveValuesFromCSV(MainActivity.this, SecondEdgeName);

                    List<List<Double>> FirstconvertedValuesList = new ArrayList<>();
                    List<List<Double>> SecondconvertedValuesList = new ArrayList<>();

                    for (List<String> coordinates : FirstvaluesList) {
                        List<Double> convertedCoordinates = new ArrayList<>();
                        for (String coordinate : coordinates) {
                            String[] coords = coordinate.split(","); // 좌표를 쉼표로 분리
                            if (coords.length == 4) { // 좌표가 위도와 경도 쌍으로 구성된 경우
                                double latitude1 = Double.parseDouble(coords[0]);
                                double longitude1 = Double.parseDouble(coords[1]);
                                double latitude2 = Double.parseDouble(coords[2]);
                                double longitude2 = Double.parseDouble(coords[3]);

                                // double 값으로 변환된 좌표를 리스트에 추가합니다.
                                convertedCoordinates.add(latitude1);
                                convertedCoordinates.add(longitude1);
                                convertedCoordinates.add(latitude2);
                                convertedCoordinates.add(longitude2);

                            }
                            FirstconvertedValuesList.add(convertedCoordinates);
                        }

                    }

                    for (List<String> coordinates : SecondvaluesList) {
                        List<Double> convertedCoordinates = new ArrayList<>();
                        for (String coordinate : coordinates) {
                            String[] coords = coordinate.split(","); // 좌표를 쉼표로 분리
                            if (coords.length == 4) { // 좌표가 위도와 경도 쌍으로 구성된 경우
                                double latitude1 = Double.parseDouble(coords[0]);
                                double longitude1 = Double.parseDouble(coords[1]);
                                double latitude2 = Double.parseDouble(coords[2]);
                                double longitude2 = Double.parseDouble(coords[3]);

                                // double 값으로 변환된 좌표를 리스트에 추가합니다.
                                convertedCoordinates.add(latitude1);
                                convertedCoordinates.add(longitude1);
                                convertedCoordinates.add(latitude2);
                                convertedCoordinates.add(longitude2);

                            }
                            SecondconvertedValuesList.add(convertedCoordinates);
                        }

                    }

                    List<LatLng> Firstpoints = new ArrayList<>();
                    for (List<Double> coordinates : FirstconvertedValuesList) {
                        for (int i = 0; i < coordinates.size(); i += 2) { // 각 좌표쌍마다 2씩 증가
                            double longitude = coordinates.get(i);
                            double latitude = coordinates.get(i + 1);
                            Firstpoints.add(new LatLng(latitude, longitude)); // 위도와 경도를 바꿔서 추가
                        }
                    }

                    List<LatLng> Secondpoints = new ArrayList<>();
                    for (List<Double> coordinates : SecondconvertedValuesList) {
                        for (int i = 0; i < coordinates.size(); i += 2) { // 각 좌표쌍마다 2씩 증가
                            double longitude = coordinates.get(i);
                            double latitude = coordinates.get(i + 1);
                            Secondpoints.add(new LatLng(latitude, longitude)); // 위도와 경도를 바꿔서 추가
                        }
                    }

                    // 원래의 points 리스트를 복사하여 pointsCopy를 생성합니다.
                    List<LatLng> FirstpointsCopy = new ArrayList<>(Firstpoints);
                    List<LatLng> SecondpointsCopy = new ArrayList<>(Secondpoints);


                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            // 사용자가 항목을 선택했을 때 실행되는 코드
                            PolylineOptions polylineOptionsSecond = new PolylineOptions();
                            polylineOptionsSecond.width(10); polylineOptionsSecond .color(Color.BLUE);

                            String selectedItem = (String) parentView.getItemAtPosition(position);
                            selectedItem = selectedItem.substring(1, 2);

                            // 선택된 항목을 가져와서 원하는 동작을 수행
                            if(selectedItem.equals(selectedDestinationLocation.substring(0, 1)))
                            {
                                googleMap.clear();
                                for (int i = 0; i < SecondpointsCopy.size(); i += 2) {
                                    // 현재 좌표와 다음 좌표를 가져와서 PolylineOptions에 추가합니다.
                                    polylineOptionsSecond.add(SecondpointsCopy.get(i));
                                    if (i + 1 < SecondpointsCopy.size()) {
                                        polylineOptionsSecond.add(SecondpointsCopy.get(i + 1));
                                    }
                                    googleMap.addPolyline(polylineOptionsSecond); // 추가된 좌표를 포함한 Polyline을 지도에 표시합니다.
                                    // 추가된 좌표를 제거하여 중복 사용을 방지합니다.
                                    if (i + 1 < SecondpointsCopy.size()) {
                                        polylineOptionsSecond.getPoints().remove(SecondpointsCopy.get(i + 1));
                                    }
                                    polylineOptionsSecond.getPoints().remove(SecondpointsCopy.get(i));
                                }

                            }
                            else if(selectedItem.equals(selectedStartLocation.substring(0, 1)))
                            {
                                googleMap.clear();
                                PolylineOptions polylineOptions = new PolylineOptions();
                                polylineOptions.width(10); polylineOptions .color(Color.BLUE);

                                for (int i = 0; i < FirstpointsCopy.size(); i += 2) {
                                    // 현재 좌표와 다음 좌표를 가져와서 PolylineOptions에 추가합니다.
                                    polylineOptions.add(FirstpointsCopy.get(i));
                                    if (i + 1 < FirstpointsCopy.size()) {
                                        polylineOptions.add(FirstpointsCopy.get(i + 1));
                                    }
                                    googleMap.addPolyline(polylineOptions); // 추가된 좌표를 포함한 Polyline을 지도에 표시합니다.
                                    // 추가된 좌표를 제거하여 중복 사용을 방지합니다.
                                    if (i + 1 < FirstpointsCopy.size()) {
                                        polylineOptions.getPoints().remove(FirstpointsCopy.get(i + 1));
                                    }
                                    polylineOptions.getPoints().remove(FirstpointsCopy.get(i));
                                }

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // 아무 항목도 선택되지 않았을 때 실행되는 코드
                        }
                    });

                }
                else if(selectedFloorStart == selectedFloorDestination)
                {


                List<String> EdgeName = dijkstra.GetEdgeName(shortestPath);
                Log.d("EdgeShortestPath", "EdgeName: " + EdgeName);

                List<List<String>> valuesList = retrieveValuesFromCSV(MainActivity.this, EdgeName);
                Log.d("latlon", "laton: " + valuesList);

                // 새로운 리스트를 생성하여 double 값으로 변환된 좌표를 저장합니다.
                List<List<Double>> convertedValuesList = new ArrayList<>();

// valuesList에 저장된 좌표들을 순회하면서 double 값으로 변환하여 convertedValuesList에 저장합니다.
                for (List<String> coordinates : valuesList) {
                    List<Double> convertedCoordinates = new ArrayList<>();
                    for (String coordinate : coordinates) {
                        String[] coords = coordinate.split(","); // 좌표를 쉼표로 분리
                        if (coords.length == 4) { // 좌표가 위도와 경도 쌍으로 구성된 경우
                            double latitude1 = Double.parseDouble(coords[0]);
                            double longitude1 = Double.parseDouble(coords[1]);
                            double latitude2 = Double.parseDouble(coords[2]);
                            double longitude2 = Double.parseDouble(coords[3]);

                            // double 값으로 변환된 좌표를 리스트에 추가합니다.
                            convertedCoordinates.add(latitude1);
                            convertedCoordinates.add(longitude1);
                            convertedCoordinates.add(latitude2);
                            convertedCoordinates.add(longitude2);
                        }
                    }
                    // 변환된 좌표를 리스트에 추가합니다.
                    convertedValuesList.add(convertedCoordinates);
                }

                Log.d("double", "double: " + convertedValuesList);

                List<LatLng> points = new ArrayList<>();
                for (List<Double> coordinates : convertedValuesList) {
                    for (int i = 0; i < coordinates.size(); i += 2) { // 각 좌표쌍마다 2씩 증가
                        double longitude = coordinates.get(i);
                        double latitude = coordinates.get(i + 1);
                        points.add(new LatLng(latitude, longitude)); // 위도와 경도를 바꿔서 추가
                    }
                }
                // 원래의 points 리스트를 복사하여 pointsCopy를 생성합니다.
                List<LatLng> pointsCopy = new ArrayList<>(points);
                Log.d("polyline","polyline: "+pointsCopy);


                PolylineOptions polylineOptions = new PolylineOptions()
                        .width(10)
                        .color(Color.BLUE);
                // pointsCopy 리스트에 저장된 좌표를 2개씩 PolylineOptions에 추가합니다.
                for (int i = 0; i < pointsCopy.size(); i += 2) {
                    // 현재 좌표와 다음 좌표를 가져와서 PolylineOptions에 추가합니다.
                    polylineOptions.add(pointsCopy.get(i));
                    if (i + 1 < pointsCopy.size()) {
                        polylineOptions.add(pointsCopy.get(i + 1));
                    }
                    googleMap.addPolyline(polylineOptions); // 추가된 좌표를 포함한 Polyline을 지도에 표시합니다.
                    // 추가된 좌표를 제거하여 중복 사용을 방지합니다.
                    if (i + 1 < pointsCopy.size()) {
                        polylineOptions.getPoints().remove(pointsCopy.get(i + 1));
                    }
                    polylineOptions.getPoints().remove(pointsCopy.get(i));
                }
            }
        }
        });


        ImageButton btSearchOff = findViewById(R.id.bt_searchOff);
        btSearchOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bt_searchoff 버튼이 클릭되었을 때 실행되는 코드를 여기에 추가합니다.

                // 폴리라인 제거 예:
               googleMap.clear();

                // 기타 관련된 작업들을 초기화하는 코드를 추가합니다.
            }
        });
    }




  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph = new UndirectedGraph();


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationInit();

        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE); // 초기에는 스피너를 숨깁니다.
        setupSpinner();

        bt_search = findViewById(R.id.bt_search);
        bt_searchOff = findViewById(R.id.bt_searchOff);
        bt_search.setVisibility(View.GONE);
        bt_searchOff.setVisibility(View.GONE);

        //bt_search.setOnClickListener();


        // 노드,노드 사이의 간선 길이를 받아오기 위한 코드
       /* String kmlFilePath = "threepoint.kml"; //kml 파일 경로

        try {
            // AssetManager를 사용하여 KML 파일 로드
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(kmlFilePath);

            // KMLParser 객체 생성
           KMLParser kmlParser = new KMLParser();


            // KML 파일 파싱 및 필터링된 노드 정보 가져오기
            List<KMLParser.NodeInfo> filteredNodeList = kmlParser.parseAndFilterKML(inputStream);


                // 결과 출력
            for (KMLParser.NodeInfo node : filteredNodeList) {
                Log.d(TAG, "Name: " + node.getName());
                Log.d(TAG, "Coordinates: " + node.getCoordinates());
                Log.d(TAG, "");
            }

            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading KML file: " + e.getMessage());
            e.printStackTrace();
        }

        //간선의 위도 경도 먼저 추출하기, 추출 완료.

        // LineStringParser 객체 생성
        LineStringParser parser = new LineStringParser();

        // 파싱할 KML 파일의 경로 지정
        String filePath = "threepoint.kml";

        // AssetManager를 사용하여 파일을 읽어옴
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // LineString 내부의 coordinates를 파싱하여 리스트로 받아오기
        List<String> coordinatesList = parser.parseLineStringCoordinates(inputStream);

        // coordinatesList의 각 요소에서 ",0"과 공백 10칸을 제거하여 새로운 리스트를 생성
        List<String> updatedCoordinatesList = new ArrayList<>();
        for (String coordinates : coordinatesList) {
            updatedCoordinatesList.add(coordinates.replaceAll("\\s+", ",").replace(",0", ""));
        }


// 각 인덱스의 문자열을 double 형식으로 변환하여 리스트에 저장
        List<List<Double>> finalCoordinatesList = new ArrayList<>();
        for (String coordinatesString : updatedCoordinatesList) {
            String[] parts = coordinatesString.split(",");
            List<Double> double_coordinates = new ArrayList<>();

            for (String part : parts) {
                double doubleValue = Double.parseDouble(part);
                double_coordinates.add(doubleValue);
            }

            finalCoordinatesList.add(double_coordinates);
        }


        //double으로 변경된 list 값을 로그로 출력
        for (List<Double> coordinates : finalCoordinatesList) {
            Log.d(TAG, "Coordinates: " + coordinates);
        }



        // 크기가 4인 외부 리스트만 따로 저장한 filteredCoordinatesList 출력
        List<List<Double>> filteredCoordinatesList = new ArrayList<>();
        int calculationCount = 0; // 계산 횟수를 기록하기 위한 카운터 변수


        for (List<Double> coordinates : finalCoordinatesList) {
            if (coordinates.size() == 4 && !filteredCoordinatesList.contains(coordinates)) {
                filteredCoordinatesList.add(coordinates);
                calculationCount++; // 계산 횟수 증가

            }
        }

        for (List<Double> coordinates : filteredCoordinatesList) {
            Log.d(TAG, "Filtered Coordinates(" + calculationCount + ")" + coordinates);
        }


        // 크기가 4보다 큰 외부 리스트만 따로 저장한 largerThanFourCoordinatesList 출력
        List<List<Double>> largerThanFourCoordinatesList = new ArrayList<>();

        for (List<Double> coordinates : finalCoordinatesList) {
            if (coordinates.size() > 4 && !largerThanFourCoordinatesList.contains(coordinates)) {
                largerThanFourCoordinatesList.add(coordinates);
            }
        }

        for (List<Double> coordinates : largerThanFourCoordinatesList) {
            Log.d(TAG, "Larger than Four Coordinates: " + coordinates);
        }


        List<List<Double>> example_result = new ArrayList<>();
        int Count = 0; // 계산 횟수를 기록하기 위한 카운터 변수
// filteredCoordinatesList에 저장된 값으로 경도와 위도 설정
        for (List<Double> coordinates : filteredCoordinatesList) {
            List<Double> exampleCoordinates = new ArrayList<>();
            exampleCoordinates.add(coordinates.get(1)); // 첫 번째 위도
            exampleCoordinates.add(coordinates.get(0)); // 첫 번째 경도
            exampleCoordinates.add(coordinates.get(3)); // 두 번째 위도
            exampleCoordinates.add(coordinates.get(2)); // 두 번째 경도
            example_result.add(exampleCoordinates);
        }

// DistanceCalculator를 사용하여 모든 좌표 쌍의 거리를 계산
        for (List<Double> coords : example_result) {
            double distance = DistanceCalculator.calculateDistance(
                    coords.get(1), coords.get(0), coords.get(3), coords.get(2)
            );
            Count++; // 계산 횟수 증가
            Log.d(TAG, "Distance between the two points(" + Count + "): " + distance + " meters");
        }
*/
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // 맵에 마커 추가
        LatLng markerLocation = new LatLng(37.3752495, 126.6321764);
        googleMap.addMarker(new MarkerOptions().position(markerLocation).title("인천대학교 송도캠퍼스"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerLocation));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 15.0f));

        // Handle zoom level changes
        googleMap.setOnCameraIdleListener(() -> {

            LatLng mapCenter = googleMap.getCameraPosition().target;

            if (googleMap.getCameraPosition().zoom >= 18.5) {
                spinner.setVisibility(View.VISIBLE); // Show spinner when zoom level is high enough
                bt_search.setVisibility(View.VISIBLE);
                bt_searchOff.setVisibility(View.VISIBLE);

                // Show features for the selected level
                int selectedLevel = spinner.getSelectedItemPosition();
                showFeaturesForLevel(selectedLevel);
            } else {
                spinner.setVisibility(View.GONE); // Hide spinner when zoom level is too low
                bt_search.setVisibility(View.GONE);
                bt_searchOff.setVisibility(View.GONE);
                 if (kmlLayer != null) {
                    kmlLayer.removeLayerFromMap();
                }
            }
        });
    }


    private void showFeaturesForLevel(int selectedLevel) {
        try {
            if (kmlLayer != null) {
                kmlLayer.removeLayerFromMap();
            }
            String kmlFilePath = "merged_F" + (selectedLevel + 1) + ".kml";
            kmlLayer = new KmlLayer(googleMap, getResources().getAssets().open(kmlFilePath), getApplicationContext());
            kmlLayer.addLayerToMap();

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }


    private void locationInit() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationRequest();
        getLastLocation();
    }

    private void initLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            handleLocationResult(task.getResult());
                        } else {
                            requestAndUpdateLocation();
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    private void requestAndUpdateLocation() {
        if (checkLocationPermission()) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    handleLocationResult(locationResult.getLastLocation());
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            requestLocationPermission();
        }
    }

    private void handleLocationResult(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            LatLng currentLocation = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        }
    }

    private boolean checkLocationPermission() {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}