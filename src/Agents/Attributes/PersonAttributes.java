/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents.Attributes;

import GeographicInformation.Cell;
import GeographicInformation.Location;
import java.util.ArrayList;
import org.postgis.Point;

/**
 * The Agent attributes are
 * -HEALTH
 * -CURIOSITY
 * -HERDFACTOR
 * -PANIC
 * -TRUST
 * for agents such as People and Groups
 * @author flummoxed
 */
public class PersonAttributes extends AgentAttributes {

    /**
     * Agent health
     */
    public int HEALTH;
    /**
     * Agent curiosity
     */
    public int CURIOSITY;
    /**
     * Likelyhood of an agent creating/joining a group
     */
    public int HERDFACTOR;
    /**
     * Extent of an agent panicking
     */
    public int PANIC;
    /**
     * The trust that has an agent has on external sources
     */
    public int TRUST;

    /**
     *The Speed of the vehicle in terms of cells per tick message.
     */
    public int SPEED = 1;

    /**
     * The flag telling wheather a agent should move or stay where it is
     */
    public boolean shouldMove = true;
    
    /**
     * 
     */

    /**
     * Maximum integer value for all agent attributes
     */
    public final static int MAX_VALUE = 100;
    /**
     * Minimum integer value for all agent attributes
     */
    public final static int MIN_VALUE = 0;
    /*
     * Integer value for health after which agent shall not be able to move
     * This has to be modified later on in order to be more descriptive about the type of injury and the kind of help needed
     */
    public final static int CRITICAL_HEALTH = 30;
    /*
     * Agent's home
     */
    public Location homeBase;

        /**
     * The cell in which the Vehicle agent is currently situated in.
     */
    public Cell currentCellLocation;
    
    /**
     * Current Point location of an agent
     */
    public Point currentLocation;

        /**
     * Stores the index of the current cell in the best path array list.
     */
    public int currentCellIndex;
    

     /**
     * the primary key of the point (vertex) in the database
     */
    public long homeBaseId;

    /**
     * the primary key of the point (vertex) in the database
     */
    public long startCellId;
    
    /*
     * Shortest path to home based on certain criteria
     */
    public ArrayList<Cell> bestRouteToHome;

    /**
     * Status flag, true if person has completed actions for the tick and false if not complete
     */
    /**
     * Initialize an object of Person Attributes
     * @param curiosity integer value for curiosity of the agent
     * @param health integer value for the health of the agent
     * @param herdfactor integer value to represent the agent's tendency to form groups
     * @param panic integer value for panic of the agent.
     * @param trust integer value for agent's trust on information it receives.
     * @throws InstantiationException whenever any value violates limits
     */
    public PersonAttributes(int curiosity, int health, int herdfactor, int panic, int trust) throws InstantiationException {

        if (!setCuriosity(curiosity)) {
            throw new InstantiationException("Curiosity value out of bounds");
        }
        if (!setHealth(health)) {
            throw new InstantiationException("Health value is out of bounds");
        }
        if (!setHerdfactor(herdfactor)) {
            throw new InstantiationException("Herd factor out of bounds");
        }
        if (!setPanic(panic)) {
            throw new InstantiationException("Panic value out of bounds");
        }
        if (!setTrust(trust)) {
            throw new InstantiationException("Trust value out of bounds");
        }
    }

    /**
     * Curiosity of an agent
     * @return Curiosity value
     */
    public int getCuriosity() {
        return CURIOSITY;
    }

    /**
     * Set CURIOSITY between MIN_VALUE and MAX_VALUE defined in PersonAttributes.
     * @param curiosity curiosity of the agent
     * @return true if value lies between specified limits else false
     * @see PersonAttributes
     */
    public boolean setCuriosity(int curiosity) {
        if (curiosity >= MIN_VALUE && curiosity <= MAX_VALUE) {
            this.CURIOSITY = curiosity;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Health of the agent
     * @return HEALTH value of the agent
     */
    public int getHealth() {
        return HEALTH;
    }

    /**
     * Set HEALTH of an agent
     * @param health health of the agent.
     * @return true if value lies between specified limits else false
     */
    public boolean setHealth(int health) {
        if (health >= MIN_VALUE & health <= MAX_VALUE) {
            this.HEALTH = health;
            return true;
        } else {
            return false;
        }
    }

    /**
     * The likelihood of an agent forming a group
     * @return herd factor
     */
    public int getHerdfactor() {
        return HERDFACTOR;
    }

    /**
     * The herd factor, the likelihood of an agent forming a group
     * @param herdfactor The likelihood of an agent forming a group
     * @return true if value lies between specified limits else false
     */
    public boolean setHerdfactor(int herdfactor) {
        if (herdfactor >= MIN_VALUE && herdfactor <= MAX_VALUE) {
            this.HERDFACTOR = herdfactor;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Chances of an agent panicing
     * @return PANIC value
     */
    public int getPanic() {
        return PANIC;
    }

    /**
     * Sets the PANIC factor
     * @param panic Chances of an agent panicing
     * @return true if value lies between specified limits else false
     */
    public boolean setPanic(int panic) {
        if (panic >= MIN_VALUE && panic <= MAX_VALUE) {
            this.PANIC = panic;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Trust an agent places on information
     * @return TRUST factor
     */
    public int getTrust() {
        return TRUST;
    }

    /**
     * Set the TRUST an agent places on information
     * @param trust Trust an agent places on information
     * @return true if value lies between specified limits else false
     */
    public boolean setTrust(int trust) {
        if (trust >= MIN_VALUE && trust <= MAX_VALUE) {
            this.TRUST = trust;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersonAttributes test = (PersonAttributes) obj;
        if (CURIOSITY != test.CURIOSITY || HEALTH != test.HEALTH || HERDFACTOR != test.HERDFACTOR || PANIC != test.PANIC || TRUST != TRUST) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "CURIOSITY: " + CURIOSITY + " HEALTH: " + HEALTH + " HERDFACTOR: " + HERDFACTOR + " PANIC: " + PANIC + " TRUST: " + TRUST + " Home: " + homeBase.toString() + "Current Location: " + currentLocation.toString();
    }
}
