/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents;

import Agents.Attributes.PersonAttributes;
import GeographicInformation.Location;
import Utilities.AIDGenerator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgis.Point;

/**
 *
 * @author flummoxed
 */
public class Group extends Agent {

    ArrayList<Person> peopleInGroup;
    /**
     *
     */
    public Integer groupSize;
    boolean isHome = false;
    private PersonAttributes attributes;

    /**
     *
     * @param initattribs
     * @param initlocation
     * @param initbase
     */
    public Group(PersonAttributes initattribs, Point initlocation, Location initbase) {
        peopleInGroup = new ArrayList<Person>();
        groupSize = peopleInGroup.size();
        try {
            attributes = new PersonAttributes(initattribs.getCuriosity(), initattribs.getHealth(), initattribs.getHerdfactor(), initattribs.getPanic(), initattribs.getTrust());
            initattribs = null;
        } catch (InstantiationException ex) {
            Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
        }
        attributes.currentLocation = new Point(initlocation.x, initlocation.y);
        initlocation = null;
        attributes.homeBase = new Location(initbase.getLatLon(), initbase.getName());
        initbase = null;
        statusFlag = false;

        AID = AIDGenerator.newID(getClass().toString());
    }

    /**
     *
     * @param oneMoreBakra
     * @return
     */
    public boolean addPersonToGroup(Person oneMoreBakra) {
        //Averaging function to get the attributes for the group
        PersonAttributes toBeAdded = (PersonAttributes) oneMoreBakra.getMyattributes();

        int curiosity = toBeAdded.CURIOSITY;
        int health = toBeAdded.HEALTH;
        int herdfactor = toBeAdded.HERDFACTOR;
        int panic = toBeAdded.PANIC;
        int trust = toBeAdded.TRUST;

        Iterator<Person> one = peopleInGroup.iterator();

        while (one.hasNext()) {
            Person dummy = one.next();
            PersonAttributes attrib = (PersonAttributes) dummy.getMyattributes();
            curiosity += attrib.CURIOSITY;
            health += attrib.HEALTH;
            herdfactor += attrib.HERDFACTOR;
            panic += attrib.PANIC;
            trust += attrib.TRUST;
        }
        try {
            int newsize = peopleInGroup.size() + 1;
            attributes = new PersonAttributes(curiosity / newsize, health / newsize, herdfactor / newsize, panic / newsize, trust / newsize);
        } catch (InstantiationException ex) {
            Logger.getLogger(Group.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        peopleInGroup.add(oneMoreBakra);
        return true;

    }

    /**
     * Agent attributes in an object
     * @return the different attributes for an agent
     */
    public PersonAttributes getMyattributes() {
        return attributes;
    }

    /**
     * Agent attributes are set here
     * @param myattributes
     */
    public void setMyattributes(PersonAttributes myattributes) {
        this.attributes = myattributes;
    }

    /**
     * Get current location
     * @return the current location in latlon
     */
    public Point getMycurrentlocation() {
        return attributes.currentLocation;
    }

    /**
     * Set currentLocation
     * @param mycurrentlocation
     */
    public void setMycurrentlocation(Point mycurrentlocation) {
        this.attributes.currentLocation = mycurrentlocation;
    }

    /**
     * Return the agent's home base
     * @return the Location object
     */
    public Location getMyhomebase() {
        return attributes.homeBase;
    }

    /**
     * Set an agen't home base
     * @param myhomebase
     */
    public void setMyhomebase(Location myhomebase) {
        this.attributes.homeBase = myhomebase;
    }

    /**
     * Compute the shortest path
     * @return
     */
    private ArrayList<Point> computeShortestPath() {
        ArrayList<Point> path = new ArrayList<Point>();
        return path;
    }

    /**
     * Get the nearest hospital
     * @return
     */
    private Point nearestHospital() {
        Point nearest = new Point();
        return nearest;
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        String peopleattribs = null;
        Iterator<Person> one = peopleInGroup.iterator();
        while (one.hasNext()) {
            peopleattribs = one.next().toString() + " ";
        }
        return "ID: " + AID + " Class: " + getClass().toString() + " AttributeList: " + attributes.toString();
    }
}
