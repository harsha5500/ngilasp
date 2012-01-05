package Utilities;

import Agents.Agent;
import Agents.Attributes.PersonAttributes;
import Agents.Person;
import Entity.CustomStyle;
import GeographicInformation.Location;
import com.keithpower.gekmlib.Folder;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Style;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * This is a singleton class. Use getInstance to get a object.
 */
public class KmlUtility implements Serializable {

    //private static KmlUtility kmlUtility = null;
    private Kml kmlDoc = null;
    Document document = null;
    private int counter = 0;
    private int hrInt;
    private int secInt;
    private int minInt;
    private Date date = null;
    HashMap<String, CustomStyle> styleMap;
    private boolean isIconDefault = true;
    private String previousTimeStamp = "";
    private long epochTime = 1262307661;

    public KmlUtility() {
        createKML();
        date = new Date(epochTime);
    }

    public KmlUtility(String url) {
        isIconDefault = false;
        createKML(url);
        date = new Date(epochTime);
    }

    public KmlUtility(HashMap<String, CustomStyle> styleMap) {
        createKML(styleMap);
        date = new Date(epochTime);

    }

    public static void main(String[] args) throws FileNotFoundException {

        KmlUtility kmlUtility = new KmlUtility();

        kmlUtility.OrderDataByAgent("/home/sagar/projects/DisasterManagement/DisasterManagement/kml");

//        System.out.println("Test");
//
//
//        //kmlUtility.test();
//
//
//
//        CustomStyle customStyle1 = new CustomStyle();
//        customStyle1.url = "http://www.uk-sail.org.uk/images/about/car-icon.gif";
//
//        CustomStyle customStyle2 = new CustomStyle();
//        customStyle2.url = "http://www.austinkleon.com/wp-content/uploads/2008/02/stick_figure.gif";
//
//        CustomStyle customStyle3 = new CustomStyle();
//        customStyle3.url = "http://www.istockphoto.com/file_thumbview_approve/478194/2/istockphoto_478194-bus-icon.jpg";
//
//        ArrayList<String> styleNames = new ArrayList<String>();
//        styleNames.add("one");
//        styleNames.add("two");
//        styleNames.add("three");
//        HashMap<String, CustomStyle> styleMap = new HashMap<String, CustomStyle>();
//
//        styleMap.put(styleNames.get(0), customStyle1);
//        styleMap.put(styleNames.get(1), customStyle2);
//        styleMap.put(styleNames.get(2), customStyle3);
//
//
//        KmlUtility kmlUtility = getInstanse(styleMap);
//        //KmlUtility kmlUtility = getInstanse();
//
//        ArrayList<Location> locations = null;
//
//
//        double inittal_diff = 0.0001;
//        double diff = 0.0001;
//
//        locations = kmlUtility.createPoints(10, inittal_diff);
//        inittal_diff = inittal_diff + diff;
//        for (int i = 0; i < 10; i++) {
//            ArrayList<Location> location = new ArrayList<Location>();
//            location.add(locations.get(i));
//            kmlUtility.addPlacemarks(location, i);
//        }
//
//
//        kmlUtility.kmlDoc.marshal(new File("op.kml"));

    }

    public boolean writeFile(String filename) {
        try {

            if (kmlDoc == null) {
                System.out.println("kmlDoc null re baba");
            }
            this.kmlDoc.marshal(new File(filename));
            Utilities.Log.logger.info("Wrote KML to file " + filename);
            return true;
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(KmlUtility.class.getName()).log();
            Utilities.Log.logger.info(ex.getMessage());
            return false;
        }

    }

    public void printLocations(ArrayList<Location> locations) {


        for (int i = 0; i < locations.size(); i++) {

            Location location = locations.get(i);
            // System.out.println(location.getName() + " " + location.getLatLon().lat + " " + location.getLon());

        }
    }

    public static KmlUtility getInstanse(HashMap<String, CustomStyle> styleMap) {

        return new KmlUtility(styleMap);
    }

    public static KmlUtility getInstanse(String url) {


        return new KmlUtility(url);
    }

    public static KmlUtility getInstanse() {

        return new KmlUtility();
    }

    private ArrayList<Location> createPoints(int num, double diff) {

        double lat = 13.0;
        double lon = 77.0;

        lat = lat + diff;
        lon = lon - diff;

        ArrayList<Location> points = new ArrayList<Location>();

        for (int i = 0; i < num; i++) {


            lat = lat + 0.0001;
            lon = lon + 0.0001;
            org.postgis.Point point = new org.postgis.Point(lon, lat);


            Location location = new Location(point, "");

            points.add(location);
        }

        return points;
    }

