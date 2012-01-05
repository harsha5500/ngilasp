package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import Agents.Attributes.TrafficLightHubAttributes;

/**
 * This behaviour operates the signals displayed by all the traffic lights controlled
 * by this hub, by changing them depending on the display time.
 */

/*
 * NOTE: The changing signal behaviour or the values for the signal timeouts needs
 * to be set depending on the intersection of the roads. This is not reflected
 * in the current operation.
 */
public class TrafficLightHubBehaviour implements TrafficLightBehaviour {

    /**
     * The TrafficLightHub attribute
     */
    TrafficLightHubAttributes runAttributes;
    /**
     * Check if agent is done executing.
     */
    boolean flag;

    /**
     * Run the behaviour of the agent with the given attributes.
     * @param agentAttributes the agent's attributes
     */
    public void run(AgentAttributes agentAttributes) {
        runAttributes = (TrafficLightHubAttributes) agentAttributes;
        changeSignal();
    }

    /**
     * This method flips the signal light of the traffic Light agent
     * i.e RED to GREEN or vice versa, if the traffic Light agent's currentStep
     * is equal to duration of the signal. Otherwise the currentStep counter
     * is incremented
     */
    private void changeSignal() {

        for (int i = 0; i < runAttributes.trafficLights.size(); i++) {

            //Increment the current step for all traffic signals.
            runAttributes.trafficLights.get(i).incrementStep();

            //Then check the current Step and change signal if required.
            if (runAttributes.trafficLights.get(i).isDurationComplete()) {
                runAttributes.trafficLights.get(i).flipSignal();
                //runAttributes.trafficLights.get(i).setColor(GlobalData.Constants.SIGNAL_RED);
                runAttributes.trafficLights.get(i).setCurrentStep(0); //Reset the current step.

                Utilities.Log.logger.info("Flipped the signal at " + runAttributes.trafficLights.get(i).getLocation()
                        + " to " + runAttributes.trafficLights.get(i).getLight());
            }
        }
    }
}
