package GeographicInformation;

import Database.Sql;
import java.io.Serializable;
import org.postgis.Point;

/**
 * The entire BoundingBox is divided into fundamental units called Cell. Each 
 * cell associates a unit area in the BoundingBox with a location. The cell
 * also states whether the location is occupied or not.
 */
public class Cell extends Location implements Serializable {

    /**
     * The id of this cell in the database
     */
    long id;

    /**
     * The id of the road to which this cell belongs
     */
    long roadId;

    /**
     * Flag saying wheather the cell belongs to a special road or not, e.g. emergency road, evacuation road
     */
    private boolean special;

   
    /**
     * States whether the cell is occupied
     */
    private boolean occupied;

    /**
     * Create a cell object with a point object and its status
     * @param point the location indicated in the cell
     * @param occupied the status of the cell true means occupied
     * @see Point
     */
    public Cell(Point point, boolean occupied) {
        this.latLon = point;
        this.occupied = occupied;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoadId() {
        return roadId;
    }

    public void setRoadId(long roadId) {
        this.roadId = roadId;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public Cell() {
    }

    


    public Cell(long id, long roadId, boolean occupied) {
        this.id = id;
        this.roadId = roadId;
        this.occupied = occupied;
    }

    public Cell(Point latLon, String name, long id, long roadId, boolean occupied) {
        super(latLon, name);
        this.id = id;
        this.roadId = roadId;
        this.occupied = occupied;
    }



    /**
     * Create a cell object with a location object and its status
     * @param point the location indicated in the cell
     * @param occupied the status of the cell true means occupied
     * @see Location
     */
    public Cell(Location location, boolean occupied) {
        this.setLatLon(location.getLatLon());
        this.occupied = occupied;
    }

    /**
     * Returns the occupancy status of the cell
     * @return true if cell is occupied
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Set the occupancy status of the cell
     * @param occupied the status of occupancy
     */
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean updateOccupiedFlagInDB(boolean occupied){
        return Sql.setOccupiedFlag("lanecells", id, occupied);
    }

    /**
     * String representation of the cell
     * @return string representation of the cell
     */
    @Override
    public String toString() {
        return "CELL: Location: " + this.latLon.toString() + "Occupied: " + isOccupied();
    }

    /**
     * Check whether the given cells are equal
     * NOTE: Cells of the same region created from different algorithms may be
     * flagged different
     * @param obj Cell object
     * @return true if the cells are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Cell other = (Cell) obj;

        if ((this.latLon.equals(other.latLon)) && (this.isOccupied() == other.isOccupied())) {
            return true;
        }
        return false;
    }

    /**
     * Return the hashCode for the Cell object
     * @return hashcode for the Cell object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
