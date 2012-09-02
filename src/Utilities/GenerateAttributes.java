/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import Agents.Attributes.PersonAttributes;
import Database.Sql;
import Entity.IdPointPair;
import GeographicInformation.BoundingBox;
import GeographicInformation.Location;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgis.Point;

/**
 *
 * @author jayanth
 */
public class GenerateAttributes {

    /**
     *
     * @return
     */
    public static Point generateCurrentLocation() {

        double northwestLat = 77.574362;
        double northwestLon = 12.998044;
        double southeastLat = 77.634308;
        double southeastLon = 12.958694;

        Point nw = new Point(northwestLat, northwestLon);
        Point se = new Point(southeastLat, southeastLon);

        BoundingBox bb = new BoundingBox(nw, se);

        double diffLat = northwestLat - southeastLat;
        double diffLon = northwestLon - southeastLon;

        double lat = (Math.random() * diffLat + southeastLat);
        double lon = (Math.random() * diffLon + southeastLon);

        Point toReturn = new Point(lat, lon);

        return toReturn;

    }

    /**
     *
     * @return
     */
    public static Location generateHomeBase() {
        double lat = 12.958694, lon = 77.574362;

        Integer latmax = 634308 - 574362;
        Integer lonmax = 998044 - 958694;

        lat = lat + (new Random().nextInt(latmax)) * 0.000001;
        lon = lon + (new Random().nextInt(lonmax)) * 0.000001;

        //Point toReturn = new Point(lat, lon);
        Location loc = new Location(new Point(lon, lat), "HomeBase");
        return loc;
    }

    /**
     * 
     * @return
     */
    public static IdPointPair generateIdPointPairOnRoad() {
        return Sql.getIdPointPairOnRoad();
    }

    /**
     * 
     * @return
     */
    public static Point generatePointOnRoad() {
        return Sql.getPointOnRoad();
    }

    /**
     * 
     * @return
     */
    public static Location generateVehicleHomeBase() {

        Point point = Sql.getPointOnRoad();
        ;
        return new Location(point, "HomeBase");

    }

    /**
     *
     * @return
     */
    public static PersonAttributes generateLifeAttributes() {
        int curiosity;
        if ((new Random().nextInt(13)) % 13 == 0) {
            curiosity = new Random().nextInt(100 - 80) + 80;
        } else {
            curiosity = new Random().nextInt(100 - 80);
        }
        int health;
        if ((new Random().nextInt(3)) % 2 == 0) {
            health = new Random().nextInt(100 - 80);
        } else {
            health = new Random().nextInt(100 - 80) + 80;
        }
        int herfactor = new Random().nextInt(100 - 80) + 80;
        int panic = new Random().nextInt(100 - 80) + 80;
        int trust = new Random().nextInt(100 - 80) + 80;
        PersonAttributes lf = null;
        try {
            lf = new PersonAttributes(curiosity, health, herfactor, panic, trust);
        } catch (InstantiationException ex) {
            Logger.getLogger(GenerateAttributes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lf;
    }

    /**
     *
     * @return
     */
    public static int generateCapacity() {
        int capacity = new Random().nextInt(5) + 1;
        return capacity;
    }

    /**
     *
     * @param capacity
     * @return
     */
    public static int generateLoad(int capacity) {
        int load = new Random().nextInt(capacity);
        return load;
    }

    /**
     *
     * @return
     */
    public static int generateSpeed() {
        int speed = new Random().nextInt(4) + 1;
        return speed;
    }
}
