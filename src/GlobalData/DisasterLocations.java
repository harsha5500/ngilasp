package GlobalData;

import Entity.Disaster;
import GeographicInformation.Location;
import java.util.ArrayList;

/**
 * This class holds the locations of the disastes.
 * @see Entity.Disaster
 */
public class DisasterLocations {

    /**
     * The list of locations where the disasters are located.
     * TODO Change this to private and add a method to extract locations from
     * individual disasters.
     */
    public static ArrayList<Location> locations;
    private ArrayList<Disaster> disasters;

    /**
     * Create a empty object
     */
    private DisasterLocations(ArrayList<Disaster> disasters) {
        this.disasters = disasters;
    }

    /**
     * String representation of all the locations where the disaster has taken place.
     * @return string representation of all the locations where the disaster has taken place.
     */
    @Override
    public String toString() {

        String locationsStr = "";

        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            locationsStr += location.toString();
        }

        return locationsStr;
    }
}
