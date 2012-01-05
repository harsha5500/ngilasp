package Agents;

import Agents.Attributes.EmergencyServiceAttributes;
import Agents.Behaviour.AmbulanceMoveBehaviour;
import Agents.Behaviour.CompositeBehaviour;
import Agents.Behaviour.FireMoveBehaviour;
import Agents.Behaviour.PoliceMoveBehaviour;
import Database.Sql;
import Entity.Disaster;
import Entity.Hospital;
import GeographicInformation.Cell;
import GeographicInformation.Location;
import GlobalData.Constants;
import GlobalData.SharedData;
import Utilities.AIDGenerator;
import org.postgis.Point;

/**
 * This class defines a emergency service agent. This is essentially a emergency
 * vehicle i.e. police, fire or an ambulance. This agent has the same attributes
 * as that of a Vehicle agents and a few extra paramenters. The agent behaviour
 * will depend on the type of emergency service the agent is rendering
 * @see Vehicle
 * @see AgentAttributes.VehicleAttributes
 * @see AgentAttributes.EmergencyServiceAttributes
 */
public class EmergencyService extends Agent {

    /**
     * EmergencyService agent's attributes
     * @see EmergencyServiceAttributes
     */
    private EmergencyServiceAttributes attributes = null;

    /**
     * Constructor to create an emergency agent
     * @param type the type of emergency agent eg. fire, ambulance, etc
     * @param myCapacity the number of people the agent can accomodate at a given time
     * @param speed the speed with which the agnet moves
     * @param currentLoad the current number of people that the agent may be carrying
     * @param home the location of the agent's home base
     * @param currentloc the current location of the agent
     * @param startcellId the id of the cell in which the agent begins to move
     * @param homebaseId the location of the id of the cell where the home base is located 
     * @throws InstantiationException
     * @see Constants
     */
    public EmergencyService(String type, int myCapacity, int speed, int currentLoad, Location home, Cell currentloc, long startcellId, long homebaseId) throws InstantiationException {
        attributes = new EmergencyServiceAttributes(type, myCapacity, speed, home, currentloc, startcellId, homebaseId);

        AID = AIDGenerator.newID(getClass().toString().substring(6));

        behaviour = new CompositeBehaviour();

        //Depening on type of agent add an appropriate behaviour
        if (type.equals(Constants.VEHICLE_TYPE_AMBULANCE)) {
            behaviour.add(new AmbulanceMoveBehaviour());
        } else if (type.equals(Constants.VEHICLE_TYPE_FIRE)) {
            behaviour.add(new FireMoveBehaviour());
        } else if (type.equals(Constants.VEHICLE_TYPE_POLICE)) {
            behaviour.add(new PoliceMoveBehaviour());
        } else {
            Utilities.Log.logger.info("EmergencyService: Error Unable to determine the Emergency Service Type ");
            System.exit(1);
        }
    }

    /**
     * Constructor to create an emergency agent
     * @param type the type of emergency agent eg. fire, ambulance, etc
     * @param myCapacity the number of people the agent can accomodate at a given time
     * @param speed the speed with which the agnet moves
     * @param currentLoad the current number of people that the agent may be carrying
     * @param home the location of the agent's home base
     * @param currentloc the current location of the agent
     * @param startcellId the id of the cell in which the agent begins to move
     * @param homebaseId the location of the id of the cell where the home base is located 
     * @param hospital the hospital oobject i.e. the hospital to which the ambulance is associated to
     * @throws InstantiationException
     */
    public EmergencyService(String type, int myCapacity, int speed, int currentLoad, Location home, Cell currentloc, long startcellId, long homebaseId, Hospital hospital) throws InstantiationException {
        attributes = new EmergencyServiceAttributes(type, myCapacity, speed, home, currentloc, startcellId, homebaseId, hospital);

        AID = AIDGenerator.newID(getClass().toString().substring(6));

        behaviour = new CompositeBehaviour();

        //Depening on type of agent add an appropriate behaviour
        if (type.equals(Constants.VEHICLE_TYPE_AMBULANCE)) {
            behaviour.add(new AmbulanceMoveBehaviour());
        } else if (type.equals(Constants.VEHICLE_TYPE_FIRE)) {
            behaviour.add(new FireMoveBehaviour());
        } else if (type.equals(Constants.VEHICLE_TYPE_POLICE)) {
            behaviour.add(new PoliceMoveBehaviour());
        } else {
            Utilities.Log.logger.info("EmergencyService: Error Unable to determine the Emergency Service Type ");
            System.exit(1);
        }
    }

