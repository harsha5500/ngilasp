package GlobalData;

import Agents.Person;
import Agents.Vehicle;
import Entity.Disaster;
import Entity.Hospital;
import Entity.TrafficLight;
import GeographicInformation.BoundingBox;
import System.EmergencyServiceCTA;
import System.PeopleCTA;
import System.TrafficLightCTA;
import System.VehicleCTA;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data that is accessible to all agents on a single CTA. This class is used
 * to store data that is shared among the agents of the given CTA.
 */
public class SharedData {

    /**
     * The number of Person agents in the CTA
     */
    public static int numberOfAgents;
    /**
     * The total number of PeopleCTAs present in the entire simulation
     * @see PeopleCTA
     */
    public static int numberOfPeopleCTA;
    /**
     * The number of vehicle agents that behave as ambulances.
     * @see Vehicle
     */
    public static int numberOfAmbulances;
    /**
     * The number of vehicle agents that behave as police vehicles.
     * @see Vehicle
     */
    public static int numberOfPoliceVehicles;
    /**
     * The number of vehicle agents that behave as fire tenders.
     * @see Vehicle
     */
    public static int numberOfFireVehicles;
    /**
     *The number of vehicles agents that behave as normal civilian vehicles.
     * @see Vehicle
     */
    public static int numberOfCivilVehicles;
    /**
     * The flag that determines whether a disaster is triggered or not.
     */
    public static boolean isDisasterTriggered = false;
    /**
     * The section of map that is of interest.
     * @see BoundingBox
     */
    public static BoundingBox boundingBox;
    /**
     * Flag to enable state save
     */
    public static boolean isStateSaveEnabled = false;
    /**
     * Tell CTA to save state at thic tick
     */
    public static int saveStateAtTick = 35;
    /**
     * The list of dead people agents. Since the agents are dead the agents do not execute
     * their behaviour.
     * @see Person
     */
    public static List<Person> deadPeopleAgents = Collections.synchronizedList(new ArrayList<Person>());
    /**
     * The list of hospitals that are present in the current bounding box.
     * @see Hospital
     */
    public static List<Hospital> hospitals = Collections.synchronizedList(new ArrayList<Hospital>());
    /**
     * The number of ambulances in per hospital. Note that the size of this list will be equal to the number of the hospitals.
     * @see Utilities.ConfigLoader
     * @see Hospital
     */
    public static List<Integer> ambulancePerHospital = Collections.synchronizedList(new ArrayList<Integer>());
    /**
     * The list of disasters that are present in the current bounding box.
     * @see Disaster
     */
    public static List<Disaster> disasters = Collections.synchronizedList(new ArrayList<Disaster>());
    /**
     * The list of traffic light status that will be used by Vehicle CTA and
     * updated by TrafficLightCTA
     * @see VehicleCTA
     * @see TrafficLightCTA
     */
    public static List<TrafficLight> trafficLights = Collections.synchronizedList(new ArrayList<TrafficLight>());
    /**
     *  The list of critically injured people that is received from PeopleCTA for a given tick.
     * This list is sent by the PeopleCTA and shared with the EmergencyServiceCTA.
     * @see PeopleCTA
     * @see EmergencyServiceCTA
     */
    public static List<Person> newInjuredPeople = Collections.synchronizedList(new ArrayList<Person>());
    /**
     *  THe list of critically injured Person agents currently in transit in a
     * ambulance
     * @see PeopleCTA
     * @see EmergencyServiceCTA
     */
    public static List<Person> injuredPeopleBeingServed = Collections.synchronizedList(new ArrayList<Person>());
    /**
     * The list of critically injured Person agents rescued i.e. present in a
     * hospital.
     * @see PeopleCTA
     * @see EmergencyServiceCTA
     */
    public static List<Person> injuredPeopleRescued = Collections.synchronizedList(new ArrayList<Person>());
    /**
     * The list of critically injured Person agent picked up from a disaster. i.e.
     * this is the list of person agents that the ambulance has reched and therefore
     * can be removed from visualization. NOTE: the list of agents being served is not the
     * same as the list picked up.
     */
    public static List<Person> pickedUpinjuredPeople = Collections.synchronizedList(new ArrayList<Person>());

    private SharedData() {
    }
}
