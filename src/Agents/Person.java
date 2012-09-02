package Agents;

import Agents.Attributes.*;
import Agents.Behaviour.CompositeBehaviour;
import Agents.Behaviour.PersonMoveOnFoothpathBehaviour;
import Database.Sql;
import Entity.Disaster;
import GeographicInformation.Cell;
import GeographicInformation.Location;
import GlobalData.Constants;
import GlobalData.SharedData;
import Utilities.AIDGenerator;
import java.util.ArrayList;
import org.postgis.Point;

/**
 * Person agent class for the simulation
 * This class decribes the characteristic of a Person Agent.
 * @author flummoxed
 */
public class Person extends Agent {

    /**
     * Flag to indicate if the agent has reached home.
     */
    public boolean isHome = false;
    /**
     * The attributes of Person Agent.
     * @see PersonAttributes
     */
    private PersonAttributes attributes;

    /**
     * Initializes Person object
     * @param initattribs initial person attributes
     * @param currentCellLocation 
     * @param initbase home base for the agent
     * @param homebaseId 
     * @param startcellId 
     * @throws InstantiationException Agentattribution initialization error
     */

    
    public Person(PersonAttributes initattribs, Cell currentCellLocation, Location initbase, long startcellId, long homebaseId) throws InstantiationException {

        try {
            attributes = new PersonAttributes(initattribs.getCuriosity(), initattribs.getHealth(), initattribs.getHerdfactor(), initattribs.getPanic(), initattribs.getTrust());
            initattribs = null;
        } catch (InstantiationException instantiationException) {
            throw new InstantiationException("Unable to initialize AgentAttributes for Person : " + instantiationException.getMessage());
        }
        this.attributes.currentCellLocation = currentCellLocation;

        this.attributes.homeBase = new Location(initbase.getLatLon(), initbase.getName());
        initbase = null;
        statusFlag = false;
        objectiveFlag = false;
        attributes.homeBaseId = homebaseId;
        attributes.startCellId = startcellId;

        AID = AIDGenerator.newID(getClass().toString().substring(6));

        behaviour = new CompositeBehaviour();
        behaviour.add(new PersonMoveOnFoothpathBehaviour());

        getBestPath();

    }

    /**
     * 
     * @return Returns the Person's attributes
     */
    public PersonAttributes getAttributes() {
        return attributes;
    }

    

    /**
     * 
     */
    public void getBestPath() {

        this.attributes.currentCellIndex = 0;
        this.attributes.bestRouteToHome = Sql.getBestPathForPerson(attributes.startCellId, attributes.homeBaseId, Constants.COST_COLUMN_LENGTH_BASED, Constants.TABLE_PEOPLE_CELLS);

    }

    /**
     * 
     * @param startPoint
     * @param endPoint
     * @param costColumn
     * @param cellTable
     * @return
     */
    public ArrayList<Cell> getBestPath(Point startPoint, Point endPoint, String costColumn, String cellTable) {

        this.attributes.currentCellIndex = 0;
        return Sql.getBestPathForPerson(startPoint, endPoint, costColumn, cellTable);

    }

    /**
     * 
     * @param startPoint
     * @param endPoinId
     * @param costColumn
     * @param cellTable
     * @return
     */
    public ArrayList<Cell> getBestPath(Point startPoint, long endPoinId, String costColumn, String cellTable) {

        this.attributes.currentCellIndex = 0;
        return Sql.getBestPathForPerson(startPoint, endPoinId, costColumn, cellTable);

    }

    /**
     * 
     * @param disaster
     */
    public void notifyDisaster(Disaster disaster) {

        processDisaster(disaster);
    }

    private void processDisaster(Disaster disaster) {
        if (this.attributes.HEALTH < attributes.CRITICAL_HEALTH) {
            this.attributes.shouldMove = false;
            return;
        }

        if (this.objectiveFlag) {
            return;
        }

        if (attributes.CURIOSITY > 60) {
            attributes.bestRouteToHome = getBestPath(attributes.currentCellLocation.getLatLon(), disaster.getLatlon(),
                    Constants.COST_COLUMN_REGULAR_VEHICLE, Constants.TABLE_PEOPLE_CELLS);
            System.out.println("\nMy best path has changed\n");
        } else {
            attributes.bestRouteToHome = getBestPath(attributes.currentCellLocation.getLatLon(), attributes.homeBaseId,
                    Constants.COST_COLUMN_REGULAR_VEHICLE, Constants.TABLE_PEOPLE_CELLS);
        }

    }

    /**
     * Person Agent's  attribute object.
     * @return the different attributes for of a person agent.
     */
    public PersonAttributes getMyattributes() {
        return attributes;
    }

