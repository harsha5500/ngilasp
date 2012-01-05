package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import Agents.Attributes.VehicleAttributes;
import Entity.TrafficLight;
import GeographicInformation.Cell;
import GlobalData.Constants;
import GlobalData.SharedData;
import java.util.ArrayList;

/**
 * This is the move behaviour for a vehicle agent. A vehicle "moves" by changing
 * its current occupying cell to the next one in its list of the safest path.
 */
public class VehicleMoveBehaviour implements VehicleBehaviour {

    //If capacity == Load
    /**
     * The vehicle agent attribute
     */
    VehicleAttributes runAttributes;
    /**
     * Check if the agent is done executing.
     */
    boolean flag;

    /**
     * Run the behaviour of the agent with the given attributes.
     * @param agentAttributes the agent's attributes
     */
    public void run(AgentAttributes agentAttributes) {
        runAttributes = (VehicleAttributes) agentAttributes;
        move();
    }

    /**
     * Simple DEBUG move method for traversiong the cells
     * Note this is methods does not make use of cell occupancy.
     */
    private void moveCell() {

        if (!runAttributes.shouldMove) {
            return;
        }

        //runAttributes.currentCellLocation.setOccupied(false); //leave the current cell
        if (checkTrafficLightStatus()) {
            runAttributes.currentCellLocation = runAttributes.bestRouteToHome.get(runAttributes.currentCellIndex);//move to next one
            runAttributes.currentCellIndex += runAttributes.SPEED;
            if (runAttributes.currentCellIndex >= runAttributes.bestRouteToHome.size()) {
                runAttributes.currentCellIndex = runAttributes.bestRouteToHome.size() - 1;
            }

            Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString());

        } else {
            Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString() + "Because of Light");
        }
        //runAttributes.currentCellLocation.setOccupied(true);//set the occupancy
    }

    /**
     * Simple move method for traversiong the cells
     */
    private void move() {

        if (!runAttributes.shouldMove) {
            return;
        }
        //runAttributes.currentCellLocation.setOccupied(false); //leave the current cell
        if (checkTrafficLightStatusOnRoad()) {
            //Signal is clear to move
            runAttributes.currentCellLocation = runAttributes.bestRouteToHome.get(runAttributes.currentCellIndex);//move to next one
            //Check if the cell is occupied
                /*check is the vehicle can move that far.
             * if yes, then check if its occupid.
             * If no, chcek if destination is occupied.
             * NOTE: No action for a occupied cell.
             * TODO: Reduce speed of vehicle or other alternatives for
             * a occupied cell.
             */
            //int futureCellIndex = runAttributes.currentCellIndex + runAttributes.SPEED; //The location where the agent will potentially move to
            int futureCellIndex = getNearestUnoccupiedCell(runAttributes.SPEED, runAttributes.bestRouteToHome, runAttributes.currentCellIndex);//Check if the location exists...
            //if (runAttributes.bestRouteToHome.size() >= futureCellIndex) {
            //Location exists
//                if ((runAttributes.bestRouteToHome.get(futureCellIndex).isOccupied())) {
//                    //Cell is occupied dont move
//                    Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString() + " Because cell is occupied");
//                } else {
//                    runAttributes.currentCellLocation.setOccupied(false); //leave the current cell
//                    runAttributes.currentCellIndex += runAttributes.SPEED;
//                    runAttributes.currentCellLocation.setOccupied(true); //set the current cell occupancy.
//                    if (runAttributes.currentCellIndex >= runAttributes.bestRouteToHome.size()) {
//                        runAttributes.currentCellIndex = runAttributes.bestRouteToHome.size() - 1;
//                    }
//                    Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString());
//                }
            /* Set the cell as occupied and verify the return value. If setting fails then stay in current
             * location.
             */
            if (runAttributes.bestRouteToHome.get(futureCellIndex).updateOccupiedFlagInDB(true)) { //IF successfully changed
                runAttributes.currentCellLocation.updateOccupiedFlagInDB(false); //leave the current cell
                runAttributes.currentCellIndex = futureCellIndex;
                runAttributes.currentCellLocation = runAttributes.bestRouteToHome.get(futureCellIndex); // change current location
                Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString());
            } else {
                Utilities.Log.logger.info(" Could not move as the set occupied failed on cell id = " + (runAttributes.bestRouteToHome.get(futureCellIndex)).getId());
            }
//            } else { //The speed overshoots the home location. Hence move to last location
//
//                Utilities.Log.logger.info("index: " + runAttributes.currentCellIndex + " moved to:" + runAttributes.currentCellLocation.toString() + " Direct Jump Home");
//            }
            //runAttributes.currentCellLocation.setOccupied(true);//set the occupancy
        }
    }

    /**
     * This function returns the nearest un-occupied cell form current location to the home
     * given the vehicle speed. This can be interpreted as a reduction in speed.
     * @param vehicleSpeed the speed of the vehicle
     * @param bestRouteHome the list of cells that the vehicle follows to reach home.
     * @return the nearest unoccupied cell form current cell.
     *
     */
    private int getNearestUnoccupiedCell(int vehicleSpeed, ArrayList<Cell> bestRouteHome, int currentCellIndex) {
        int probableCell = currentCellIndex + vehicleSpeed;//The location where the agent will potentially move to

        // Check if the vehicle overshoots the home base
        if (probableCell >= bestRouteHome.size() - 1) {
            probableCell = bestRouteHome.size() - 1;
        }

        //Check occupancy
        for (int i = (currentCellIndex + 1); i < probableCell; i++) {
            if (bestRouteHome.get(i).isOccupied()) {
                return i - 1;
            }
        }
        return probableCell;
    }

    private boolean checkTrafficLightStatusOnRoad() {
//        //Get the current cell in which the vehicle is located from its best route.
//        Cell possibility = runAttributes.bestRouteToHome.get(runAttributes.currentCellIndex);
//
//        //From the list of traffic lights find the nearest traffic light located nearest to this cell.
//        //If the traffic light is at a distance of more that OBEY_TRAFFIC_LIGHT_DISTANCE cells then
//        //ignore the traffic light else check the color of the light.
//        for (TrafficLight l : SharedData.trafficLights) {
//            if (l.getLocation().getLatLon().distance(possibility.getLatLon()) > Constants.OBEY_TRAFFIC_LIGHT_DISTANCE) {
//                //Light is too far. Ignore the traffic light.
//                return true;
//            } else {
//                if (l.getColor() == GlobalData.Constants.SIGNAL_GREEN) {
//                    return true;
//                }
//            }
//        }
//        return false;
        return true;
    }

    /**
     * This function checks if there are any traffic light around and checks if
     * the traffic light rules have to be followed.
     * NOTE: The function cannot identity the exactly which road the traffic
     * light controls. But generally follows the nearest traffic light. This
     * may lead to semantic errors in future.
     * @return
     */
    //TODO: Better algorithm for traffic light rules
    private boolean checkTrafficLightStatus() {
        //Get the current cell in which the vehicle is located from its best route.
        Cell possibility = runAttributes.bestRouteToHome.get(runAttributes.currentCellIndex);

        //From the list of traffic lights find the nearest traffic light located nearest to this cell.
        //If the traffic light is at a distance of more that OBEY_TRAFFIC_LIGHT_DISTANCE cells then
        //ignore the traffic light else check the color of the light.
        for (TrafficLight l : SharedData.trafficLights) {
            if (l.getLocation().getLatLon().distance(possibility.getLatLon()) > Constants.OBEY_TRAFFIC_LIGHT_DISTANCE) {
                //Light is too far. Ignore the traffic light.
                return true;
            } else {
                if (l.getColor() == GlobalData.Constants.SIGNAL_GREEN) {
                    return true;
                }
            }
        }
        return false;
    }
