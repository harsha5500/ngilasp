/*
 * Test bed to learn using java properties files.
 */
package Utilities;

import Entity.Disaster;
import Entity.Hospital;
import GeographicInformation.BoundingBox;
import GlobalData.CTANetwork;
import java.util.ArrayList;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

/**
 *
 * @author harsha
 */
public class ConfigParser implements ContentHandler {

    private Locator locator;
    private String value;
    //For Location area
    private BoundingBox boundingBox;
    //private boolean simFlag = false;
    //private boolean northFlag = false;
    //For location in general
    private double lat;
    private double lon;
    //For agent Config
    //private Location tempLocation;
    //forDisasters
    //private boolean disasterFlag = false;
    private Disaster disaster;
    private int intensity;
    private int disTime;
    private ArrayList<Disaster> disasterList;
    //Infrastructure
    private Hospital hospital;
    private ArrayList<Hospital> hospitalList;
    private String tempName;
    private int capacity;
    
    //Agent Configurations
    //private String hostIp;

    @Override
    public void setDocumentLocator(Locator locator) {
        System.out.println("  * setDocumentLocator() called");
        //We save this for latter use if desired.
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        System.out.println("Parsing Begins....");
    }

    @Override
    public void endDocument() throws SAXException {
        System.out.println(".....Parsing Ends");
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        System.out.println("PI: Target:" + target + " and Data:" + data);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        System.out.println("Mapping starts for prefix " + prefix + " mapped to URI " + uri);
    }

    @Override
    public void endPrefixMapping(String prefix) {
        System.out.println("Mapping ends for prefix " + prefix);
    }

    @Override
    /*
     * TODO
     * Include the location name tag in the xml to name the locations.
     */
    public void startElement(String namespaceURI, String localName, String rawName, Attributes atts) throws SAXException {
        System.out.print("startElement: " + localName);
//        if (!namespaceURI.equals("")) {
//            System.out.println(" in namespace " + namespaceURI
//                    + " (" + rawName + ")");
//        } else {
//            System.out.println(" has no associated namespace");
//        }
//        for (int i = 0; i < atts.getLength(); i++) {
//            System.out.println(" Attribute: " + atts.getLocalName(i)
//                    + "=" + atts.getValue(i));
//        }
        //Create Objects here.
        if (localName.equalsIgnoreCase("simulation_area")) {
            //simFlag = true;
            boundingBox = new BoundingBox();
        } else if (localName.equalsIgnoreCase("disasters")) {
            //disasterFlag = true;
            disasterList = new ArrayList<Disaster>();
        } else if (localName.equalsIgnoreCase("infrastructure")) {
            hospitalList = new ArrayList<Hospital>();
            //Other infrastructure can be initilized here.
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String rawName) throws SAXException {
        //Assign Values here.
        System.out.println("endElement: " + localName + "\n");
        if (localName.equalsIgnoreCase("latitude")) {
            lat = Double.parseDouble(value);
        } else if (localName.equalsIgnoreCase("longitude")) {
            lon = Double.parseDouble(value);
        } else if (localName.equalsIgnoreCase("north")) {
            boundingBox.nw.x = lat;
            boundingBox.nw.y = lon;
        } else if (localName.equalsIgnoreCase("east")) {
            boundingBox.se.x = lat;
            boundingBox.se.y = lon;
        } else if (localName.equalsIgnoreCase("time_disaster")) {
            disTime = Integer.parseInt(value);
        } else if (localName.equalsIgnoreCase("intensity")) {
            intensity = Integer.parseInt(value);
        } else if (localName.equalsIgnoreCase("disaster")) {
            disaster = new Disaster(lat, lon, intensity, disTime);
            disasterList.add(disaster);
        } else if (localName.equalsIgnoreCase("name")) {
            tempName = value;
        } else if (localName.equalsIgnoreCase("capacity")) {
            capacity = Integer.parseInt(value);
        } else if (localName.equalsIgnoreCase("hospital")) {
            hospital = new Hospital(lat, lon, tempName, capacity);
            hospitalList.add(hospital);
        } else if (localName.equalsIgnoreCase("ip")){
            CTANetwork.hosts.add(value);
        } else if (localName.equalsIgnoreCase("")){
            
        }
    }

    @Override
    public void characters(char[] ch, int start, int end) throws SAXException {
        value = new String(ch, start, end);
        System.out.println("characters: " + value);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
        String s = new String(ch, start, end);
        System.out.println("ignorableWhitespace: [" + s + "]");

    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        System.out.println("Skipping entity " + name);
    }
}
