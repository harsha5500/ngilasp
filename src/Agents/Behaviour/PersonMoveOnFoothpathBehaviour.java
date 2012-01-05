/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import Agents.Attributes.PersonAttributes;
import Entity.Hospital;
import GlobalData.SharedData;
import java.util.Iterator;
import org.postgis.Point;

/**
 *
 * @author sagar
 */
public class PersonMoveOnFoothpathBehaviour implements PersonBehaviour {

    private PersonAttributes personAttributes;

    public void run(AgentAttributes agentAttributes) {
        personAttributes = (PersonAttributes) agentAttributes;

        move();
    }

    /**
     * Simple DEBUG move method for traversiong the cells
     */
    private void move() {

        //        personAttributes.currentCellLocation.setOccupied(false); //leave the current cell

        if(!personAttributes.shouldMove){
            return;
        }

        if(personAttributes.bestRouteToHome.size() <= personAttributes.currentCellIndex){
            return;
        }
        personAttributes.currentCellLocation = personAttributes.bestRouteToHome.get(personAttributes.currentCellIndex);//move to next one
        personAttributes.currentCellIndex += personAttributes.SPEED;
        if(personAttributes.currentCellIndex >= personAttributes.bestRouteToHome.size()){
            personAttributes.currentCellIndex = personAttributes.bestRouteToHome.size()-1;
        }

        Utilities.Log.logger.info("index: " + personAttributes.currentCellIndex + " moved to:" + personAttributes.currentCellLocation.toString());

      //      personAttributes.currentCellLocation.setOccupied(true);//set the occupancy
    }

}