    private void createKML() {

        // Start with a blank Kml object, give it a URL
        kmlDoc = new Kml();
        document = kmlDoc.createAndSetDocument();

    }

    private void createKML(HashMap<String, CustomStyle> styleMap) {

        // Start with a blank Kml object, give it a URL
        kmlDoc = new Kml();
        document = kmlDoc.createAndSetDocument();
        this.addCustomStyles(styleMap);
    }

    private void createKML(String url) {

        // Start with a blank Kml object, give it a URL
        kmlDoc = new Kml();
        document = kmlDoc.createAndSetDocument();


        Style style = document.createAndAddStyle().withId("customIcon");

        final IconStyle iconstyle = style.createAndSetIconStyle().withColor("ff00ff00").withColorMode(ColorMode.RANDOM).withScale(1.1d);

        iconstyle.createAndSetIcon().withHref(url);


    }

    public void addPlacemarks(ArrayList<Location> latLons, int tick) {
        String startTime = getTimeFromTicks(tick);
        String endTime = getTimeFromTicks(tick + 1);

        for (int i = 0; i < latLons.size(); i++) {
            Placemark placemark = new Placemark();

            placemark.createAndSetTimeStamp().setWhen(startTime);
            //placemark.createAndSetTimeSpan().withBegin(startTime).withEnd(endTime);

            Location location = latLons.get(i);

            Point point = new Point();
            point.addToCoordinates(location.getLatLon().x, location.getLatLon().y);
            placemark.setGeometry(point);
            document.addToFeature(placemark);
        }
    }

    public void addPlacemarks(ArrayList<Location> latLons) {

        Placemark placemark;

        if (isIconDefault) {
            placemark = new Placemark();
        } else {
            placemark = new Placemark().withStyleUrl("#customIcon");
        }

        placemark.createAndSetTimeSpan().withBegin(getTime()).withEnd(getTime());


        MultiGeometry multiGeometry = new MultiGeometry();

        for (int i = 0; i < latLons.size(); i++) {
            Location location = latLons.get(i);

            Point point = new Point();
            point.addToCoordinates(location.getLatLon().x, location.getLatLon().y);
            multiGeometry.addToGeometry(point);
            placemark.setGeometry(multiGeometry);


        }

        document.addToFeature(placemark);

    }

    public void addPlacemarks(ArrayList<Location> latLons, List<Agent> agents, String agentType, ArrayList<String> styleNames, int tick) {

        System.out.println("size :" + latLons.size() + " " + agents.size() + " " + styleNames.size());
        
        String startTime = getTimeFromTicks(tick);
        String endTime = getTimeFromTicks(tick + 1);

        for (int i = 0; i < agents.size(); i++) {

            Agent agent = agents.get(i);

            Placemark placemark = new Placemark().withStyleUrl("#" + styleNames.get(i));

            placemark.createAndSetTimeStamp().withWhen(startTime);
            //placemark.createAndSetTimeSpan().withBegin(startTime).withEnd(endTime);

            Location location = latLons.get(i);

            Point point = new Point();
            point.addToCoordinates(location.getLatLon().x, location.getLatLon().y);
            placemark.setGeometry(point);

            ExtendedData extendedData = new ExtendedData();
            placemark.setExtendedData(extendedData);

            Data idData = new Data(agent.getAID());
            extendedData.getData().add(idData);
            idData.setName("id");

            Data idType = new Data(agentType);
            extendedData.getData().add(idType);
            idType.setName("agentType");

            if (agentType.equals("person")) {

                Person person = (Person) agent;

                Data idLife = new Data(person.getAttributes().HEALTH + "");
                extendedData.getData().add(idLife);
                idType.setName("health");
            }

            document.addToFeature(placemark);
        }
    }

    public void addPlacemarks(ArrayList<Location> latLons, ArrayList<String> styleNames, int tick) {


        String startTime = getTimeFromTicks(tick);
        String endTime = getTimeFromTicks(tick + 1);

        for (int i = 0; i < latLons.size(); i++) {
            Placemark placemark = new Placemark().withStyleUrl("#" + styleNames.get(i));

            placemark.createAndSetTimeStamp().setWhen(startTime);
            placemark.createAndSetTimeSpan().withBegin(startTime).withEnd(endTime);

            Location location = latLons.get(i);

            Point point = new Point();
            point.addToCoordinates(location.getLatLon().x, location.getLatLon().y);
            placemark.setGeometry(point);
            document.addToFeature(placemark);
        }
    }