    /**
     * Constructor to create an emergency agent
     * @param type the type of emergency agent eg. fire, ambulance, etc
     * @param myCapacity the number of people the agent can accomodate at a given time
     * @param speed the speed with which the agnet moves
     * @param currentLoad the current number of people that the agent may be carrying
     * @param hospital the hospital oobject i.e. the hospital to which the ambulance is associated to
     * @throws InstantiationException
     */
    public EmergencyService(String type, int myCapacity, int speed, int currentLoad, Hospital hospital) throws InstantiationException {
        attributes = new EmergencyServiceAttributes(type, myCapacity, speed, currentLoad, hospital);

        AID = AIDGenerator.newID(getClass().toString().substring(6));

        behaviour = new CompositeBehaviour();

        //Depening on type of agent add an appropriate behaviour
        if (type.equals(Constants.VEHICLE_TYPE_AMBULANCE)) {
            behaviour.add(new AmbulanceMoveBehaviour());
        } else if (type.equals(Constants.VEHICLE_TYPE_FIRE)) {
            behaviour.add(new FireMoveBehaviour());
        } else if (type.equals(Constants.VEHICLE_TYPE_POLICE)) {
            behaviour.add(new PoliceMoveBehaviour());
        } else {
            Utilities.Log.logger.info("EmergencyService: Error Unable to determine the Emergency Service Type ");
            System.exit(1);
        }
    }

    /**
     * This method is called to notify the agent about a disaster
     * @param disaster the disaster that has taken place
     */
    public void notifyDisaster(Disaster disaster) {
        processDisaster(disaster);
    }

    /**
     * This method performs the necessary state changes and processes the given
     * disaster. This methods makes the agent aware of a disaster
     * @param disaster the disaster that has taken place
     */
    private void processDisaster(Disaster disaster) {
        if (attributes.TYPE.equals(Constants.VEHICLE_TYPE_AMBULANCE)) {
            this.pickInjuredAgentFromDisaster();
        } else {
            this.attributes.setHomeBase(new Location(disaster.getLatlon(), ""));
            this.getBestPath(getCurrentLocation(), SharedData.disasters.get(0).getLatlon(), Constants.COST_COLUMN_EMERGENCY_VEHICLE, Constants.TABLE_VEHICLE_CELLS);
        }
    }

    /**
     * Returns the EmergencyService agent attributes
     * @return EmergencyService agent attributes
     */
    public EmergencyServiceAttributes getAttributes() {
        return this.attributes;
    }

    /**
     * Set the EmergencyService agent attributes
     * @param emergencyAttributes EmergencyService agent attributes
     */
    public void setAttributes(EmergencyServiceAttributes emergencyAttributes) {
        this.attributes = emergencyAttributes;
    }

    /**
     * return the current location of the EmergencyService agent
     * @return current location of the EmergencyService agent
     */
    public Point getCurrentLocation() {
        return attributes.currentCellLocation.getLatLon();
    }

    /**
     * The run method of a agent performs the all of the agent behaviours.
     */
    @Override
    public void run() {

        if (SharedData.isDisasterTriggered) { //Is the disaster Triggered can add &&!statusFlag
            if (attributes.TYPE.equals(Constants.VEHICLE_TYPE_AMBULANCE)) { //Is type Ambulancec
                if (this.getCurrentLocation().equals(attributes.getHospital().getLatLon())) { //Is Agent Idle? i.e. is the currentLocation = HOspital?
                    if (attributes.getPassengers().size() > 0) { // There are some passengers in the ambulance, Drop them off
                        dropInjuredAgentAtHospital();
                    } else { //get a new person to go to.
                        pickInjuredAgentFromDisaster();
                    }
                    //behaviour.run(attributes);
                    behaviour.run(attributes);
                } else {
                    //Agent is NOT idle, Check if its home i.e has the ambulance reached the disaster location
                    attributes.isAgentHome = isHomeStatus(); // update the home status
                    if (attributes.isAgentHome) {// Agent has reached home
                        //Swap the locations and get the next best path. NOTE: clear isHomeFlag
                        reverseDirections();
                        attributes.isAgentHome = false;
                    } else {
                        //behaviour.run(attributes);
                        behaviour.run(attributes);
                    }
                }
                statusFlag = true;
            } else { //If the vehicle is not a ambulance type
                if (!statusFlag) { //Check the status flag
                    attributes.isAgentHome = isHomeStatus(); // update the home status
                    if (!attributes.isAgentHome) {
                        //behaviour.run(attributes);
                        behaviour.run(attributes);
                    } else {
                        objectiveFlag = true;
                    }
                    statusFlag = true;//Function call done
                }
            }
        } //if the disaster is not triggered, the agents are not calling theit behaviour methods
        else {
            statusFlag = true; //Function call done
        }
    }

