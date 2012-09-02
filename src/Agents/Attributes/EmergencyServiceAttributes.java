package Agents.Attributes;

import Agents.Person;
import Entity.Hospital;
import GeographicInformation.Cell;
import GeographicInformation.Location;
import java.util.ArrayList;

/**
 * The attributes of the EmergencyService agent attribute. Note that this is a
 * extension of the VehicleAttributes.
 * @see VehicleAttributes
 */
public class EmergencyServiceAttributes extends VehicleAttributes {

    /**
     * The hospital that a EmergencyService (ambulance) is associated with.
     * This value is null for other EmergencyServices
     */
    private Hospital hospital;
    /**
     * This flag indicates whether the ambulance is going towards the hospital or not
     */
    private boolean isGoingToHospital;
    /**
     * The list of passengers on the EmergencyService vehicle (ambulance)
     */
    public ArrayList<Person> passengers = new ArrayList<Person>();

    /**
     * Return the hospital that the EmergencyService agent is associated with
     * @return the hospital that the EmergencyService agent is associated with
     */
    public Hospital getHospital() {
        return hospital;
    }

    /**
     * Set the hospital that the EmergencyService agent is associated with
     * @param hospital the hospital that the EmergencyService agent is associated with
     */
    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    /**
     * Check whether the EmergencyService (ambulance) is going towards the
     * hospital
     * @return true if EmergencyService (ambulance) is going towards the hospital
     */
    public boolean isIsGoingToHospital() {
        return isGoingToHospital;
    }

    /**
     * Set the EmergencyService (ambulance) is going towards the hospital flag
     * @param isGoingToHospital
     */
    public void setIsGoingToHospital(boolean isGoingToHospital) {
        this.isGoingToHospital = isGoingToHospital;
    }

    /**
     * Return the list of passengers in the EmergencyService (ambulance)
     * @return list of passengers in the EmergencyService (ambulance)
     */
    public ArrayList<Person> getPassengers() {
        return passengers;
    }

    /**
     * Set the list of passengers in the EmergencyService (ambulance)
     * @param passengers list of passengers in the EmergencyService (ambulance)
     */
    public void setPassengers(ArrayList<Person> passengers) {
        this.passengers = passengers;
    }

    /**
     * The constructor for the emergency service attributes
     * @param type type of EmergencyService Vehicle
     * @param myCapacity capacity in terms of passengers for the EmergencyService Vehicle
     * @param speed speed of the EmergencyService Vehicle
     * @param home home base of the EmergencyService Vehicle
     * @param currentLocation current location of the EmergencyService Vehicle
     * @param startcellId the start cell of the EmergencyService Vehicle
     * @param homebaseId home base id for the EmergencyService Vehicle's home base
     * @param hospital the hospital that the EmergencyService Vehicle is associated with
     * @see VehicleAttributes
     */
    public EmergencyServiceAttributes(String type, int myCapacity, int speed, Location home, Cell currentLocation, long startcellId, long homebaseId, Hospital hospital) {
        //String type, int capacity, int speed, Location homeBase, Cell currentLocation, ArrayList<Cell> bestRoute) {
        super(type, myCapacity, speed, home, currentLocation, null); //NOTE best route is null
        this.startCellId = startcellId;
        this.homeBaseId = homebaseId;
        this.hospital = hospital;
        this.passengers = new ArrayList<Person>();
    }

    /**
     * The constructor for the emergency service attributes
     * @param type type of EmergencyService Vehicle
     * @param myCapacity capacity in terms of passengers for the EmergencyService Vehicle
     * @param speed speed of the EmergencyService Vehicle
     * @param currentLoad current load of the EmergencyService Vehicle
     * @param home home base of the EmergencyService Vehicle
     * @param currentLocation current location of the EmergencyService Vehicle
     * @param startcellId the start cell of the EmergencyService Vehicle
     * @param homebaseId home base id for the EmergencyService Vehicle's home base
     * @see VehicleAttributes
     */
    public EmergencyServiceAttributes(String type, int myCapacity, int speed, int currentLoad, Location home, Cell currentLocation, long startcellId, long homebaseId) {
        //String type, int capacity, int speed, Location homeBase, Cell currentLocation, ArrayList<Cell> bestRoute) {
        super(type, myCapacity, speed, home, currentLocation, null); //NOTE best route is null
        this.startCellId = startcellId;
        this.homeBaseId = homebaseId;
        this.currentLoad = currentLoad;
        this.passengers = new ArrayList<Person>();
    }

    /**
     * The constructor for the emergency service attributes
     * @param type type of EmergencyService Vehicle
     * @param myCapacity capacity in terms of passengers for the EmergencyService Vehicle
     * @param speed speed of the EmergencyService Vehicle
     * @param home home base of the EmergencyService Vehicle
     * @param currentLocation current location of the EmergencyService Vehicle
     * @param startcellId the start cell of the EmergencyService Vehicle
     * @param homebaseId home base id for the EmergencyService Vehicle's home base
     */
    public EmergencyServiceAttributes(String type, int myCapacity, int speed, Location home, Cell currentLocation, long startcellId, long homebaseId) {
        super(type, myCapacity, speed, home, currentLocation, null); //NOTE best route is null
        this.startCellId = startcellId;
        this.homeBaseId = homebaseId;
        this.passengers = new ArrayList<Person>();
    }

    /**
     * The constructor for the emergency service attributes
     * @param type type of EmergencyService Vehicle
     * @param myCapacity capacity in terms of passengers for the EmergencyService Vehicle
     * @param speed speed of the EmergencyService Vehicle
     * @param currentLoad current load of the EmergencyService Vehicle
     * @param hospital the hospital that the EmergencyService Vehicle is associated with
     */
    public EmergencyServiceAttributes(String type, int myCapacity, int speed, int currentLoad, Hospital hospital) {
        super(type, myCapacity, speed, new Location(hospital.latlon, ""), new Cell(hospital.latlon, true), null); //NOTE best route is null
        this.hospital = hospital;
        this.currentLoad = currentLoad;
        this.passengers = new ArrayList<Person>();
    }
}
