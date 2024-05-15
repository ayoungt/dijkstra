package com.example.dijkstra;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class KMLParser {

    public static class NodeInfo {
        private String name;
        private String coordinates;

        public NodeInfo(String name, String coordinates) {
            this.name = name;
            this.coordinates = coordinates;
        }

        public String getName() {
            return name;
        }

        public String getCoordinates() {
            return coordinates;
        }
    }

    public List<NodeInfo> parseAndFilterKML(InputStream inputStream) {
        List<NodeInfo> filteredNodeList = new ArrayList<>();

        try {
            // XML 파서 초기화
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // KML 파일 파싱
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // Placemark 엘리먼트들 가져오기
            NodeList placemarkList = doc.getElementsByTagName("ns0:Placemark");

            // 모든 Placemark 엘리먼트에 대해 반복
            for (int i = 0; i < placemarkList.getLength(); i++) {
                Node placemarkNode = placemarkList.item(i);
                if (placemarkNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element placemarkElement = (Element) placemarkNode;

                    // Point 엘리먼트 확인
                    NodeList pointList = placemarkElement.getElementsByTagName("ns0:Point");
                    if (pointList.getLength() > 0) {
                        // Point 엘리먼트가 있는 경우 좌표 정보 추출
                        String coordinates = placemarkElement.getElementsByTagName("ns0:coordinates").item(0).getTextContent();

                        // 이름 추출
                        String name = placemarkElement.getElementsByTagName("ns0:name").item(0).getTextContent();

                        // 객체로 만들어서 리스트에 추가
                        filteredNodeList.add(new NodeInfo(name, coordinates));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filteredNodeList;
    }

    public List<String> parseKML(InputStream inputStream) {
        List<String> coordinatesList = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("coordinates")) {
                    eventType = parser.next();
                    coordinatesList.add(parser.getText());
                }
                eventType = parser.next();
            }

            inputStream.close();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return coordinatesList;
    }
}