    /**
     * Only the ambulance calls this method. Drops critically injured agents at the respective hospital
     */
    private void dropInjuredAgentAtHospital() {
        for (Person p : attributes.getPassengers()) {
            int index = SharedData.injuredPeopleBeingServed.indexOf(p);
            SharedData.injuredPeopleRescued.add(SharedData.injuredPeopleBeingServed.remove(index));
        }
        attributes.getPassengers().clear();
        setCurrentLocation(attributes.getHospital().getLatLon());
        //NOTE: Do not clear isAgentHome flag
    }

    /**
     * Only the ambulance calls this method. A free ambulance calls this method
     * to puick up a critically injured agent form the disaster locations
     */
    private void pickInjuredAgentFromDisaster() {
        setCurrentLocation(attributes.getHospital().getLatLon()); //Current location is hospital

        if (SharedData.newInjuredPeople.size() != 0) {
            Person passenger = SharedData.newInjuredPeople.remove(0);
            setHomeBase(new Location(passenger.getCurrentCellLocation().getLatLon(), "")); //destination is the person injured.
            attributes.getPassengers().add(passenger); // Add the injured person to the passenger list
            SharedData.injuredPeopleBeingServed.add(passenger); //Remove form newInjured and add it to injuredBeingServed
            this.getBestPath(getCurrentLocation(), attributes.getHomeBase().getLatLon(), Constants.COST_COLUMN_EMERGENCY_VEHICLE, Constants.TABLE_VEHICLE_CELLS);//Calculate the new best path
            attributes.isAgentHome = false; // Clear the home flag
        }
    }

    /**
     * Only the ambulance calls this method. This method is called by the ambulance
     * once it reaches a critically injured person at a disaster location
     */
    private void reverseDirections() {
        setCurrentLocation(attributes.getHomeBase().getLatLon()); // Set current location to injured agent's location
        setHomeBase(new Location(attributes.getHospital().getLatLon(), "")); //set the home lcoation to the hospital's location
        this.getBestPath(getCurrentLocation(), attributes.getHomeBase().getLatLon(), Constants.COST_COLUMN_EMERGENCY_VEHICLE, Constants.TABLE_VEHICLE_CELLS);
        //Update the list of picked up people to be sent to people CTA
        for (Person passenger : attributes.getPassengers()) {
            SharedData.pickedUpinjuredPeople.add(passenger);
        }
        Utilities.Log.logger.info("EmergencyService: Picked up some people: " + SharedData.pickedUpinjuredPeople.size());
    }

    /**
     * Returns the best path between any two given locations
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
     * This method checks if the EmergencyService agent has reached its destination
     * or not
     * @return true when the EmergencyService has reached its destination
     */
    protected boolean isHomeStatus() {
        if (attributes.currentCellIndex < attributes.bestRouteToHome.size() - 1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Sets the EmergencyService agent's current location
     * @param location EmergencyService agent's current location
     */
    public void setCurrentLocation(Point location) {
        attributes.currentCellLocation.setLatLon(location);
    }

    /**
     * Sets the EmergencyService agent's home base
     * @param homeBase EmergencyService agent's home base
     */
    public void setHomeBase(Location homeBase) {
        this.attributes.homeBase = homeBase;
    }

    /**
     * Return the current load of the EmergencyService agent
     * @return the current load on the EmergencyService agent
     */
    public int getCurrentLoad() {
        return this.attributes.currentLoad;
    }

    /**
     * Sets the curent load of the EmergencyService agent
     * @param currentLoad Current load of the EmergencyService agent
     */
    public void setCurrentLoad(int currentLoad) {
        this.attributes.currentLoad = currentLoad;
    }

    /**
     * Check if the EmergencyService agent has reached its destination
     * @return boolean true if the EmergencyService agent has reached its destination
     */
    public boolean isHome() {
        return this.isHome();
    }

    /**
     * Set the home status flag for EmergencyService agent.
     * @param isAgentHome status flag to check if the EmergencyService is home
     */
    public void setIsHome(boolean isHome) {
        this.attributes.setIsHome(isHome);
    }

    /**
     * Return the home base of the EmergencyService agent
     * @return home base of the EmergencyService agent
     */
    public Location getHomeBase() {
        return attributes.homeBase;
    }

    /**
     * Compare two EmergencyService agents, return true if their IDs are equal.
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
     * Return hash code of the EmergencyService agent object
     * @return hash code of the EmergencyService agent object
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Can't create a vehicle without any attributes");
    }
}
