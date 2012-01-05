package Entity;

import java.io.Serializable;
import org.postgis.Point;

/**
 * This class will hold a point from the database and its id in the table.
 */
public class IdPointPair implements Serializable {
    /**
     * The id for the cell associated with the point
     */
    public long id;
    /**
     * The geograpical location
     */
    public Point point;
}
