package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import Agents.Attributes.EmergencyServiceAttributes;
import GlobalData.SharedData;

/**
 * This is a behaviour very similar to the vehicle move behaviour. The vehicle
 * moves to a disaster location to signify helping contain the disaster.
 */
public class PoliceMoveBehaviour implements VehicleBehaviour {

    /**
     * The EmergencyService attributes
     */
    EmergencyServiceAttributes runAttributes;
    /**
     * Flag to check if the agent has moved
     */
    boolean flag;

    /**
     * Run the behaviour of the agent with the given attributes.
     * @param agentAttributes the agent's attributes
     */
    public void run(AgentAttributes agentAttributes) {
        runAttributes = (EmergencyServiceAttributes) agentAttributes;
        move();
    }

    /**
     * Simple DEBUG move method for traversiong the cells
     * Note this is methods does not make use of cell occupancy.
     */
    private void move() {
        Utilities.Log.logger.info("Police Move Behaviour");
        if (!SharedData.isDisasterTriggered) {
            Utilities.Log.logger.info("No best Route");
            return;
        } else {

            //Disaster is triggered. Move to a disaster location
            if (runAttributes.bestRouteToHome.size() == 0) {
                return;
            }
            runAttributes.currentCellLocation = runAttributes.bestRouteToHome.get(runAttributes.currentCellIndex);//move to next one
            runAttributes.currentCellIndex += runAttributes.SPEED;
            if (runAttributes.currentCellIndex >= runAttributes.bestRouteToHome.size()) {
                runAttributes.currentCellIndex = runAttributes.bestRouteToHome.size() - 1;
            }
            Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString());
            //      runAttributes.currentCellLocation.setOccupied(true);//set the occupancy
        }
    }
}
