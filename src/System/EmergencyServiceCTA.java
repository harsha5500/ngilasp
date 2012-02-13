package System;

import Agents.Agent;
import Agents.Person;
import Agents.Vehicle;
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

/**
 * Care taker agent for emergency agents. This Cate Taker is responsible for
 * emergency vehicles only, i.e. Police Vehicles, ambulances.
 * Each type of vehicle will have a characteristic behaviour. Information exchange
 * will exist between EmergencyService and Poeple Care Takesrs, etc. 
 * @see CareTakerAgent
 * @see EmergencyService
 * @see Vehicle
 * @see AmbulanceMoveBehaviour
 * @see FireMoveBehaviour
 * @see PoliceMoveBehaviour
 */
public class EmergencyServiceCTA extends CareTakerAgent {


    HashMap<String, CustomStyle> vehicleStyles = new HashMap<String, CustomStyle>();
    
    /**
     * Total number of emergency vehicles that are hadled by
     * this CTA.
     */
    private int totalNumberOfVehicles;
    /**
     * Number of police Vehicles
     */
    private int numberOfPolice;
    /**
     * Number of ambulances
     */
    private int numberOfAmbulances;
    /**
     * number of fire brigades
     */
    private int numberOfFirebrigades;
    /**
     * The list of locations where the agents are located.
     */
    private ArrayList<Location> agentLocations;
    /**
     * The styles that represent the agents.
     */
    private ArrayList<String> agentStyles;
    /**
     * Stop receiving messages when the flag is true.
     */
    private boolean holdMessages = false;
    private boolean restoreState = false;

    /**
     * Constructor to create a EmergencyService CTA.
     */
    public EmergencyServiceCTA() {
        Utilities.Log.ConfigureLogger(); //Start the loggger
        readConfigurations();// Read the configuration files.
        createObjects(); //Create the necessary objects, agents, styles, etc. See CareTakerAgent.java
        buildCTAStatus();//Find the status of the other CTAs in the network.
        addQueueListener();//Add the message listener
        //Total number of vehicles = no Ambulances + no Police + no Fire
        setTotalNumberOfVehicles(SharedData.numberOfAmbulances + SharedData.numberOfFireVehicles + SharedData.numberOfPoliceVehicles);
        setNumberOfAmbulances(SharedData.numberOfAmbulances);
        setNumberOfFireVehicles(SharedData.numberOfFireVehicles);
        setNumberOfPoliceVehicles(SharedData.numberOfPoliceVehicles);
        Utilities.Log.logger.info("======Emergency Service CTA Started=====");
        logCurrentCTAStatus();
    }

    /**
     *  This function prints out the status of all the EmergencyService vehicles
     * along with the number of CTAs involved in the simulation.
     */
    private void logCurrentCTAStatus() {
        Utilities.Log.logger.info("EmergencyService CTA" + "=== Ambulance: " + getNumberOfAmbulances() + "== Police: " + getNumberOfPoliceVehicles() + "== Fire Engines: " + getNumberOfFireVehicles() + "== TOTAL: " + getTotalNumberOfVehicles());
        Utilities.Log.logger.info("EmergencyService CTA - Number of CTAs in the network: " + CTANetwork.hosts.size());
    }

    private void logRescueStatus() {
        Utilities.Log.logger.info("EmergencyService CTA== New Injured People: " + SharedData.newInjuredPeople.size() + "== Serving Injured: " + SharedData.injuredPeopleBeingServed.size() + "== Rescued People: " + SharedData.injuredPeopleRescued.size());
    }

    /**
     * Creates all the objects necessary for the CTA operation.
     * @see EmergencyServiceCTA
     * @see CareTakerAgent
     */
    private void createObjects() {
        CTAStatus = Collections.synchronizedMap(new HashMap<String, Integer>());
        agents = new ArrayList<Agent>();
        agentStyles = new ArrayList<String>();
    }

