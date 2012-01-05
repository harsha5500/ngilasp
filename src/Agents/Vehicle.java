package Agents;

import Agents.Attributes.VehicleAttributes;
import Agents.Behaviour.CompositeBehaviour;
import Agents.Behaviour.VehicleMoveBehaviour;
import Database.Sql;
import Entity.Disaster;
import GeographicInformation.Cell;
import GeographicInformation.Location;
import GlobalData.Constants;
import Utilities.AIDGenerator;
import java.util.ArrayList;
import org.postgis.Point;

/**
 * Vehicle agent class for the simulation
 * This class decribes the characteristic of a Vehicle Agent.
 * Each vehicle is expected to have a capacity to carry a certain amount of load
 * in terms of people (Person Agents).
 */
public class Vehicle extends Agent {

    /**
     * The attributes of a Vehicle agent.
     * @see VehicleAttributes
     */
    protected VehicleAttributes attributes = null;

    /**
     * Initilizes Vehicle Object
     * @param mycap The capacity of the vehicle.
     * @param curload The current load on the vehicle.
     * @param home The home base of the vehicle.
     * @param curloc The current location of the vehicle.
     * @param spd The current speed with which the vehicle moves.
     * @param type The type of vehicle.
     * @throws InstantiationException Agentattribution initialization error
     */
    public Vehicle(int mycap, int curload, Location home, Cell curloc, int spd, String type, long startcellId, long homebaseId) throws InstantiationException {
        attributes = new VehicleAttributes(type, mycap, spd, home, curloc, new ArrayList<Cell>());
        attributes.currentLoad = curload;
        attributes.CAPACITY = mycap;
        attributes.homeBase = home;
        attributes.currentCellLocation = curloc;
        attributes.SPEED = spd;
        attributes.homeBaseId = homebaseId;
        attributes.startCellId = startcellId;
        AID = AIDGenerator.newID(getClass().toString().substring(6));

        behaviour = new CompositeBehaviour();
        behaviour.add(new VehicleMoveBehaviour());

        getBestPath();
        //THis call has to return true or the vehicle cannot be spwaned.
        //attributes.currentCellLocation.updateOccupiedFlagInDB(true);
    }