    /**
     * Person Agent attributes are set here.
     * @param myattributes The attributes of a Person Agent.
     * @see PersonAttributes
     * @see AgentAttributes
     */
    public void setMyattributes(PersonAttributes myattributes) {
        this.attributes = myattributes;
    }

    /**
     * Get current location of the agent.
     * @return the current location as Point.
     * @see Point
     */
    public Cell getCurrentCellLocation() {
        return attributes.currentCellLocation;
    }

    /**
     * Set agent's currentLocation
     * @param mycurrentlocation The agent's current location as Point object.
     * @see Point
     */
    public void setMycurrentlocation(Point mycurrentlocation) {
        attributes.currentLocation = mycurrentlocation;
    }

    /**
     * Return the agent's home base.
     * @return The home base of the agent as a Location object.
     */
    public Location getMyhomebase() {
        return attributes.homeBase;
    }

    /**
     * Set the agent's home base.
     * @param myhomebase The agent's home base as a Location object.
     * @see Location
     */
    public void setMyhomebase(Location myhomebase) {
        attributes.homeBase = myhomebase;
    }

    /**
     * Check if a vehicle agent is home. Since the speed of the vehicle agents
     * is not constant, the step size is multiplied with the agent's speed
     * before checking if they are home.
     * @return whether the agent is home or not
     */
    private boolean isHomeStatus() {
        if (attributes.currentCellIndex < attributes.bestRouteToHome.size() - 1) {
            return false;
        } else {
            return true;
        }

    }

//    /**
//     * Check if the agent has reached home. The check depends on the AGENT_STEP_SIZE
//     * defined in the Constants class of the GlobalData package. The current algorithm
//     * checks if the agent is in the proximity of the home base by at most AGENT_STEP_SIZE to
//     * conclude that it has reched home.
//     * @return returns true if the agent has reched home.
//     */
//    private boolean isHomeStatus() {
//        if ((Math.abs(attributes.currentLocation.x - attributes.homeBase.getLatLon().x) <= Constants.AGENT_STEP_SIZE) &&
//                (Math.abs(attributes.currentLocation.y - attributes.homeBase.getLatLon().y) <= Constants.AGENT_STEP_SIZE)) {
//            attributes.currentLocation.x = attributes.homeBase.getLatLon().x;
//            attributes.currentLocation.y = attributes.homeBase.getLatLon().y;
//            Utilities.Log.logger.info(this.AID + " : Nowhere to go !");
//            return true;
//        }
//        return true;
//    }
    /**
     * Run method that runs the individual behaviours that is associated with this agent.
     * The person agents Run method checks if the agent is dead before its behaviour is invoked.
     * Dead agents do not run the behaviour.
     */
    @Override
    public void run() {

        if (this.objectiveFlag == true) {
            statusFlag = true;
            return;
        }

        //check health, if you die then add yourself to dead queue and set flag
        //move one step towards home base and update your pos and set flag
        if (attributes.HEALTH == 0) {
            SharedData.deadPeopleAgents.add(this);
            statusFlag = true;
            objectiveFlag = true;
            return;
        } else {
            // TODO decrease health only if within a radious
            if(attributes.HEALTH < attributes.CRITICAL_HEALTH){
                attributes.HEALTH--;
            }
            if (!statusFlag) {
                isHome = isHomeStatus();
                //isHome = !move(); //Agent wont move when he has reached hospital
                behaviour.run(attributes);
                statusFlag = true;//Function call done
            } else {
                return;
            }
        }

        //Right now their objective is to reach home, therefore ishome = objectiveFlag
        objectiveFlag = isHome;

    }

    /**
     * Compute the shortest path to the home base for an agent.
     * @return the ordered ArrayList of Points for the agent to follow.
     * @see Point
     */
    private ArrayList<Point> computeShortestPath() {
        ArrayList<Point> path = new ArrayList<Point>();
        return path;
    }

    /**
     * Get the nearest hospital for the agent.
     * @return the location of the nearest Hospital as a Point.
     */
    private Point nearestHospital() {
        Point nearest = new Point();
        return nearest;
    }

    /**
     * Override toString
     * @return String representation of the Person Object.
     */
    @Override
    public String toString() {
        return "ID: " + AID + " Class: " + getClass().toString() + " AttributeList: " + attributes.toString();
    }

    /**
     * Compare two people, return true if their IDs are equal, i.e. since the IDs are equal,
     * the agents must be the same.
     * @param obj Person Agent Object
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
        final Person other = (Person) obj;
        if (this.AID.equals(other.AID)) {
            return true;
        }
        return false;

    }

    /**
     * hashCode
     * @return
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Can't create a person without any attributes");
    }
}
