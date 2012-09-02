package Agents.Attributes;

import GeographicInformation.Cell;
import GeographicInformation.Location;
import java.util.ArrayList;
import org.postgis.Point;

/**
 * The vehicle agent attributes
 */
public class VehicleAttributes extends AgentAttributes {

    //TODO List of persons riding in the vehicle?
    /**
     * The current amount of load the vehicle is carrying.
     */
    public int currentLoad = 0;
    /**
     * The type of vehicle. Ex. Ambulance, Fire Engine, etc,.
     */
    public final String TYPE;
    /**
     * The number of persons that the vehicle can carry at a time.
     */
    public int CAPACITY;
    /**
     *The Speed of the vehicle in terms of cells per tick message.
     */
    public int SPEED;
    /**
     *Agent's home
     */
    public Location homeBase;
    /**
     * the primary key of the point (vertex) in the database
     */
    public long homeBaseId;
    /**
     * The flag telling wheather a agent should move or stay where it is
     */
    public boolean shouldMove = true;
    /**The shortest and safest route home in case of a vehicle is a set of cells
     * and not just latlon values
     */
    public ArrayList<Cell> bestRouteToHome;
    /**
     * The cell in which the Vehicle agent is currently situated in.
     */
    public Cell currentCellLocation;
    /**
     * the primary key of the point (vertex) in the database
     */
    public long startCellId;
    /**
     * Stores the index of the current cell in the best path array list.
     */
    public int currentCellIndex;
    /**
     * Flag to indicate if the agent has reached home.
     */
    public boolean isAgentHome = false;

    /**
     * Initilize the vehicle attributes.
     * @param type 
     * @param capacity capacity of the vehicle
     * @param speed speed of the vehicle
     * @param homeBase the home base of the vehicle agents
     * @param currentLocation the current location of the agent
     * @param bestRoute
     */
    public VehicleAttributes(String type, int capacity, int speed, Location homeBase, Cell currentLocation, ArrayList<Cell> bestRoute) {
        this.CAPACITY = capacity;
        this.SPEED = speed;
        this.homeBase = homeBase;
        this.currentCellLocation = currentLocation;
        this.bestRouteToHome = bestRoute;
        this.TYPE = type;
    }

    /**
     * Initilize minimal vehicle attributes
     * @param type the type of vehicle
     */
    public VehicleAttributes(String type) {
        this.TYPE = type;
    }

    /**
     * Check if the vehicle agent is home
     * @return true if the vehicle agent has reached its destination
     */
    public boolean isHome() {
        return isAgentHome;
    }

    /**
     * Sets the flag to state that the vehicle has reached home
     * @param isHome true if the vehicle has reached its destination
     */
    public void setIsHome(boolean isHome) {
        this.isAgentHome = isHome;
    }

    /**
     * Return the current load in terms of Person agents travelling in the vehicle
     * @return the number of passengers that the vehicle is carrying
     */
    public int getCurrentLoad() {
        return currentLoad;
    }

    /**
     * Set the current load in terms of Person agents travelling in the vehicle
     * @param currentLoad the number of passengers that the vehicle is carrying
     */
    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }

    /**
     * Return the type of vehicle
     * @return the type of vehicle
     */
    public String getTYPE() {
        return TYPE;
    }

    /**
     * Returns the current cell in which the vehicle agent is located.
     * @return the cell in which the agent is located.
     */
    public Cell getCurrentCellLocation() {
        return currentCellLocation;
    }

    /**
     * Set the current cell in which the vehicle agent is located.
     * @param currentCellLocation current cell in which the vehicle agent is located.
     */
    public void setCurrentCellLocation(Cell currentCellLocation) {
        this.currentCellLocation = currentCellLocation;
    }

    /**
     * Get the capacity of the vehicle
     * @return the capacity of the vehicle
     */
    public int getCapacity() {
        return this.CAPACITY;
    }

    /**
     * Set the capacity of the vehicle
     * @param capacity the capacity of the vehicle
     */
    public void setCapacity(int capacity) {
        this.CAPACITY = capacity;
    }

    /**
     * Return the speed of the vehicle.
     * @return speed speed of the vehicle.
     */
    public int getSpeed() {
        return SPEED;
    }

    /**
     * Set the speed of the vehicle.
     * @param SPEED 
     */
    public void setSpeed(int SPEED) {
        this.SPEED = SPEED;
    }

    /**
     * Get the ArrayList of cells which represents the shortest and the safest
     * route to follow the reach the agent's home base.
     * @return ArrayList of cells which represents the shortest and the safest path
     */
    public ArrayList<Cell> getBestRouteToHome() {
        return bestRouteToHome;
    }

    /**
     * Set the ArrayList of cells which represents the shortest and the safest
     * route to follow the reach the agent's home base.
     * @param bestRouteToHome ArrayList of cells which represents the shortest and the safest path
     */
    public void setBestRouteToHome(ArrayList<Cell> bestRouteToHome) {
        this.bestRouteToHome = bestRouteToHome;
    }

    /**
     * Get the home base for the vehicle agent.
     * @return home base 
     */
    public Location getHomeBase() {
        return homeBase;
    }

    /**
     * Set the home base of the agent
     * @param homeBase set the base
     */
    public void setHomeBase(Location homeBase) {
        this.homeBase = homeBase;
    }

    /**
     * Return true if the attributes are equal.
     * @param obj vehicle attribute object
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VehicleAttributes other = (VehicleAttributes) obj;
        if (this.CAPACITY != other.CAPACITY) {
            return false;
        }
        if (this.SPEED != other.SPEED) {
            return false;
        }
        if (this.homeBase != other.homeBase && (this.homeBase == null || !this.homeBase.equals(other.homeBase))) {
            return false;
        }
        if (this.bestRouteToHome != other.bestRouteToHome && (this.bestRouteToHome == null || !this.bestRouteToHome.equals(other.bestRouteToHome))) {
            return false;
        }
        if (this.currentCellLocation != other.currentCellLocation && (this.currentCellLocation == null || !this.currentCellLocation.equals(other.currentCellLocation))) {
            return false;
        }
        return true;
    }

    /**
     * Return hash code of the vehicle attributes object
     * @return hash code of the vehicle attributes object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Debug function to print the best path home.
     */
    public void printBestpathToHome() {

        for (int i = 0; i < bestRouteToHome.size(); i++) {
            Point point = bestRouteToHome.get(i).getLatLon();
            System.out.println(point.y + "," + point.x);
        }
    }

    /**
     * String representation of the vehicle attributes.
     * @return string containig the vehicle attributes.
     */
    @Override
    public String toString() {
        return "TYPE: " + TYPE + "CAPACITY: " + CAPACITY + " SPEED: " + SPEED + " Home: "
                + homeBase.toString() + "Current Location: " + currentCellLocation.toString() + "Current Load: " + currentLoad + "Is Home? " + isHome();
    }
}
