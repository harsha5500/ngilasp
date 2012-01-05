package System;

import Agents.Agent;
import Agents.Behaviour.VehicleMoveBehaviour;
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
 * Care taker agent for vehicle agents. This Cate Taker is responsible for
 * civilian vehicles only. Therefore the vehilces will follow traffic rules, etc.
 * Information exchange will exist between Traffic Light's Care Taker and
 * the Vehicle's Cate Taker. All references to vehicles in this
 * document will be civilian vehicles.
 * @see CareTakerAgent
 * @see Vehicle
 * @see VehicleMoveBehaviour
 */
public class VehicleCTA extends CareTakerAgent {

    HashMap<String, CustomStyle> vehicleStyles = new HashMap<String, CustomStyle>();
    /**
     * Total number of civilian vehicles
     */
    private int totalNumberOfVehicles;
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
     * Constructor to create the vehicle CTA
     */
    public VehicleCTA() {
        Utilities.Log.ConfigureLogger(); //Start the loggger
        readConfigurations();// Read the configuration files.
        createObjects();
        buildCTAStatus();
        addQueueListener();
        setTotalNumberOfVehicles(SharedData.numberOfCivilVehicles);
        Utilities.Log.logger.info("Vehicle CTA Started");
        Utilities.Log.logger.info("Number of CTAs : " + CTANetwork.hosts.size() + " with a total of " + getTotalNumberOfVehicles() + "vehicles.");
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

    private VehicleCTA(String filename) {
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
     * Create the individual cilillian vehicles.
     */
    void createVehicles() {
        //Create the civilian vehicles
        for (int i = 0; i < SharedData.numberOfCivilVehicles; i++) {
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
            String type = Constants.VEHICLE_TYPE_CIVIL;
            //Utilities.Log.logger.info(currentLocation.toString() + " " + homeBase.toString() + " " + lifeAttribs.toString());
            Vehicle v = null;
            try {
                v = new Vehicle(capacity, curLoad, homeBase, cell, speed, type, startcellId, homebaseId);
            } catch (InstantiationException ex) {
                Utilities.Log.logger.info("Error creating a new vehicle. " + ex.getMessage());
                //Since the vehicles could not be created exit the CTA
                exitSystem();
            }
            agents.add(v);
            agentStyles.add(Constants.VEHICLE_TYPE_CIVIL);
        }
        Utilities.Log.logger.info("Finished Creating Civilian Vehicles. Added " + agents.size() + " agents");
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
        kmlUtility.addPlacemarks(agentLocations, agents, "vehicle", agentStyles, this.getCurrentTickNumber());
        if (currentTickNumber % 50 == 0) {
            writeKMLFile("vehicle");
            kmlUtility = new KmlUtility(vehicleStyles);
        }

        for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
        }
        if ((currentTickNumber % SharedData.saveStateAtTick == 0 && SharedData.isStateSaveEnabled) || initiateStateSave) {
            initiateStateSave = false;
            Utilities.Log.logger.info("Sending message for CTAs to save their respective states at tick: " + currentTickNumber);
            sendSavingState(currentTickNumber);
            beginStateSave();
            holdMessages = true;
            changeSavedCTAsToReady();
            holdMessages = false;
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

    /**
     * Return total number of vehicles
     * @return total number of vehicles.
     */
    public int getTotalNumberOfVehicles() {
        return totalNumberOfVehicles;
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


            while (vehicles.hasNext()) {
                Vehicle vehicle = (Vehicle) vehicles.next();

                vehicle.notifyDisaster(boom);
//                }
            }
        }

    }

    /**
     * Create the all the style objects that will be used by the any of the
     * vehicle agents
     */
    private void createStyles() {

        CustomStyle defaultVehicle = new CustomStyle(Constants.VEHICLE_SCALE, Constants.VEHICLE_DEFAULT_IMAGE);

        //TODO change key (strings) to constants  
        vehicleStyles.put("civil", defaultVehicle);
        kmlUtility = new KmlUtility(vehicleStyles);
    }

    /**
     * Set total number of vehicles
     * @param totalNumberOfVehicles total number of vehicles
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

        Utilities.Log.logger.info("Finished Vehicle SetUp");
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
            for (int i = 0; i < agents.size(); i++) {
                if (((Vehicle) agents.get(i)).getAttributes().isAgentHome) {
                    agents.get(i).statusFlag = true;
                    agents.remove(i);
                    i--;
                    //continue;
                } else {
                    agents.get(i).statusFlag = false;
                    agentLocations.add(new Location(((Vehicle) agents.get(i)).getCurrentLocation(), ""));
                    agentStyles.add(((Vehicle) agents.get(i)).getAttributes().TYPE);
                }

            }
            //String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
            //kmlutility.writeFile("kml/" + stamp + ".kml");
        }
        Utilities.Log.logger.info("VehicleCTA completed running agents.");
    }

    /**
     * Any specific CTA initilizations are done here.
     */
    private void initialize() {
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
                        Utilities.Log.logger.info(ex.getMessage());
                        //Since the runCTA was intrrupted, exit the CTA.
                        exitSystem();
                    }
                }
                if (initiateStateSave && currentTickNumber >= tickNumberForStateSave) {
                } else {
                    kaboom();
                    holdMessages = true;
                    changeReadyCTAsToComputing();
//                CTAsReadyForTick.clear();
//                CTAsReadyForTick.addAll(CTAsCompletedRun);
                    holdMessages = false;
                    runAgents();

                    if (agents.size() < 300) {
                        SharedData.isDisasterTriggered = true;
                    }
                    Utilities.Log.logger.info("Current Tick Number: " + currentTickNumber);
                    currentTickNumber++;
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
        if (args.length == 0) {
            VehicleCTA obj = new VehicleCTA();
            obj.runCTA();
        } else {
            VehicleCTA obj = new VehicleCTA(args[0]);
            obj.restoreState = true;
            obj.runCTA();
        }
    }

    /**
     * this method is called by the CTA when it needs to terminate.
     */
    private void exitSystem() {
        sendDoneWithWork();
//        sendFinishedExecutingCTA();
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        kmlUtility.writeFile("kml/" + stamp + ".kml");
        System.exit(0);
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
        String fileName = "savedstates/" + stamp + "_" + currentTickNumber + "_VehicleCTA.data";
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
                Utilities.Log.logger.info("VehicleCTA: Status Update Message");
                Message statusType = (Message) message.messageObject;
                switch (statusType.type) {
                    case CTANetwork.CTA_READY_FOR_NEXT_TICK:
                        Utilities.Log.logger.info("VehicleCTA: Ready for next Tick");
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_DONE_WITH_WORK:
                        Utilities.Log.logger.info("VehicleCTA: Done with work");
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
            case CTANetwork.RMQ_TYPE_AGENT_DATA:
                //trafficLightStatus = (HashMap<Long, Integer>) message.messageObject;
                SharedData.trafficLights = (ArrayList<TrafficLight>) message.messageObject;
                Utilities.Log.logger.info("Received Traffic Light Status: No of Lights = " + SharedData.trafficLights.size());
                break;
        }

    }
}
