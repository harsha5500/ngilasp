package GlobalData;

import GeographicInformation.Location;
import java.util.ArrayList;

/**
 * This class holds the locations of the disastes.
 * @see Entity.Disaster
 */
public class DisasterLocations {

    /**
     * The list of locations where the disasters are located.
     */
    public static ArrayList<Location> locations;

    /**
     * Create a empty object
     */
    private DisasterLocations() {
    }

    /**
     * String representation of all the locations where the disaster has taken place.
     * @return string representation of all the locations where the disaster has taken place.
     */
    @Override
    public String toString(){

        String locationsStr = "";

        for(int i = 0;i< locations.size();i++){
            Location location = locations.get(i);
            locationsStr += location.toString();
        }

        return locationsStr;
    }
}
