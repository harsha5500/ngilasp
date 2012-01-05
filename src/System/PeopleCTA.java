package System;

import Agents.Agent;
import Agents.Person;
import Agents.Attributes.PersonAttributes;
import Entity.CustomStyle;
import Entity.Disaster;
import Entity.Hospital;
import Entity.IdPointPair;
import Entity.TrafficLight;
import GeographicInformation.BoundingBox;
import GeographicInformation.Cell;
import GeographicInformation.Location;
import GlobalData.CTANetwork;
import GlobalData.Constants;
import GlobalData.SharedData;
import Messaging.Message;
import Messaging.QueueManager;
import Messaging.QueueParameters;
import Utilities.GenerateAttributes;
import Utilities.KmlUtility;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgis.Point;
import java.util.Collections.*;

/**
 * This class creates and manages threads for a number of people
 * @author jayanth
 */
public class PeopleCTA extends CareTakerAgent {

    HashMap<String, CustomStyle> peopleStyles = new HashMap<String, CustomStyle>();
    
    /**
     * the total number of people dead, among those handled by this caretaker agent
     */
    private int numberOfDead;
    /**
     * the total number of people injured, among those handled by this caretaker agent
     */
    private int numberOfInjured;
    /**
     * All the people handled by this caretaker agent
     */
    private ArrayList<Location> agentLocations;
    /**
     * The styles for the agents, when displayed through KML
     */
    private ArrayList<String> agentStyles;
    /**
     * Flag to make sure messages arent read.
     * The listener does not read messaged when this flag is true
     */
    private boolean holdMessages = false;
   
    /**
     * This flag when set will tell the CTA that state has been restored
     */
    private boolean restoreState = false;
   

    /**
     * Reads configurations, creates objects and begins listening to the message queue.
     */
    public PeopleCTA() {
        Utilities.Log.ConfigureLogger();
        //setTotalNumberOfPeople(SharedData.numberOfAgents);
        //setFirstTime(true);
        readConfigurations();
        createObjects();
        buildCTAStatus();
        setNumberOfDead(0);
        setNumberOfInjured(0);
        addQueueListener();
        Utilities.Log.logger.info("Number of CTAs : " + CTANetwork.hosts.size());
        currentTickNumber = 0;
    }

    /**
     * Creates Objects
     */
    private void createObjects() {
        CTAStatus = Collections.synchronizedMap(new HashMap<String, Integer>());
        agents = Collections.synchronizedList(new ArrayList<Agent>());
        agentStyles = new ArrayList<String>();
    }