    public String getTimeFromTicks(int tick) {

        Date date = new Date(tick * 1000);

        String year = date.getYear() + 1900 + "";

        String month = (date.getMonth() + 1) + "";
        if (month.length() < 2) {
            month = "0" + month;
        }

        String day = date.getDate() + "";
        if (day.length() < 2) {
            day = "0" + day;
        }

        String hour = date.getHours() + "";
        if (hour.length() < 2) {
            hour = "0" + hour;
        }

        String min = date.getMinutes() + "";
        if (min.length() < 2) {
            min = "0" + min;
        }

        String sec = date.getSeconds() + "";
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        String timeStampStr = year + "-" + month + "-" + day + "T" + hour + ":" + min + ":" + sec + "Z";

        return timeStampStr;

    }

    public void addPlacemarks(ArrayList<Location> latLons, ArrayList<String> styleNames) {


        String startTime = getTime();
        String endTime = getTime();

        for (int i = 0; i < latLons.size(); i++) {
            Placemark placemark = new Placemark().withStyleUrl("#" + styleNames.get(i));

            placemark.createAndSetTimeStamp().setWhen(startTime);
            //placemark.createAndSetTimeSpan().withBegin(startTime).withEnd(endTime);

            Location location = latLons.get(i);

            Point point = new Point();
            point.addToCoordinates(location.getLatLon().x, location.getLatLon().y);
            placemark.setGeometry(point);
            document.addToFeature(placemark);
        }
    }

    public void addPlacemarks(ArrayList<Location> latLons, String styleName) {

        Placemark placemark = new Placemark().withStyleUrl("#" + styleName);

        placemark.createAndSetTimeSpan().withBegin(getTime()).withEnd(getTime());



        MultiGeometry multiGeometry = new MultiGeometry();

        for (int i = 0; i < latLons.size(); i++) {
            Location location = latLons.get(i);
            Point point = new Point();
            point.addToCoordinates(location.getLatLon().x, location.getLatLon().y);
            multiGeometry.addToGeometry(point);
            placemark.setGeometry(multiGeometry);


        }

        document.addToFeature(placemark);
    }

    private void addCustomStyles(HashMap<String, CustomStyle> styleMap) {

        Set keySet = styleMap.keySet();

        Iterator iter = keySet.iterator();

        while (iter.hasNext()) {
            String key = (String) iter.next();
            CustomStyle customStyle = styleMap.get(key);

            Style style = document.createAndAddStyle().withId(key);

            final IconStyle iconstyle = style.createAndSetIconStyle().withScale(1.1d).withColor(customStyle.color);

            iconstyle.createAndSetIcon().withHref(customStyle.url);
        }
    }

