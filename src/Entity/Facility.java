package Entity;

import java.io.Serializable;
import org.postgis.Point;


/**
 * This class represents the variois facilities present in the geographic location.
 * Any facility in the area of interest is created by extending this class. The
 * facilities include hospitals, police stations, fire stations, etc.
 */
abstract public class Facility implements Serializable {
    /**
     * The location of the facility
     */
    public Point latlon;
    /**
     * The name of the facility. Note that unique names will be easier to handle.
     */
    public String name;
    /**
     * The address of the facility.
     */
    public String address;

    /**
     * Returns the address of the facility.
     * @return the address of the facility
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the address of the facility.
     * @param address facility address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the Location object of the facility.
     * @return the location of the facility.
     */
    public Point getLatLon() {
        return latlon;
    }

    /**
     * Set the location of the facility.
     * @param latlon
     */
    public void setLocation(Point latlon) {
        this.latlon = latlon;
    }

    /**
     * Returns the name of the facility.
     * @return name name of the facility
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the faciity.
     * @param name the name of the facility.
     */
    public void setName(String name) {
        this.name = name;
    }
}