    /**
     * Constructor, to be used when Restoring state from a particular point.
     * This constructor wil first restore state from the file, and then continue with a slightly different mode of running.
     * @param filename
     */
    private PeopleCTA(String filename) {
        Utilities.Log.ConfigureLogger();
        createObjects();
        restoreState(filename);
        addQueueListener();
        restoreState = true;
        Utilities.Log.logger.info("Restored state after " + currentTickNumber + " tick");
        sendRestoreState();
        Utilities.Log.logger.info("Sent Restored State Message");
        long timeBeforewaiting = System.currentTimeMillis();
        while (!checkIfAllCTAsDoneWithRestoreState()) {
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_SAVING_STATE) {
                    updateTimeOutList();
                    Utilities.Log.logger.info("Continuing with run after timeout for saving state");
                    break;
                } else {
                    //wait until all CTAs are ready
                    Thread.sleep(500);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        buildCTAStatus();
        Utilities.Log.logger.info("Number of CTAs : " + CTANetwork.hosts.size());
    }

    /**
     * Get the number of people alreadyd dead
     * @return
     */
    public int getNumberOfDead() {
        return numberOfDead;
    }

    /**
     *
     * @param numberOfDead
     */
    public void setNumberOfDead(int numberOfDead) {
        this.numberOfDead = numberOfDead;
    }

    /**
     * Get the number of people Injured
     * @return
     */
    public int getNumberOfInjured() {
        return numberOfInjured;
    }

    /**
     * Set the number of people injured
     * @param numberOfInjured
     */
    public void setNumberOfInjured(int numberOfInjured) {
        this.numberOfInjured = numberOfInjured;
    }

    /**
     * Function to group people, based on how long they travel together
     */
    void groupPeople() {
    }

    /**
     * Function to get the status of ambulances, and if they are near the people, add people to ambulances.
     * @see allocatePeopleToAmbulance
     */
    void getAmbulanceData() {
    }

    /**
     * Function to add people to ambulances.
     */
    void allocatePeopleToAmbulance() {
    }

    /**
     * Kill people, if they exit out of the map, or if they are too close to the disaster when it happens, or if their health becomes 0
     */
    void removePeopleFromSystem() {
        for (Person p : SharedData.deadPeopleAgents) {
            // to do : write equals and hash code method for people, to remove them from arraylists
            agents.remove(p);
            numberOfDead++;
        }
    }

    /**
     * Start the people threads
     */
    void createPeople() {

        for (int i = 0; i < SharedData.numberOfAgents; i++) {

            IdPointPair homeBaseIdPointPair = GenerateAttributes.generateIdPointPairOnRoad();
            long homebaseId = homeBaseIdPointPair.id;

            IdPointPair currentLocationIdPointPair = GenerateAttributes.generateIdPointPairOnRoad();
            long startcellId = currentLocationIdPointPair.id;

            Point currentLocation = currentLocationIdPointPair.point;

            Cell currentCellLocation = new Cell(currentLocation, false);

            Location homeBase = new Location(currentLocationIdPointPair.point, "");

            PersonAttributes lifeAttribs = GenerateAttributes.generateLifeAttributes();
            //Utilities.Log.logger.info(currentLocation.toString() + " " + homeBase.toString() + " " + lifeAttribs.toString());
            Person p = null;
            try {
                p = new Person(lifeAttribs, currentCellLocation, homeBase, startcellId, homebaseId);
            } catch (InstantiationException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }

            agents.add(p);
        }

        agentLocations = new ArrayList<Location>();
        for (Agent a : agents) {
            Person p = (Person) a;
            agentStyles.add(Constants.PERSON_HEALTHY);
            agentLocations.add(new Location(p.getCurrentCellLocation().getLatLon(), p.getAID()));
        }

        Utilities.Log.logger.info("Finished Creating People. Added " + agents.size() + " agents");
    }

    /**
     *
     * @return
     */
    @Override
    public boolean checkIfAllAgentsReadyForNextTick() {
        int count = 0;
        for (Agent p : agents) {
            if (p.statusFlag) {
                count++;
            }
        }
        if (count == agents.size()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Activate the agents, by calling their action methods
     */
    @Override
    public void activateAgentBehaviour() {
        Collections.shuffle(agents);
        for (Agent p : agents) {
            p.run();
        }
    }

    private void cleanUp() {
        for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
        }
        sendReadyForTick();

        kmlUtility.addPlacemarks(agentLocations, agents, "people", agentStyles,this.getCurrentTickNumber());


        Utilities.Log.logger.info("Sent ready for next tick");

        //kmlUtility.addPlacemarks(agentLocations, agentStyles,currentTickNumber);
        if (currentTickNumber % 10 == 0) {
            writeKMLFile("people");
            kmlUtility = new KmlUtility(peopleStyles);
        }

        for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
        }
           
        if ((currentTickNumber % SharedData.saveStateAtTick == 0 && SharedData.isStateSaveEnabled) || initiateStateSave) {
            initiateStateSave = false;
            Utilities.Log.logger.info("Sending message for CTAs to save their respective states at tick: " + currentTickNumber);
            sendSavingState(currentTickNumber);
            beginStateSave();
        }
            //holdMessages = true;
 //           changeSavedCTAsToReady();
            changeCTAStatus(Constants.localHost, CTANetwork.CTA_READY_FOR_NEXT_TICK);
            //holdMessages = false;
           for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
        }
    }