    /**
     *
     * @param filename
     */
    public EmergencyServiceCTA(String filename) {
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
     * Create the individual EmergencyService vehicles, i.e. ambulances, police
     * and fire vehicles.
     */
    void createVehicles() {
        // For each hospital in the system create the number of ambulances.
        for (int i = 0; i < SharedData.ambulancePerHospital.size(); i++) {
            int noOfAmbulanceCreated = 0;
            //Till the required number of ambulances are created per hospital repeat
            while (noOfAmbulanceCreated != SharedData.ambulancePerHospital.get(i)) {
                int capacity = GenerateAttributes.generateCapacity();
                int curLoad = GenerateAttributes.generateLoad(capacity);
                int speed = GenerateAttributes.generateSpeed();
                String type = Constants.VEHICLE_TYPE_AMBULANCE;
                EmergencyService v = null;
                try {
                    v = new EmergencyService(type, capacity, speed, curLoad, SharedData.hospitals.get(i));
                } catch (InstantiationException ex) {
                    Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
                }
                agents.add(v);
                agentStyles.add(Constants.VEHICLE_TYPE_AMBULANCE);
                noOfAmbulanceCreated++;
            }
        }
        Utilities.Log.logger.info("Finished Creating Ambulances. Added " + agents.size() + " agents");

        //Create the fire vehicles
        for (int i = 0; i < SharedData.numberOfFireVehicles; i++) {
            IdPointPair homeBaseIdPointPair = GenerateAttributes.generateIdPointPairOnRoad();
            long homebaseId = homeBaseIdPointPair.id;

            IdPointPair currentLocationIdPointPair = GenerateAttributes.generateIdPointPairOnRoad();
            long startcellId = currentLocationIdPointPair.id;

            Point currentLocation = currentLocationIdPointPair.point;
            Location homeBase = new Location(currentLocationIdPointPair.point, "");

            int capacity = GenerateAttributes.generateCapacity();
            int curLoad = GenerateAttributes.generateLoad(capacity);
            Cell cell = new Cell(currentLocation, true);
            int speed = GenerateAttributes.generateSpeed();
            String type = Constants.VEHICLE_TYPE_FIRE;
            EmergencyService v = null;
            try {
                v = new EmergencyService(type, capacity, speed, curLoad, homeBase, cell, startcellId, homebaseId);
            } catch (InstantiationException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
            agents.add(v);
            agentStyles.add(Constants.VEHICLE_TYPE_FIRE);
        }
        Utilities.Log.logger.info("Finished Creating Fire Vehicles. Added " + agents.size() + " agents");

        //Create the Police Vehicles
        for (int i = 0; i < SharedData.numberOfPoliceVehicles; i++) {
            IdPointPair homeBaseIdPointPair = GenerateAttributes.generateIdPointPairOnRoad();
            long homebaseId = homeBaseIdPointPair.id;

            IdPointPair currentLocationIdPointPair = GenerateAttributes.generateIdPointPairOnRoad();
            long startcellId = currentLocationIdPointPair.id;

            Point currentLocation = currentLocationIdPointPair.point;
            Location homeBase = new Location(currentLocationIdPointPair.point, "");
            int capacity = GenerateAttributes.generateCapacity();
            int curLoad = GenerateAttributes.generateLoad(capacity);
            Cell cell = new Cell(currentLocation, true);
            int speed = GenerateAttributes.generateSpeed();
            String type = Constants.VEHICLE_TYPE_POLICE;
            //Utilities.Log.logger.info(currentLocation.toString() + " " + homeBase.toString() + " " + lifeAttribs.toString());
            EmergencyService v = null;
            try {
                v = new EmergencyService(type, capacity, speed, curLoad, homeBase, cell, startcellId, homebaseId);
            } catch (InstantiationException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
            agents.add(v);
            agentStyles.add(Constants.VEHICLE_TYPE_POLICE);
        }
        Utilities.Log.logger.info("Finished Creating Police Vehicles. Added " + agents.size() + " agents");
        Utilities.Log.logger.info("Finished Creating All Vehicles. Added " + agents.size() + " agents");
    }

    /**
     * Check if all the agents are ready for next tick.
     * @return true if all agents are ready.
     */
    @Override
    public boolean checkIfAllAgentsReadyForNextTick() {

        /**TODO
         * This logic can be changed to exit as soon as you find p.statusFlag as false
         */
        int count = 0;
        for (Agent p : agents) {
            if (p.statusFlag) {
                count++;
            }
        }
        if ((count == agents.size()) && (count > 0)) {
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

    /**
     * Cleanup operations performed by the care taker after each run of all agents.
     * The cleanup operation adds the placemarks to the kml files and writes out
     * the output. In addition it changes the status of the CTA to ready for next
     * tick.
     */
    private void cleanUp() {
        sendReadyForTick();
        Utilities.Log.logger.info(agents.size() + " yet to reach home");
        //kmlUtility.addPlacemarks(agentLocations, agentStyles, currentTickNumber);
         kmlUtility.addPlacemarks(agentLocations, agents, "emergency", agentStyles,this.getCurrentTickNumber());
        if (currentTickNumber % 50 == 0) {
            writeKMLFile("emergency");
            kmlUtility = new KmlUtility(vehicleStyles);
        }

        for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
        }
        if (currentTickNumber % SharedData.saveStateAtTick == 0 && SharedData.isStateSaveEnabled) {
            initiateStateSave = false;

            Utilities.Log.logger.info("Sending message for CTAs to save their respective states at tick: " + currentTickNumber);
            sendSavingState(currentTickNumber);
            beginStateSave();
        }
//            changeSavedCTAsToReady();
        changeCTAStatus(Constants.localHost, CTANetwork.CTA_READY_FOR_NEXT_TICK);
        holdMessages = false;
//        }
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

    /**
     * Create the all the style objects that will be used by the any of the
     * emergency service agents.
     */
    private void createStyles() {
        
        CustomStyle fireVehicle = new CustomStyle(Constants.VEHICLE_FIRE, Constants.FIRE_SCALE, Constants.VEHICLE_FIRE_IMAGE);
        CustomStyle ambulanceVehicle = new CustomStyle(Constants.VEHICLE_AMBULANCE, Constants.AMBULANCE_SCALE, Constants.VEHICLE_AMBULANCE_IMAGE);
        CustomStyle policeVehicle = new CustomStyle(Constants.VEHICLE_POLICE, Constants.POLICE_SCALE, Constants.VEHICLE_POLICE_IMAGE);


        //TODO change key (strings) to constants
        vehicleStyles.put(Constants.VEHICLE_TYPE_POLICE, policeVehicle);
        vehicleStyles.put(Constants.VEHICLE_TYPE_FIRE, fireVehicle);
        vehicleStyles.put(Constants.VEHICLE_TYPE_AMBULANCE, ambulanceVehicle);
        kmlUtility = new KmlUtility(vehicleStyles);
    }

    /**
     * Set total number of vehicles
     * @param totalNumberOfVehicles
     */
    public void setTotalNumberOfVehicles(int totalNumberOfVehicles) {
        this.totalNumberOfVehicles = totalNumberOfVehicles;
    }

    /**
     * This method is called to set up the CTA for operation
     */
    private void setUp() {
        // Boot functions
        if (Utilities.AIDGenerator.initializeAIDGen()) {
            createStyles();

        } else {
            Utilities.Log.logger.info("AIDs cannot be generated");
            //System.exit(0);
            exitSystem();
        }

        // Generate Agents
        if (!restoreState) {
            createVehicles();
        }

        Utilities.Log.logger.info("Finished EmergencyService SetUp");
        sendReadyForTick();
    }

    /**
     * The run agents calls the individual agents run methods and advances the
     * agent by one tick. The CTA does not move forward untill all agents have
     * completed their run behaviour once within a given timeout.
     * The EmergencyService run agent calls the run method of the agents but does not
     * check if the agents are home as these agents are active for the entire simulation,
     * similar to a real emergency services.
     * @see CareTakerAgent
     */
    private void runAgents() {
        
        activateAgentBehaviour();
        agentLocations = new ArrayList<Location>();
        agentStyles = new ArrayList<String>();
        while (checkIfAllAgentsReadyForNextTick()) {

            //Check if agent a given agent has reached the home base
            for (int i = agents.size() - 1; i > -1; i--) {

                //if (((EmergencyService) agents.get(i)).isHome) {
                //agents.remove(i);
                //i = agents.size() - 1;
                //  continue;
                //}

                agents.get(i).statusFlag = false;
                agentLocations.add(new Location(((EmergencyService) agents.get(i)).getCurrentLocation(), ""));
                agentStyles.add(((EmergencyService) agents.get(i)).getAttributes().TYPE);

            }
            //String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
            //kmlutility.writeFile("kml/" + stamp + ".kml");
        }
        Utilities.Log.logger.info("EmergencyServiceCTA completed running agents.");
    }

    /**
     * Any specific CTA initilizations are done here.
     */
    void initialize() {
    }

    /**
     *  This function checks if all the agents on this CTA have satisfied their
     * objective, in which case the CTA has completed its task.
     * @return true if all agents in the CTA have completed their objectives
     */
    private boolean objectiveSatisfiedForAllAgents() {
        int count = 0;
        for (Agent p : agents) {
            if (p.objectiveFlag) {
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
     *  This method complets one iteration of the CTA's operation. The method
     * terminates when all the agents in the CTA have completed their tasks.
     */
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
                        Logger.getLogger(EmergencyServiceCTA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (initiateStateSave && currentTickNumber >= tickNumberForStateSave) {
                } else {
                currentTickNumber++;
                Utilities.Log.logger.info("Tick Number: " + currentTickNumber);


                kaboom();
                holdMessages = true;
                changeReadyCTAsToComputing();
//                CTAsReadyForTick.clear();
//                CTAsReadyForTick.addAll(CTAsCompletedRun);


                //If sending the critical agent fails then stop the CTA
                if (sendPickedUpPeople() == false) {
                    Utilities.Log.logger.info("Failed to send pickedUp agent data");
                    //break;
                } else {
                    //clear the previously picked up agents after sending the list to PeopleCTA
                    SharedData.pickedUpinjuredPeople.clear();
                }

                holdMessages = false;
                runAgents();

//                if (agents.size() < 300) {
//                    SharedData.isDisasterTriggered = true;
//                }

                   }
                cleanUp();
            }
            exitSystem();
        } catch (Exception e) {
            Utilities.Log.logger.error("Unable to complete work due to exection. Terminating " + e.getMessage());
            e.printStackTrace();
            exitSystem();
        }
    }

    /**
     * CTA Main function
     * @param args currently no arguements are passed
     */
    public static void main(String args[]) {
        EmergencyServiceCTA obj = new EmergencyServiceCTA();
        obj.runCTA();
    }

    /**
     * this method is called by the CTA when it needs to terminate.
     */
    private void exitSystem() {
        sendDoneWithWork();
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        kmlUtility.writeFile("kml/" + stamp + ".kml");
        
        System.exit(0);
    }

    /**
     * This is the method called to handle any message received by this CTA
     * @param message the message object form any other CTA
     * @see CTANetwork
     * @see Messaging.QueueParameters
     * @see Messaging.QueueManager
     */
    public void receivedMessage(Message message) {
        //update status of the host from which message was received
        while (holdMessages) {
        }
        switch (message.type) {
            case CTANetwork.RMQ_TYPE_STATUS_UPDATE:
                //Find out what type of status update it is
                Utilities.Log.logger.info("EmergencyServiceCTA: Status Update Message");
                Message statusType = (Message) message.messageObject;
                switch (statusType.type) {
                    case CTANetwork.CTA_READY_FOR_NEXT_TICK: //Some CTA is ready for next tick
                        Utilities.Log.logger.info("EmergencyServiceCTA: Ready for next Tick");
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_DONE_WITH_WORK: //Some CTA has completed its RunCTA method
                        Utilities.Log.logger.info("EmergencyServiceCTA: Done with work");
                        changeCTAStatus(statusType.hostName, statusType.type);
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
            case CTANetwork.RMQ_TYPE_AGENT_DATA: //People CTA has sent data
                List<Person> criticalPersonList = (List<Person>) message.messageObject; // De-serilize the Injured people data
                //chcek for duplicates and update the critically injured list
                Utilities.Log.logger.info("Received Critically Injured People " + criticalPersonList.size());// + "With values (id,value):" + criticalPersonList);
                for (Person person : criticalPersonList) {
                    if (!SharedData.newInjuredPeople.contains(person)) {
                        SharedData.newInjuredPeople.add(person);
                    }
                }
                break;
        }
    }

    /**
     * Send the list of critically injured people who were picked up at the
     * disaster location. and need to be removed from the visualization.
     * @return true if the message was sent successfully.
     */
    private boolean sendPickedUpPeople() {

        Message statusUpdate = new Message();
        statusUpdate.type = CTANetwork.RMQ_TYPE_AGENT_DATA;

        //ArrayList<Person> pickedUpAgents = new ArrayList<Person>();

        statusUpdate.messageObject = SharedData.pickedUpinjuredPeople;

        ArrayList<String> broadCastList = CTANetwork.hostTypeMap.get(Constants.CTA_TYPE_PEOPLE);

        Utilities.Log.logger.info("EmergencyServiceCTA:: Sending PickedupAgent List: " + SharedData.pickedUpinjuredPeople.size());

        /*Chcek if there is a PeopleCTA at all?
         * If there are no vehicle CTA it means there are no vehicles. Hence the TrafficLightCTA can exit
         */
        if (broadCastList == null || broadCastList.size() == 0) {
            Utilities.Log.logger.info("EmergencyServiceCTA::There are no people CTAs to send the message to.");
            //return;
            Utilities.Log.logger.info("Exiting EmergencyServiceCTA");
            //exitSystem();
            return false;
        } else {

            ArrayList<String> activeHosts = new ArrayList<String>();
            for (String host : broadCastList) {
                //Check the status of the PeopleCTAs
                //Utilities.Log.logger.info("Current host status: " + host + ":" + CTAStatus.get(host));
                if (CTAStatus.get(host) != CTANetwork.CTA_DONE_WITH_WORK) {
                    activeHosts.add(host);
                } else {
                    Utilities.Log.logger.info("EmergencyServiceCTA:: " + host + "Host has Done with its work.");
                }
                Utilities.Log.logger.info("EmergencyServiceCTA:: ActiveHost Size: " + activeHosts.size());
            }

            Utilities.Log.logger.info("EmergencyServiceCTA:: " + activeHosts.size() + " PeopleCTAs are currently active.");
            if (activeHosts.size() == 0) {
                Utilities.Log.logger.info("There are no people CTAs to send the message to.");
                Utilities.Log.logger.info("Exiting EmergencyServiceCTA");
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

    /**
     *
     */
    @Override
    protected void saveCTAState() {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        DataOutputStream dos = null;
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        String fileName = "savedstates/" + stamp + "_" + currentTickNumber + "_EmergencyCTA.data";
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
            dos.writeInt(totalNumberOfVehicles);
            dos.writeInt(numberOfAmbulances);
            dos.writeInt(numberOfFirebrigades);
            dos.writeInt(numberOfPolice);
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
            totalNumberOfVehicles = (int) din.readInt();
            numberOfAmbulances = (int) din.readInt();
            numberOfFirebrigades = (int) din.readInt();
            numberOfPolice = (int) din.readInt();
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
     * Return total number of vehicles
     * @return total number of vehicles.
     */
    public int getTotalNumberOfVehicles() {
        return totalNumberOfVehicles;
    }

    /**
     * Return the total number of ambulances
     * @return total number of ambulances
     */
    public int getNumberOfAmbulances() {
        return numberOfAmbulances;
    }

    /**
     * Set the number of ambulances
     * @param numberOfAmbulances number of ambulances
     */
    public void setNumberOfAmbulances(int numberOfAmbulances) {
        this.numberOfAmbulances = numberOfAmbulances;
    }

    /**
     * Return the number of Fire vehicles
     * @return the number of fire vehicles
     */
    public int getNumberOfFireVehicles() {
        return numberOfFirebrigades;
    }

    /**
     * Set the number of fire vehicles
     * @param numberOfFirebrigades number of fire vehicles
     */
    public void setNumberOfFireVehicles(int numberOfFirebrigades) {
        this.numberOfFirebrigades = numberOfFirebrigades;
    }

    /**
     * Return the number of police vehicles
     * @return the number of police vehicles
     */
    public int getNumberOfPoliceVehicles() {
        return numberOfPolice;
    }

    /**
     * Set the number of police vehicles
     * @param numberOfPolice number of police vehicles
     */
    public void setNumberOfPoliceVehicles(int numberOfPolice) {
        this.numberOfPolice = numberOfPolice;
    }

    /**
     * This method is invoked when the disaster is triggered. The method performs
     * the necessary state changes and informs the individual agents about the disaster.
     */
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
            SharedData.isDisasterTriggered = true;

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
            Iterator<Agent> vehicles = agents.iterator();


            //addInjuredPerson();
            while (vehicles.hasNext()) {
                EmergencyService erService = (EmergencyService) vehicles.next();

                erService.notifyDisaster(boom);
//                }

            }
        }

    }
}
