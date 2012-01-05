package Agents.Attributes;

import Entity.TrafficLight;
import java.util.ArrayList;

/**
 * This class currently has only the list of TrafficLights that a Hub handles.
 * This class will be modified if certain hubs are different.
 */
public class TrafficLightHubAttributes extends AgentAttributes {

    /**
     * The list of traffic lights that the hub controls.
     */
    public ArrayList<TrafficLight> trafficLights;

    /**
     * Create a TrafficLightAttributes object.
     * @param trafficLights the list of traffic lights managed by the hub.
     */
    public TrafficLightHubAttributes(ArrayList<TrafficLight> trafficLights) {
        this.trafficLights = trafficLights;
    }

    /**
     * Create an empty TrafficLightHubttributes object
     */
    public TrafficLightHubAttributes() {
    }

    /**
     * Get the list of TrafficLights that are managed.
     * @return List of TrafficLight Objects.
     */
    public ArrayList<TrafficLight> getTrafficLights() {
        return trafficLights;
    }

    /**
     * Set the list of TrafficLight Objects to be managed.
     * @param trafficLights List of TrafficLight Objects.
     */
    public void setTrafficLights(ArrayList<TrafficLight> trafficLights) {
        this.trafficLights = trafficLights;
    }

    /**
     * String Represnetation TrafficLight
     * @return Return a string representation of all the TrafficLights.
     */
    @Override
    public String toString() {
        String returnString = "";
        for (int i = 0; i < trafficLights.size(); i++) {
            returnString = returnString + "\n" + this.trafficLights.get(i);
        }
        return returnString;
    }

    /**
     * Compares if two traffic light objects are equal.
     * @param obj The TrafficLightHubAttribute object.
     * @return true if all the attributes are same, i.e. the list of all the
     * traffic lights managed is also same.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final TrafficLightHubAttributes other = (TrafficLightHubAttributes) obj;

        if (this.trafficLights.size() != other.trafficLights.size()) {
            return false;
        }

        for (int i = 0; i < other.trafficLights.size(); i++) {
            if (!this.trafficLights.get(i).equals(other.trafficLights.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return hash code of the TrafficLightHubArrtibutes object
     * @return hash code of the TrafficLightHubArrtibutes object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