    /**
     * Returns the Vehicle's attributes
     * @return Vehicle agent attributes
     */
    public VehicleAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set the Vehicle's attributes
     * @param attributes Vehicle's attributes
     */
    public void setAttributes(VehicleAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     *  Returns the best path between any two given locations
     * @param startPoint start location
     * @param endPoint destination
     * @param costColumn cost set to use to evaluate the best path
     * @param cellsTable the set of cells to use to form the path
     */
    public void getBestPath(Point startPoint, Point endPoint, String costColumn, String cellsTable) {
        this.attributes.currentCellIndex = 0;
        this.attributes.bestRouteToHome = Sql.getBestPathForPerson(startPoint, endPoint, costColumn, cellsTable);

    }

    /**
     * Returns the best path between any two given locations
     * @param startPoint start location
     * @param endPointId destination
     * @param costColumn cost set to use to evaluate the best path
     * @param cellsTable the set of cells to use to form the path
     */
    public void getBestPath(Point startPoint, long endPointId, String costColumn, String cellsTable) {
        this.attributes.currentCellIndex = 0;
        this.attributes.bestRouteToHome = Sql.getBestPathForPerson(startPoint, endPointId, costColumn, cellsTable);

    }

    /**
     * Returns the best path based on the start cell and home base of the agent
     */
    public void getBestPath() {
        this.attributes.currentCellIndex = 0;
        this.attributes.bestRouteToHome = Sql.getBestPathForPerson(attributes.startCellId, attributes.homeBaseId,
                Constants.COST_COLUMN_REGULAR_VEHICLE, Constants.TABLE_VEHICLE_CELLS);
    }

    /**
     * This method performs the necessary state changes and processes the given
     * disaster. This methods makes the agent aware of a disaster
     * @param disaster the disaster that has taken place
     */
    private void processDisaster(Disaster disaster) {
        if (this.attributes.isHome()) {
            return;
        }
        getBestPath(this.attributes.currentCellLocation.getLatLon(),
                this.attributes.homeBaseId, Constants.COST_COLUMN_REGULAR_VEHICLE, Constants.TABLE_VEHICLE_CELLS);
    }

     /**
     * This method is called to notify the agent about a disaster
     * @param disaster the disaster that has taken place
     */
    public void notifyDisaster(Disaster disaster) {
        processDisaster(disaster);
    }

    /**
     * Return the current load of the vehicle.
     * @return the current load on the vehicle.
     */
    public int getCurrentLoad() {
        return this.attributes.currentLoad;
    }

    /**
     * Sets the curent load of the vehicle.
     * @param currentLoad Current load of the vehicle.
     */
    public void setCurrentLoad(int currentLoad) {
        this.attributes.currentLoad = currentLoad;
    }

    /**
     * Check if the vehicle agent is home.
     * @return boolean true if the vehicle agent is home.
     */
    public boolean isHome() {
        return this.isHome();
    }

    /**
     * Set the home status flag for vehicle agent.
     * @param isAgentHome status flag to check if an agent is home
     */
    public void setIsHome(boolean isHome) {
        this.attributes.setIsHome(isHome);
    }

    /**
     * Return the home base of the vehicle.
     * @return home base of the vehicle
     */
    public Location getHomeBase() {
        return attributes.homeBase;
    }

    /**
     * Set the home base of the vehicle.
     * @param homeBase home base of the vehicle.
     */
    public void setHomeBase(Location homeBase) {
        this.attributes.homeBase = homeBase;
    }

    /**
     * String representation of vehicle agent object.
     * @return String represeantation of vehicle
     */
    @Override
    public String toString() {
        return "ID: " + AID + " Class: " + getClass().toString() + " Type: " + attributes.TYPE + " Maximum Capacity: " + attributes.CAPACITY + " Current Load: " + attributes.currentLoad + " Speed: " + attributes.SPEED;
    }

    /**
     * Compare two vehicles, return true if their IDs are equal.
     * @param obj
     * @return true if vehicle ids are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vehicle other = (Vehicle) obj;
        if (this.AID.equals(other.AID)) {
            return true;
        }
        return false;

    }

    /**
     * Return hashCode of the vehicle agent object
     * @return hashCode of the vehicle agent object
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Can't create a vehicle without any attributes");
    }

    /**
     * Check if a vehicle agent is home. Since the speed of the vehicle agents
     * is not constant, the step size is multiplied with the agent's speed
     * before checking if they are home.
     * @return whether the agent is home or not
     */
    protected boolean isHomeStatus() {
        if (attributes.currentCellIndex < attributes.bestRouteToHome.size() - 1) {
            return false;
        } else {
            attributes.currentCellLocation.updateOccupiedFlagInDB(false);
            return true;
        }
    }

    /**
     * The run method. The run method for vehicles will depend on the type of vehicle.
     */
    @Override
    public void run() {
        if (!statusFlag) {
            this.attributes.isAgentHome = isHomeStatus();
            if (!this.attributes.isAgentHome) {
                behaviour.run(attributes);
            } else {
                objectiveFlag = true;
            }
            statusFlag = true;//Function call done

        }
    }

    /**
     * Queries the GIS database for the shortest route
     * NOTE THE FUNCTION IS INCOMPLETE: The location is a laton and
     * the corresponding cell has to be searched.
     * @return current location of the Vehicle
     */
    public Point getCurrentLocation() {
        return attributes.currentCellLocation.getLatLon();
    }

    /**
     * Set the current location of the vehicle agent
     * @param location current location of the Vehicle
     */
    public void setCurrentLocation(Point location) {
        attributes.currentCellLocation.setLatLon(location);
    }
}
