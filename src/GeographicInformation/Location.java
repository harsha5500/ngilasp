package GeographicInformation;

import java.io.Serializable;
import org.postgis.Point;

/**
 * This class defines geographical location and its associated information.
 */
public class Location implements Serializable {

    /**
     * The latitude and logitude vaue for the Location
     */
    protected Point latLon;
    /**
     * A name associated with the location
     */
    protected String name;

    /**
     * Create a location object given the co-ordinates.
     * @param latLon the lat-lon of the location
     * @param name the name of the location.
     */
    public Location(Point latLon, String name) {
        this.latLon = latLon;
        this.name = name;
    }

    /**
     * Create a empty Location
     */
    public Location() {
    }

    /**
     * Create a new location give the latitude and logitude of the location
     * @param lat the latitude of the location
     * @param lon the logitude of the location
     */
    public Location(double lat, double lon) {
        latLon = new Point(lon, lat);
    }

    /**
     * Return the latlon co-ordinates of the location.
     * @return latlon co-ordinates of the location.
     */
    public Point getLatLon() {
        return latLon;
    }

    /**
     * Set the latlon co-ordinates of the location
     * @param latLon latlon co-ordinates of the location
     */
    public void setLatLon(Point latLon) {
        this.latLon = latLon;
    }

    /**
     * This method sets the location as point by passing the latitude and 
     * longitude values as doubles. This avoids passing a Point object and thus 
     * abstracts all subsequent classes from importing the postgis package.
     * @param lat the latitude value
     * @param lon  the longitude value
     */
    public void setLatLon(double lat, double lon) {
        this.latLon = new Point(lat, lon);
    }

    /**
     * Return the name of the location
     * @return name of the location
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the location
     * @param name name of the location
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Check if the locations are equal
     * @param obj location object
     * @return true if the locations are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location test = (Location) obj;
        if (name.equals(test.name) && latLon.equals(test.latLon)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the hashCode for the Location object
     * @return hashcode for the Location object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * String representation of the Location object
     * @return string represntation of the Location object
     */
    @Override
    public String toString() {
        return "name:" + this.name + " latLon:" + this.latLon.toString();
    }
}
