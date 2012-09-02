package GeographicInformation;

import java.io.Serializable;
import org.postgis.Point;

/**
 * A geographical bounding box to represent a rectangular area
 */
public class BoundingBox implements Serializable {
    /**
     * The NothWest extreme point of the bounding area
     */
    public Point nw;
    /**
     * The SouthEast extreme point of the bounding area
     */
    public Point se;
    /**
     * Create the bounding box with the given co-ordinates
     * @param nw north-west co-ordinate
     * @param se south-east co-ordinate
     */
    public BoundingBox(Point nw, Point se) {
        this.nw = new Point(nw.x, nw.y);
        this.se = new Point(se.x, se.y);
    }
    /**
     * Create an empty bounding box without boundries
     */
    public BoundingBox() {
        this.nw = new Point();
        this.se = new Point();
    }
    /**
     * Returns the string representation of the BoundingBox object
     * @return string representation of the BoundingBox object
     */
    @Override
    public String toString() {
        return "nw:" + nw.toString() + " sw:" + nw.toString();
    }
}