    /**
     * This begins the process of saving states, for the CTA.
     * After sending a message to other CTAs saying it is saving its state, it waits until every CTA is also doing the same.
     * It then writes the state as binary data into a file, and sends a message saying it has completed saving state.
     * It waits until all CTAs have completed saving their respective states before exiting the funtion.
     */
    private void beginStateSave() {
        // Check if all CTAs are saving state
        long timeBeforewaiting = System.currentTimeMillis();
        while (!checkIfAllCTAsReadyForStateSave()) {
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_SAVING_STATE) {
                    updateTimeOutList();
                    Utilities.Log.logger.info("Continuing with run after timeout for saving state");
                    break;
                } else {
                    //wait until all CTAs are ready
                    Thread.sleep(500);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        saveCTAState();
        initiateStateSave = false;
        sendSavedState();

        // Check if all CTAs have finished saving state
        timeBeforewaiting = System.currentTimeMillis();
        while (!checkIfAllCTAsDoneWithStateSave()) {
            Utilities.Log.logger.info("Waiting for CTA to finish saving state");
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_SAVING_STATE) {
                    updateTimeOutList();
                    Utilities.Log.logger.info("Continuing with run after timeout for saved state");
                    break;
                } else {
                    //wait until all CTAs are ready
                    Thread.sleep(500);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setUp() {
        // Boot functions
        if (Utilities.AIDGenerator.initializeAIDGen()) {
            createStyles();

        } else {
            Utilities.Log.logger.info("AIDs cannot be generated");
            exitSystem();
        }

        // Generate Agents, if running for first time. If restored from previous state dont
        if (!restoreState) {
            createPeople();
        }

        Utilities.Log.logger.info("Finished SetUp");
        for (int i = 0; i < SharedData.disasters.size(); i++) {
            Utilities.Log.logger.info("Disaster location : " + SharedData.disasters.get(i).getLatlon().toString());
        }

        for (int i = 0; i < SharedData.hospitals.size(); i++) {
            Utilities.Log.logger.info("Hospital location : " + SharedData.hospitals.get(i).latlon.toString());
        }
        //Utilities.Log.logger.info("Hospital location : " + SharedData.hospitals.get(1).latlon.toString());
        sendReadyForTick();
    }

    private void runAgents() {
        //Utilities.Log.logger.info("Started runAgents.");
        activateAgentBehaviour();
        agentLocations = new ArrayList<Location>();
        agentStyles = new ArrayList<String>();
        while (checkIfAllAgentsReadyForNextTick()) {
            numberOfDead = 0;
            numberOfInjured = 0;

            //Check if given agent has reached the home base
            for (int i = 0; i < agents.size(); i++) {
                Person p = (Person) agents.get(i);
                p.statusFlag = false;

                int health = p.getMyattributes().HEALTH;
                //Check if agent is dead
                if (health == 0) {
                    ((Person) agents.get(i)).objectiveFlag = true;
                }

                if (health == 0) {
                    agentStyles.add("Dead");
                    numberOfDead++;
                } else if (health <= 30) {
                    agentStyles.add("Critical");
                    numberOfInjured++;
                } else if (health <= 80) {
                    agentStyles.add("Injured");
                    numberOfInjured++;
                } else {
                    agentStyles.add("Healthy");
                }
                agentLocations.add(new Location(((Person) agents.get(i)).getCurrentCellLocation().getLatLon(), ""));
            }
            break;
        }
        //Utilities.Log.logger.info("Completed runAgents");
    }

    private void initialize() {
    }

    private boolean objectiveSatisfiedForAllAgents() {
        int count = 0;
        for (Agent p : agents) {
            if (p.objectiveFlag) {
                count++;
            }
        }

        Utilities.Log.logger.info((agents.size() - count) + " agents yet to satisfy objective");
        if (count == agents.size()) {
            return true;
        } else {
            return false;
        }
    }

    private void runCTA() {
        try {
            setUp();

            //Runs until objectives for all agents is fulfilled
            while (!objectiveSatisfiedForAllAgents()) {
                initialize();

                //Time out for waiting for other agents
                long timeBeforewaiting = System.currentTimeMillis();

                //now check if all agents are ready
                while (!checkIfAllCTAsReadyForNextTick(holdMessages)) {
                    try {
                        long timeNow = System.currentTimeMillis();
                        if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_CTA) {
                            updateTimeOutList();
                            Utilities.Log.logger.info("Continuing with run after timeout");
                            break;
                        } else {
                            //wait until all CTAs are ready
                            Thread.sleep(500);
                        }

                    } catch (InterruptedException ex) {
                        Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }


                if (initiateStateSave && currentTickNumber >= tickNumberForStateSave) {
                } else {
                    currentTickNumber++;
                    Utilities.Log.logger.info("Tick Number : " + currentTickNumber + " === Injured : " + numberOfInjured + " Dead : " + numberOfDead + " ===");

                    //Cycle through disasters to check which is the disaster that has to be triggered
//                Iterator<Disaster> iterator = SharedData.disasters.iterator();
//                while (iterator.hasNext()) {
//                    Disaster boom = iterator.next();
//                    if (currentTickNumber == boom.disasterTriggerTimeInTicks) {
//                        SharedData.isDisasterTriggered = true;
//                        Utilities.Log.logger.info("Disaster triggered");
//                        //Placemartk has to be added for the disaster
//                    }
//                }
                    kaboom();
                    holdMessages = true;
                     for (String host : CTAStatus.keySet()) {
                          Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
                     }
                    changeReadyCTAsToComputing();
                    //If sending the critical agent fails then stop the CTA
                    if (sendCriticalAgents() == false) {
                        Utilities.Log.logger.info("Failed to send critical agent data");
                        //break;
                    }
                     for (String host : CTAStatus.keySet()) {
                          Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
                      }
                    holdMessages = false;
                    runAgents();
                }
                cleanUp();
            }
            Utilities.Log.logger.info("All agents have completed!");
            exitSystem();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, e);
            String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
            //kmlUtility.writeFile();
            kmlUtility.writeFile("kml/" + "Error" + stamp + "_" + currentTickNumber + ".kml");
            Utilities.Log.logger.info("Unable to complete work due to execution. Terminating");
            
            sendDoneWithWork();
        }
    }

    /**
     * Main function
     * @param args
     */
    public static void main(String args[]) {


        if (args.length == 0) {
            PeopleCTA obj = new PeopleCTA();
            obj.runCTA();
        } else {
            PeopleCTA obj = new PeopleCTA(args[0]);
            obj.restoreState = true;
            obj.runCTA();
        }

    }

    private void exitSystem() {
        sendDoneWithWork();
//        sendFinishedExecutingCTA();
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        kmlUtility.writeFile("kml/peopleCTA_" + stamp + "_" + currentTickNumber + "Final_" + ".kml");
        System.exit(0);
    }

    /**
     * This function saves the state of the CTA. It saves all of the objects in the system as binary data in a file.
     */
    @Override
    protected void saveCTAState() {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        DataOutputStream dos = null;
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        String fileName = "savedstates/" + stamp + "_" + currentTickNumber + "_peopleCTA.data";
        try {

            fos = new FileOutputStream(fileName);
            out = new ObjectOutputStream(fos);
            dos = new DataOutputStream(fos);

            // Objects of PeopleCTA
            //out.writeObject(kmlUtility);
            //out.writeObject(queueManager);
            out.writeObject(CTAStatus);
            out.writeObject(agents);
            out.writeObject(agentLocations);
            out.writeObject(agentStyles);

            // Objects of CTANetwork
            out.writeObject(CTANetwork.hostQueueMap);
            out.writeObject(CTANetwork.hostTypeMap);
            out.writeObject(CTANetwork.hosts);

            // Objects of SharedData
            out.writeObject(SharedData.boundingBox);
            out.writeObject(SharedData.ambulancePerHospital);
            out.writeObject(SharedData.deadPeopleAgents);
            out.writeObject(SharedData.disasters);
            out.writeObject(SharedData.hospitals);
            out.writeObject(SharedData.trafficLights);
            out.writeObject(SharedData.injuredPeopleBeingServed);
            out.writeObject(SharedData.injuredPeopleRescued);
            out.writeObject(SharedData.newInjuredPeople);
            out.writeObject(SharedData.pickedUpinjuredPeople);

            dos.writeInt(SharedData.numberOfAgents);
            dos.writeInt(SharedData.numberOfAmbulances);
            dos.writeInt(SharedData.numberOfCivilVehicles);
            dos.writeInt(SharedData.numberOfFireVehicles);
            dos.writeInt(SharedData.numberOfPeopleCTA);
            dos.writeInt(SharedData.numberOfPoliceVehicles);
            dos.writeBoolean(SharedData.isDisasterTriggered);

            dos.writeInt(currentTickNumber);
            dos.writeInt(numberOfDead);
            dos.writeInt(numberOfInjured);
            dos.writeBoolean(holdMessages);

            dos.close();
            fos.close();
            out.close();

            Utilities.Log.logger.info("Saved State");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Restores the state of this CTA from a file. Thsi will restore all objects of the CTA saved previously.
     * The CTA will continue running from the point where state was restored, at the point in the file where it was previously saved
     * @param filename
     */
    @Override
    protected void restoreState(String filename) {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        DataInputStream din = null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            din = new DataInputStream(fis);

            //queueManager = (QueueManager) in.readObject();
            CTAStatus = (Map<String, Integer>) in.readObject();
            agents = (ArrayList<Agent>) in.readObject();
            agentLocations = (ArrayList<Location>) in.readObject();
            agentStyles = (ArrayList<String>) in.readObject();

            CTANetwork.hostQueueMap = (HashMap<String, QueueParameters>) in.readObject();
            CTANetwork.hostTypeMap = (HashMap<Integer, ArrayList<String>>) in.readObject();
            CTANetwork.hosts = (ArrayList<String>) in.readObject();

            SharedData.boundingBox = (BoundingBox) in.readObject();
            SharedData.ambulancePerHospital = (List<Integer>) in.readObject();
            SharedData.deadPeopleAgents = (List<Person>) in.readObject();
            SharedData.disasters = (List<Disaster>) in.readObject();
            SharedData.hospitals = (List<Hospital>) in.readObject();
            SharedData.trafficLights = (List<TrafficLight>) in.readObject();
            SharedData.injuredPeopleBeingServed = (List<Person>) in.readObject();
            SharedData.injuredPeopleRescued = (List<Person>) in.readObject();
            SharedData.newInjuredPeople = (List<Person>) in.readObject();
            SharedData.pickedUpinjuredPeople = (List<Person>) in.readObject();

            SharedData.numberOfAgents = (int) din.readInt();
            SharedData.numberOfAmbulances = (int) din.readInt();
            SharedData.numberOfCivilVehicles = (int) din.readInt();
            SharedData.numberOfFireVehicles = (int) din.readInt();
            SharedData.numberOfPeopleCTA = (int) din.readInt();
            SharedData.numberOfPoliceVehicles = (int) din.readInt();
            SharedData.isDisasterTriggered = (boolean) din.readBoolean();

            currentTickNumber = (int) din.readInt();
            numberOfDead = (int) din.readInt();
            numberOfInjured = (int) din.readInt();
            holdMessages = (boolean) din.readBoolean();

            din.close();
            in.close();
            fis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
//        }
        }
    }

    /**
     *
     * @param message
     */
//    public void receivedMessage(String message) {
////        //update status of the host from which message was received
////        while (holdMessages) {
////        }
////        String[] params = message.split(":");
////
////        if (Integer.parseInt(params[0].trim()) == CTANetwork.RMQ_TYPE_STATUS_UPDATE) {
////            if (Integer.parseInt(params[2].trim()) == CTANetwork.CTA_READY_FOR_NEXT_TICK) {
////                changeCTAStatus(params[1].trim().toString(), Integer.parseInt(params[2].trim()));
////
////            } else if (Integer.parseInt(params[2].trim()) == CTANetwork.CTA_DONE_WITH_WORK) {
////                changeCTAStatus(params[1].trim().toString(), Integer.parseInt(params[2].trim()));
////            }
////
////            Utilities.Log.logger.info("Status received : " + (Integer.parseInt(params[2].trim()) == CTANetwork.CTA_DONE_WITH_WORK ? "DONE_WITH_WORK" : (Integer.parseInt(params[2].trim()) == CTANetwork.CTA_READY_FOR_NEXT_TICK ? "READY_FOR_NEXT_TICK" : params[2])) + " : " + (Integer.parseInt(params[0].trim()) == CTANetwork.RMQ_TYPE_STATUS_UPDATE ? "RMQ_TYPE_STATUS_UPDATE" : (Integer.parseInt(params[0].trim()) == CTANetwork.RMQ_TYPE_AGENT_MSG) ? "RMQ_TYPE_AGENT_MSG" : params[0]) + " from " + params[1]);
////        }
//    }
    private void kaboom() {



//        if (currentTickNumber % 5 != 0) {
//            return;
//        }

        //this has to be done for all disasters for all agents
        //check for people within a certain radius and reduce their health by a factor-this has to be parameterized sometime soon
        //equation of a circle (x-h)^2+(y-k)^2=r^2 where (h,k) is the centre
        //therefore, factor f= (xp-h)^2+(yp-k)^2-r^2
        // f<0 implies within a radius. Health reduced as a factor of (r-f)/r so at the perimeter they still don't loose any health

        Iterator<Disaster> iterator = SharedData.disasters.iterator();

        while (iterator.hasNext()) {
            Disaster boom = iterator.next();
            if (boom.tick != currentTickNumber) {
                return;
            }

            System.out.println("Disaster!!! Disaster!!!\n Bhagooo.... Bhaaagooo");

            Double h = boom.getLatlon().getX();
            Double k = boom.getLatlon().getY();

            //Utilities.Log.logger.info("h: "+h.toString());
            //Utilities.Log.logger.info("k: "+k.toString());

            Double radius = boom.getIntensity() * 0.0008;

            Double rsquare = radius * radius;

            //Utilities.Log.logger.info("Radius square : " + rsquare.toString());

//            if (boom.disasterTriggerTimeInTicks <= currentTickNumber) {

            //cycle through all the people in the CTA
            Iterator<Agent> people = agents.iterator();
            while (people.hasNext()) {
                Person poorguy = (Person) people.next();

                int currenthealth = poorguy.getMyattributes().HEALTH;

                if (currenthealth == 0) {
                    continue;
                }

                Double xp = poorguy.getCurrentCellLocation().getLatLon().x;
                //Utilities.Log.logger.info("Xp: "+xp.toString());
                Double yp = poorguy.getCurrentCellLocation().getLatLon().y;
                //Utilities.Log.logger.info("yp: "+yp.toString());

                // Utilities.Log.logger.info("Distance square: " + ((Double) (((xp - h) * (xp - h)) + ((yp - k) * (yp - k)))).toString());

                Double factor = ((xp - h) * (xp - h)) + ((yp - k) * (yp - k)) - rsquare;
                //Utilities.Log.logger.info("Factor : " + factor.toString());

                if (factor < 0) {

                    //Double redfactor = (radius - Math.sqrt(Math.abs(factor))) / radius;
                    Double redfactor = (Math.sqrt(((xp - h) * (xp - h)) + ((yp - k) * (yp - k)))) / radius;

                    //Utilities.Log.logger.info("Reduction factor : " + redfactor);

                    //reduce health
                    int newHealth = (int) (currenthealth * redfactor);
                    Utilities.Log.logger.info("Person : " + poorguy.getAID() + " Health: " + poorguy.getMyattributes().HEALTH + " -> " + newHealth);
                    poorguy.getMyattributes().HEALTH = newHealth;
                }

                poorguy.notifyDisaster(boom);
//                }
            }
        }

    }

    private boolean sendCriticalAgents() {
        Message statusUpdate = new Message();
        statusUpdate.type = CTANetwork.RMQ_TYPE_AGENT_DATA;

        ArrayList<Person> criticalAgents = new ArrayList<Person>();

        //Collect all the critically injured agents and send
        for (Agent agent : agents) {
            Person person = (Person) agent;
            if (person.getMyattributes().getHealth() <= PersonAttributes.CRITICAL_HEALTH) {
                criticalAgents.add(person);
            }
        }

        statusUpdate.messageObject = criticalAgents;

        ArrayList<String> broadCastList = CTANetwork.hostTypeMap.get(Constants.CTA_TYPE_EMERGENCY_SERVICE);

        Utilities.Log.logger.info("PersonCTA::The number of Critically Injured people: " + criticalAgents.size());
        /*Chcek if there is a VehicleCTA at all?
         * If there are no vehicle CTA it means there are no vehicles. Hence the TrafficLightCTA can exit
         */
        if (broadCastList == null || broadCastList.size() == 0) {
            Utilities.Log.logger.info("PersonCTA::There are no emergency CTAs to send the message to.");
            //return;
            Utilities.Log.logger.info("Exiting PersonCTA");
            //exitSystem();
            return false;
        } else {

            ArrayList<String> activeHosts = new ArrayList<String>();
            for (String host : broadCastList) {
                //Check the status of the VehicleCTAs
                //Utilities.Log.logger.info("Current host status: " + host + ":" + CTAStatus.get(host));
                if (CTAStatus.get(host) != CTANetwork.CTA_DONE_WITH_WORK) {
                    activeHosts.add(host);
                } else {
                    Utilities.Log.logger.info("PersonCTA:: " + host + "Host has Done with its work.");
                }
                Utilities.Log.logger.info("PersonCTA:: ActiveHost Size: " + activeHosts.size());
            }

            Utilities.Log.logger.info("PersonCTA:: " + activeHosts.size() + " EmergencyServiceCTAs are currently active.");
            if (activeHosts.size() == 0) {
                Utilities.Log.logger.info("There are no emergencyService CTAs to send the message to.");
                Utilities.Log.logger.info("Exiting PersonCTA");
                //exitSystem();
                return false;
            } else {
                for (String host : activeHosts) {
                    //queueManager.send(host, outputBuffer.toByteArray());
                    queueManager.send(host, statusUpdate);
                }
                return true;
            }
        }
    }

    private void createStyles() {
        

        CustomStyle healthy = new CustomStyle(Constants.PERSON_HEALTHY, Constants.PERSON_SCALE, Constants.PERSON_IMAGE);
        CustomStyle injured = new CustomStyle(Constants.PERSON_INJURED, Constants.PERSON_SCALE, Constants.PERSON_IMAGE);
        CustomStyle critical = new CustomStyle(Constants.PERSON_CRITICAL, Constants.PERSON_SCALE, Constants.PERSON_IMAGE);
        CustomStyle dead = new CustomStyle(Constants.PERSON_DEAD, Constants.PERSON_SCALE, Constants.PERSON_IMAGE);

        peopleStyles.put("Healthy", healthy);
        peopleStyles.put("Injured", injured);
        peopleStyles.put("Critical", critical);
        peopleStyles.put("Dead", dead);

        kmlUtility =new KmlUtility(peopleStyles);
    }

    public void receivedMessage(Message message) {
        //update status of the host from which message was received
        while (holdMessages) {
        }
        switch (message.type) {
            case CTANetwork.RMQ_TYPE_STATUS_UPDATE:
                //Find out what type of status update it is
                Message statusType = (Message) message.messageObject;
                switch (statusType.type) {
                    case CTANetwork.CTA_READY_FOR_NEXT_TICK:
                        changeCTAStatus(statusType.hostName, statusType.type);
                        Utilities.Log.logger.info("Received Next Tick from : " + statusType.hostName + ":" + statusType.type);
                        break;
                    case CTANetwork.CTA_DONE_WITH_WORK:
                        changeCTAStatus(statusType.hostName, statusType.type);
                        Utilities.Log.logger.info("Received Done with work from : " + statusType.hostName + ":" + statusType.type);
                        break;
                    case CTANetwork.CTA_SAVING_STATE:
                        Utilities.Log.logger.info("Received state save message from " + statusType.hostName + "at tick: " + currentTickNumber);
                        tickNumberForStateSave = (Integer) statusType.messageObject;
                        initiateStateSave = true;
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_SAVED_STATE:
                        Utilities.Log.logger.info("Received state saved message from " + statusType.hostName + "at tick: " + currentTickNumber);
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_RESTORED_STATE:
                        Utilities.Log.logger.info("Received restored state message from " + statusType.hostName + "at tick: " + currentTickNumber);
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
//                    case CTANetwork.CTA_COMPLETE_EXIT:
//                        Utilities.Log.logger.info("VehicleCTA: Done with work and Exitting.....");
//                        changeCTAStatus(statusType.hostName, statusType.type);
//                        break;
                }
                break;

            case CTANetwork.RMQ_TYPE_AGENT_DATA:
                List<Person> pickedUpPersonList = (List<Person>) message.messageObject;
                //chcek for duplicates and update the critically injured list
                Utilities.Log.logger.info("Received Picked up People List : " + pickedUpPersonList.size());
                for (Person person : pickedUpPersonList) {
                    if (agents.contains(person)) {
                        agents.remove(person);
                    }
                }
                //Utilities.Log.logger.info("Received Critically Injured People " + criticalPersonList.size() + "With values (id,value):" + criticalPersonList);
                break;
        }
        Utilities.Log.logger.info("PeopleCTA: ReceivedMessage");

    }
}
