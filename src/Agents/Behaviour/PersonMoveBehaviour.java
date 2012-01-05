/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import Agents.Attributes.PersonAttributes;
import Entity.Hospital;
import GlobalData.Constants;
import GlobalData.DisasterLocations;
import GlobalData.SharedData;
import GeographicInformation.Location;
import java.util.Iterator;
import org.postgis.Point;

/**
 *
 * @author sagar
 */
public class PersonMoveBehaviour implements PersonBehaviour {

    private PersonAttributes personAttributes;

    public void run(AgentAttributes agentAttributes) {
        personAttributes = (PersonAttributes) agentAttributes;

        move();
    }

    /**
     *
     */
    public void decideWhereToMove() {
        //Utilities.Log.logger.info(this.AID + " : Identifying destination");
        //Check if disaster has been triggered
        //go home if not, otherwise decide where to go
        if (!SharedData.isDisasterTriggered) {
            //Utilities.Log.logger.info("I'm happy in a peaceful city");
            return;
        } else {
            // based on curiosity run towards disaster
            double distancetodisaster;
            double distancetohospital = -10;

            Point disasterlocation = SharedData.disasters.get(0).getLatlon();

            Point closesthospitallocation = personAttributes.homeBase.getLatLon();

            distancetodisaster = (Math.pow(personAttributes.currentLocation.x - disasterlocation.x, 2) + Math.pow(personAttributes.currentLocation.y - disasterlocation.y, 2));
            // Utilities.Log.logger.info("Distance to disaster = " + distancetodisaster);

            Iterator<Hospital> iterator = SharedData.hospitals.iterator();

            //Utilities.Log.logger.info("Number of hospitals: " + SharedData.hospitals.size());

            //identify closest hospital
            double newdistance = 0;
            while (iterator.hasNext()) {
                Hospital target = iterator.next();

                Point targetlocation = target.getLatLon();

                newdistance = (Math.pow(personAttributes.currentLocation.x - targetlocation.x, 2) + Math.pow(personAttributes.currentLocation.y - targetlocation.y, 2));

                if (newdistance < distancetohospital || distancetohospital < 0) {
                    closesthospitallocation = targetlocation;
                    distancetohospital = newdistance;
                }
            }

            // Utilities.Log.logger.info("Distance to closest hospital = " + distancetohospital);

            //compare if hospital and disaster based on curiosity
            double curiousdisasterdistance = distancetodisaster * (100 - personAttributes.CURIOSITY);
            double curioushospitaldistance = distancetohospital * personAttributes.CURIOSITY;

            if (curiousdisasterdistance < curioushospitaldistance) {
                personAttributes.homeBase.setLatLon(disasterlocation);
                //Utilities.Log.logger.info("Moving to disaster");
            } else {
                personAttributes.homeBase.setLatLon(closesthospitallocation);
                // Utilities.Log.logger.info("Moving to Hospital");
            }

        }
    }

    private void move() {

        if (personAttributes.getHealth() <= PersonAttributes.CRITICAL_HEALTH) {
            Utilities.Log.logger.info("I need an ambulance!!");
            return;
        }

        decideWhereToMove();

        //now do the running bit
        int directionX, directionY;
        directionX = 1;
        directionY = 1;

        if (personAttributes.currentLocation.x > personAttributes.homeBase.getLatLon().x) {
            directionX = -1;
        }
        if (personAttributes.currentLocation.y > personAttributes.homeBase.getLatLon().y) {
            directionY = -1;
        }

        if ((Math.abs(personAttributes.currentLocation.x - personAttributes.homeBase.getLatLon().x) <= Constants.AGENT_STEP_SIZE) &&
                (Math.abs(personAttributes.currentLocation.y - personAttributes.homeBase.getLatLon().y) <= Constants.AGENT_STEP_SIZE)) {
            personAttributes.currentLocation.x = personAttributes.homeBase.getLatLon().x;
            personAttributes.currentLocation.y = personAttributes.homeBase.getLatLon().y;

            if (personAttributes.currentLocation.x == SharedData.disasters.get(0).getLatlon().x  && personAttributes.currentLocation.y == SharedData.disasters.get(0).getLatlon().y) {
                Utilities.Log.logger.info("Reached Disaster!");
                return;
            }

//            Location location = DisasterLocations.locations.get(0);
//            Point point = location.getLatLon();
//
//            if (personAttributes.currentLocation.x == DisasterLocations.locations.get(0).getLatLon().x && personAttributes.currentLocation.y == DisasterLocations.locations.get(0).getLatLon().y) {
//                Utilities.Log.logger.info("Reached Disaster!");
//                return;
//            }
            Utilities.Log.logger.info("Nowhere to go !");
            return;
            //return false;
        }

        personAttributes.currentLocation.x = personAttributes.currentLocation.x + (directionX * Constants.AGENT_STEP_SIZE);
        personAttributes.currentLocation.y = personAttributes.currentLocation.y + (directionY * Constants.AGENT_STEP_SIZE);
        Utilities.Log.logger.info("New Location = " + personAttributes.currentLocation.toString());
        //return true;
    }
}