    public void OrderDataByAgent(String dir) {


        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File pathname) {

                if (pathname.isFile()) {

                    return true;
                } else {
                    return false;
                }
            }
        };

        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles(fileFilter);

        for (int i = 0; i < listOfFiles.length; i++) {

            String fileName = listOfFiles[i].getPath();
            System.out.println(fileName);
            readKML(fileName);
        }

    }

    public void readKML(String filename) {
//        Kml unmarshal = Kml.unmarshal(new File(filename));
//        Document document = (Document) unmarshal.getFeature();
//
//
//
//        List<Feature> featureList = document.getFeature();
//
//        for (int i = 0; i < featureList.size(); i++) {
//            Feature feature = featureList.get(i);
//
//            System.out.println(feature.getDescription());
//        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(filename);
            org.w3c.dom.Document document = (org.w3c.dom.Document) builder.parse(file);
            Element root = document.getDocumentElement();
            nodeDetails(document);

            NodeList children = root.getChildNodes();


            NodeList placemarkList = root.getElementsByTagName("Placemark");

            //System.out.println(":)" + placemarkList.getLength());

            for (int i = 0; i < placemarkList.getLength(); i++) {
                Node placemark = (Node) placemarkList.item(i);
                //System.out.println("placemark name " + placemark.getNodeName());
                readPlaceamark(placemark);
            }


        } catch (Exception e) {

            System.out.println(e.getMessage());
        }
    }

    private void readPlaceamark(Node placemark) {

        NodeList placemarkChildNodes = placemark.getChildNodes();

        for(int i =0; i < placemarkChildNodes.getLength();i++){
            Node placemarkChild = placemarkChildNodes.item(i);

            if(placemarkChild.getNodeType() == 3){
                continue;
            } else if(placemarkChild.getNodeType() == 1){
                if("TimeSpan".equals(placemarkChild.getNodeName())){

                    getStartTime(placemarkChild);
                        
                    

                }else if("ExtendedData".equals(placemarkChild.getNodeName())){
                    getExtendedData(placemarkChild);
                }else if("Point".equals(placemarkChild.getNodeName())){

                }else if("styleUrl".equals(placemarkChild.getNodeName())){

                }
            }

            //nodeDetails(placemarkChildNodes.item(i));
        }


    }


    public ArrayList<Node> getNodesWithType(NodeList nodeList, int type){

        ArrayList<Node> returnNodeList = new ArrayList<Node>();

        for(int i=0;i<nodeList.getLength();i++){

            Node node = nodeList.item(i);
            
            if(node.getNodeType() == type){
                //System.out.println(node.getNodeName() + " node type " + node.getNodeType());
                returnNodeList.add(node);
            }
        }

        return returnNodeList;
    }

        private void getExtendedData(Node extendedData){
                    NodeList typeNodes = extendedData.getChildNodes();

                    ArrayList<Node> nodeList = getNodesWithType(typeNodes, 1);

        for(int i =0;i< nodeList.size();i++){

            Node dataNode = nodeList.get(i);
            NamedNodeMap attributes = dataNode.getAttributes();

            for(int j = 0; j < attributes.getLength(); j++){
                Node node = attributes.item(j);
                System.out.println("-" + node.getNodeName() + " " + node.getNodeValue());
            }

                              NodeList idChildNodes =  dataNode.getChildNodes();
                  ArrayList<Node> idValidChildNodes = getNodesWithType(idChildNodes, 1);
                  
                  Node dataValue = idValidChildNodes.get(0);
                  System.out.println("------" + dataValue.getNodeName() + " " + dataValue.getTextContent());
  
        }
        }

    private void getStartTime(Node timeStamp){

        NodeList timeNodes = timeStamp.getChildNodes();
        String startDate = null;
        for(int i =0;i< timeNodes.getLength();i++){

            Node timeNode = timeNodes.item(i);
            if(timeNode.getNodeType() == 1){
                if("begin".equals(timeNode.getNodeName())){
                    //System.out.println("Begin " + timeNode.getTextContent());
                    startDate = timeNode.getTextContent();
                }
            }
        }

        


        //System.out.println("getStartTime " + node.getNodeType());
    }
    
    private void nodeDetails(Node node) {
        System.out.println("Node Type:"
                + node.getNodeType() + "nNode Name:"
                + node.getNodeName());
        if (node.hasChildNodes()) {
//            System.out.println("Child Node Type:"
//                    + node.getFirstChild().getNodeType()
//                    + " nNode Name:"
//                    + node.getFirstChild().getNodeName());
        }
    }

    private String getPreviousTime() {
        if (previousTimeStamp.equals("")) {
            previousTimeStamp = getTime();
        }

        return previousTimeStamp;
    }

    private String getTime() {


        String timeStampStr = "";
        counter += 1;
        secInt = counter;

        if (secInt > 59) {
            secInt = 0;
            counter = 0;
            minInt = minInt + 1;
        }

        if (minInt > 59) {
            minInt = 0;
            hrInt = hrInt + 1;
        }

        if (hrInt > 23) {
            hrInt = 0;
        }

        String year = date.getYear() + 1900 + "";

        String month = (date.getMonth() + 1) + "";
        if (month.length() < 2) {
            month = "0" + month;
        }

        String day = date.getDate() + "";
        if (day.length() < 2) {
            day = "0" + day;
        }

        String hour = hrInt + "";
        if (hour.length() < 2) {
            hour = "0" + hour;
        }

        String min = minInt + "";
        if (min.length() < 2) {
            min = "0" + min;
        }

        String sec = secInt + "";
        if (sec.length() < 2) {
            sec = "0" + sec;
        }

        timeStampStr = year + "-" + month + "-" + day + "T" + hour + ":" + min + ":" + sec + "Z";

        previousTimeStamp = timeStampStr;

        return timeStampStr;


    }

}
