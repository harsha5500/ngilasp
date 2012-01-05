package Agents;

import Agents.Attributes.TrafficLightHubAttributes;
import Agents.Behaviour.CompositeBehaviour;
import Agents.Behaviour.TrafficLightHubBehaviour;
import Database.Sql;
import Entity.TrafficLight;

/**
 * This class represents the TrafficLightHub Agent which controls a set of
 * traffic lights. TrafficLightHub is added manually identified on the map and
 * is added to the database manually. The list of traffic light a hub controls is
 * added manually and created when the hub is created.
 * @see TrafficLight
 * @see TrafficLightAttributes
 */
public class TrafficLightHub extends Agent {

    /**
     * ID of the traffic light hub on the map. Each hub has a unique ID.
     * NOTE: The ID once set cannot be modified hence the lack of set Method.
     * These IDs are preloaded into the database and read from there.
     */
    private int hubId;
    /**
     * Attributes of the traffic light hub
     * @see TrafficLightHubAttributes
     */
    private TrafficLightHubAttributes attributes = new TrafficLightHubAttributes();

    /**
     * Constructor to create a TrafficLightHub. The constuctor will also generate the
     * TrafficLights the Hub controls.
     * @param hubId the ID of the TrafficLightHub to be created.
     */
    public TrafficLightHub(int trafficLightHubID) {
        this.hubId = trafficLightHubID;
        this.generateTrafficLights();

        //Add the traffic light behaviour
        behaviour = new CompositeBehaviour();
        behaviour.add(new TrafficLightHubBehaviour());
    }

    /**
     * Constructor to create a TrafficLightHub.
     */
    public TrafficLightHub() {
    }

    /**
     * Returns the HubId of the traffic lights
     * @return hubid of the traffic lights
     */
    public int getHubId() {
        return hubId;
    }

    /**
     * Function that generates the list of TrafficLight by getting the data of
     * all the traffic lights the Hub controls from the database.
     * @see Database.Sql
     */
    private void generateTrafficLights() {
        //Read the data base and for each of the traffic light agent create a TrafficLight object and add it to the arraylist
        this.attributes.trafficLights = Sql.getTrafficLights(hubId);
        Utilities.Log.logger.info("hubId " + hubId + " fecthed traffic lights");
        //Log the values here
        Utilities.Log.logger.info("The traffic lightes for " + this.hubId + " are: " + this.attributes.trafficLights);
    }

    /**
     * The run method of a agent performs the all of the agent behaviours.
     * It checks if a traffic light has completed timeout and changes its signal.
     */
    @Override
    public void run() {
        if (!statusFlag) {
            behaviour.run(attributes);
            statusFlag = true;//Function call done
        }
    }

    /**
     * Retrun true if two TrafficLight hubs have the same ID.
     * @param obj object of type TrafficLightHub
     * @return true if the hubs have the same hubId.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrafficLightHub other = (TrafficLightHub) obj;

        //Note we can compare AID here to get the same effect.
        if (this.getHubId() != other.getHubId()) {
            return false;
        }
        return true;
    }

    /**
     * Return hash code of this TrafficLightHub object
     * @return hash code of this TrafficLightHub object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns the hub hubId and the list of traffic lights the agents is handeling.
     * @return the hub hubId and the list of traffic lights the agents is handeling.
     */
    @Override
    public String toString() {
        String returnString = "";
        returnString = returnString + "HUB ID: " + getHubId() + "Handeling:\n" + attributes.toString();
        return returnString;
    }

    /**
     * Returns the attributes of this traffic light hub
     * @return attributes of this traffic light hub
     */
    public TrafficLightHubAttributes getAttributes() {
        return attributes;
    }
}