//    public void decideWhereToMove() {
//        //Utilities.Log.logger.info(this.AID + " : Identifying destination");
//        //Check if disaster has been triggered
//        //go home if not, otherwise decide where to go
//        if (!SharedData.isDisasterTriggered) {
//            //Utilities.Log.logger.info("I'm happy in a peaceful city");
//            return;
//        } else {
//            // based on curiosity run towards disaster
//            double distancetodisaster;
//            double distancetohospital = -10;
//
//            Point disasterlocation = SharedData.disasters.get(0).getLatlon();
//
//            Point closesthospitallocation = runAttributes.homeBase.getLatLon();
//
//            distancetodisaster = (Math.pow(runAttributes.currentCellLocation.getLatLon().x - disasterlocation.x, 2) + Math.pow(runAttributes.currentCellLocation.getLatLon().y - disasterlocation.y, 2));
//            // Utilities.Log.logger.info("Distance to disaster = " + distancetodisaster);
//
//            Iterator<Hospital> iterator = SharedData.hospitals.iterator();
//
//            //Utilities.Log.logger.info("Number of hospitals: " + SharedData.hospitals.size());
//
//            //identify closest hospital
//            double newdistance = 0;
//            while (iterator.hasNext()) {
//                Hospital target = iterator.next();
//
//                Point targetlocation = target.getLatLon();
//
//                newdistance = (Math.pow(runAttributes.currentCellLocation.getLatLon().x - targetlocation.x, 2) + Math.pow(runAttributes.currentCellLocation.getLatLon().y - targetlocation.y, 2));
//
//                if (newdistance < distancetohospital || distancetohospital < 0) {
//                    closesthospitallocation = targetlocation;
//                    distancetohospital = newdistance;
//                }
//            }
    // Utilities.Log.logger.info("Distance to closest hospital = " + distancetohospital);
    //compare if hospital and disaster based on curiosity
//            double curiousdisasterdistance = distancetodisaster * (100 - runAttributes.CURIOSITY);
//            double curioushospitaldistance = distancetohospital * runAttributes.CURIOSITY;
//
//            if (curiousdisasterdistance < curioushospitaldistance) {
//                runAttributes.homeBase.setLatLon(disasterlocation);
//                //Utilities.Log.logger.info("Moving to disaster");
//            } else {
//                runAttributes.homeBase.setLatLon(closesthospitallocation);
//                // Utilities.Log.logger.info("Moving to Hospital");
//            }
//
//        }
//    }
}
